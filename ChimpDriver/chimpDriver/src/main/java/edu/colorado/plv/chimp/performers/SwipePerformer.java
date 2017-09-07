package edu.colorado.plv.chimp.performers;


import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.core.deps.guava.collect.ImmutableMap;
import android.support.test.uiautomator.BySelector;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Map;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.managers.MatcherManager;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;

/**
 * Created by Pezh on 9/5/17.
 */

public class SwipePerformer extends Performer<AppEventOuterClass.Swipe> {

    public SwipePerformer(ChimpDriver chimpDriver, ViewManager viewManager,
                         WildCardManager wildCardManager, BySelector wildCardSelector,
                         Matcher<View> userDefinedMatcher) {
        super("Swipe", chimpDriver, viewManager, wildCardManager, wildCardSelector, userDefinedMatcher);
    }

    Map<AppEventOuterClass.Orientation.OrientType, ViewAction> swipeActions = ImmutableMap.of(
            AppEventOuterClass.Orientation.OrientType.LEFT, swipeLeft(),
            AppEventOuterClass.Orientation.OrientType.RIGHT, swipeRight(),
            AppEventOuterClass.Orientation.OrientType.UP, swipeUp(),
            AppEventOuterClass.Orientation.OrientType.DOWN, swipeDown()

    );

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.Swipe swipe) {
        return viewManager.retrieveTargets(swipe.getUiid());
    }

    @Override
    public AppEventOuterClass.Swipe performMatcherAction(AppEventOuterClass.Swipe origin, Matcher<View> matcher) {
        ViewAction swipe = swipeActions.get(origin.getPos().getOrientType());
        Espresso.onView(matcher).perform(swipe);
        return origin;
    }
    @Override
    public AppEventOuterClass.Swipe performWildCardTargetAction(AppEventOuterClass.Swipe origin, WildCardTarget target) {
        ViewAction swipe = swipeActions.get(origin.getPos().getOrientType());
        Espresso.onView(target.uiMatcher).perform(swipe);


        AppEventOuterClass.Swipe.Builder builder = AppEventOuterClass.Swipe.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID)
                .setNameid(MatcherManager.describeMatcherAsDisplay(target.uiObj))).setPos(origin.getPos());
        return builder.build();
    }

    @Override
    public AppEventOuterClass.Swipe performXYAction(AppEventOuterClass.Swipe origin, int x, int y) {

        return origin;
    }




}

