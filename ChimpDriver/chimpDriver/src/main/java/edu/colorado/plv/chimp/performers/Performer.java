package edu.colorado.plv.chimp.performers;

import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiObject2;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;

import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import edu.colorado.plv.chimp.managers.MatcherManager;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;
import edu.colorado.plv.chimp.viewactions.ChimpStagingAction;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by edmundlam on 5/19/17.
 */

public abstract class Performer<Act> {

    public abstract ArrayList<ViewManager.ViewTarget> getTargets(Act origin);

    public abstract Act performMatcherAction(Act origin, Matcher<View> matcher);

    public abstract Act performWildCardTargetAction(Act origin, WildCardTarget target);

    public abstract Act performXYAction(Act origin, int x, int y);

    //public abstract Act performUiObjectAction(Act origin, UiObject uiObject) throws UiObjectNotFoundException;

    protected String displayAction;
    protected ChimpDriver chimpDriver;
    protected ViewManager viewManager;
    protected WildCardManager wildCardManager;
    protected BySelector wildCardSelector;
    protected Matcher<View> userDefinedMatcher;


    public Performer(String displayAction, ChimpDriver chimpDriver, ViewManager viewManager,
                     WildCardManager wildCardManager, BySelector wildCardSelector,
                     Matcher<View> userDefinedMatcher){
        this.displayAction = displayAction;
        this.chimpDriver = chimpDriver;
        this.viewManager = viewManager;
        this.wildCardManager = wildCardManager;
        this.wildCardSelector = wildCardSelector;
        this.userDefinedMatcher = userDefinedMatcher;
    }


    public String tag(String loc) {
        return displayAction + "-Performer@" + loc;
    }

    public Act performAction(Act origin) throws NoViewEnabledException {

        Act performedAction = null;

        for(ViewManager.ViewTarget target: getTargets(origin)) {
             try {
                 performedAction = performAction(origin, target);
                 if (performedAction != null) {
                     return performedAction;
                 }
             } catch (NoMatchingViewException e) {
                 Log.i(tag("performAction"),"Attempted " + target.toString() + " and failed: " + e.toString());
             }
        }

        Log.e(tag("performAction"),"Exhausted all action targets.");

        throw new NoViewEnabledException(tag("performAction") + ": exhausted all action targets");

    }

    public Act performAction(Act origin, ViewManager.ViewTarget target) {

        switch(target.targetType) {
            case Matcher:
                Matcher<View> matcher = ((ViewManager.ViewMatcherTarget) target).matcher;
                return performMatcherAction(origin, matcher);
            case XYCoord:
                ViewManager.ViewXYTarget xyTarget = ((ViewManager.ViewXYTarget) target);
                return performXYAction(origin, xyTarget.x, xyTarget.y);
            case WildCard:
                return performWildCardAction(origin);
            default:
                return null;
        }

    }

    public Act performWildCardAction(Act origin) {

        Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        chimpDriver.preemptiveTraceReport();

        try {
            ArrayList<UiObject2> uiObject2s = wildCardManager.retrieveUiObject2s(wildCardSelector);
            ArrayList<WildCardTarget> matchers = MatcherManager.getViewMatchers(uiObject2s, userDefinedMatcher);

            while(matchers.size() > 0) {
                Log.i(tag("wildcard"), Integer.toString(matchers.size()));
                WildCardTarget target = wildCardManager.popOne(matchers);
                try {
                    Log.i(tag("wildcard"), "Attempting to perform action on UiObject");
                    Act result = performWildCardTargetAction(origin, target);
                    return result;
                } catch (AmbiguousViewMatcherException avme){
                    Log.e(tag("wildcard"), avme.getStackTrace()[0].toString());
                } catch (NoMatchingViewException nmve){
                    Log.e(tag("wildcard"), nmve.getStackTrace()[0].toString());
                } catch (PerformException pe){
                    Log.e(tag("wildcard"), pe.getStackTrace()[0].toString());

                }
            }

        } catch (Exception ee) {
            Log.e(tag("wildcard"), "Error occurred at wild card top-level");
            ee.printStackTrace();
        }

        Log.e(tag("wildcard"), "Exhausted all wild card options.");
        return null;

    }



}
