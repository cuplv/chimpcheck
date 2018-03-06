package edu.colorado.plv.chimp.performers;

import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.Espresso;
import android.support.test.uiautomator.BySelector;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Random;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.managers.MatcherManager;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;
import edu.colorado.plv.chimp.viewmatchers.AmbiguousCounter;
import edu.colorado.plv.chimp.viewmatchers.MatchWithIndex;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by edmundlam on 5/19/17.
 */

public class ClickPerformer extends Performer<AppEventOuterClass.Click> {

    public ClickPerformer(ChimpDriver chimpDriver, ViewManager viewManager,
                          WildCardManager wildCardManager, BySelector wildCardSelector, BySelector wildCardChildSelector,
                          Matcher<View> userDefinedMatcher) {
        super("Click", chimpDriver, viewManager, wildCardManager, wildCardSelector, wildCardChildSelector, userDefinedMatcher);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.Click click) {
        return viewManager.retrieveTargets(click.getUiid());
    }

    @Override
    public AppEventOuterClass.Click performMatcherAction(AppEventOuterClass.Click origin, Matcher<View> matcher) {
        
        try{
            AmbiguousCounter.resetCounter();
            Espresso.onView(new AmbiguousCounter(allOf(matcher, isDisplayed()))).perform(click());
        } catch (AmbiguousViewMatcherException avme) {
            avme.printStackTrace();
            int counter = AmbiguousCounter.getCounter();
            AmbiguousCounter.resetCounter();
            Random seed = new Random(System.currentTimeMillis());
            int randIdx = seed.nextInt(counter);
            Espresso.onView(MatchWithIndex.withIndex(matcher, randIdx)).perform(click());
        }

        return origin;
    }

    @Override
    public AppEventOuterClass.Click performXYAction(AppEventOuterClass.Click origin, int x, int y) {
        Espresso.onView(isRoot()).perform(ChimpActionFactory.clickXY(x, y));
        return origin;
    }

    @Override
    public AppEventOuterClass.Click performWildCardTargetAction(AppEventOuterClass.Click origin, WildCardTarget target) {

        AppEventOuterClass.Click.Builder builder = AppEventOuterClass.Click.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID)
                .setNameid(MatcherManager.describeMatcherAsDisplay(target.uiObj)));
        Espresso.onView(target.uiMatcher).perform(click());
        return builder.build();

    }


}
