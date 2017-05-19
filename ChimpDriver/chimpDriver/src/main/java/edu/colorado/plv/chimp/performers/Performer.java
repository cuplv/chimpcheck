package edu.colorado.plv.chimp.performers;

import android.graphics.Rect;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;
import edu.colorado.plv.chimp.viewactions.ChimpStagingAction;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by edmundlam on 5/19/17.
 */

public abstract class Performer<Act> {

    public abstract ArrayList<ViewManager.ViewTarget> getTargets(Act origin);

    public abstract Act performMatcherAction(Act origin, Matcher<View> matcher);

    public abstract Act performXYAction(Act origin, int x, int y);

    public abstract Act performUiObjectAction(Act origin, UiObject uiObject) throws UiObjectNotFoundException;

    protected String displayAction;
    protected ChimpDriver chimpDriver;
    protected ViewManager viewManager;
    protected WildCardManager wildCardManager;
    protected UiSelector wildCardTopSelector, wildCardChildSelector;

    public Performer(String displayAction, ChimpDriver chimpDriver, ViewManager viewManager, WildCardManager wildCardManager, UiSelector wildCardTopSelector, UiSelector wildCardChildSelector) {
        this.displayAction = displayAction;
        this.chimpDriver = chimpDriver;
        this.viewManager = viewManager;
        this.wildCardManager = wildCardManager;
        this.wildCardTopSelector = wildCardTopSelector;
        this.wildCardChildSelector = wildCardChildSelector;
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
            ArrayList<UiObject> uiObjects = wildCardManager.retrieveUiObjects(wildCardTopSelector, wildCardChildSelector);

            while(uiObjects.size() > 0) {
                UiObject uiObject = wildCardManager.popOne(uiObjects);
                try {
                    Log.i(tag("wildcard"), "Attempting to perform action on UiObject");
                    return performUiObjectAction(origin, uiObject);
                } catch (UiObjectNotFoundException e) {
                    Log.i(tag("wildcard"), "Failed clicking on UIObject: " + wildCardManager.uiObjectInfo(uiObject) + " " + e.toString());
                }
            }

        } catch (UiObjectNotFoundException e) {
            Log.e(tag("wildcard"), "Error occurred at wild card top-level");
        }

        Log.e(tag("wildcard"), "Exhausted all wild card options.");
        return null;

    }


}
