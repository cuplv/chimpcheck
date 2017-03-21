package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.util.Log;
import android.view.KeyEvent;


import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;

import static android.support.test.espresso.Espresso.pressBack;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by edmund on 3/13/17.
 */
public class EspressoChimpDriver<A extends Activity> extends ChimpDriver<A> {

    // Try Event Block

    @Override
    protected EventTraceOuterClass.TryEvent launchTryEvent(EventTraceOuterClass.TryEvent tryEvent) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchTryEvent"), tryEvent.toString());
        // TODO

        // Exception: android.support.test.espresso.NoMatchingViewException:
        // } catch (NoMatchingViewException e){ //

        return tryEvent;
    }

    @Override
    protected EventTraceOuterClass.Decide launchDecideEvent(EventTraceOuterClass.Decide decide) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchDecideEvent"), decide.toString());
        // TODO
        return decide;
    }

    @Override
    protected EventTraceOuterClass.DecideMany launchDecideManyEvent(EventTraceOuterClass.DecideMany decideMany) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchDecideManyEvent"), decideMany.toString());
        // TODO
        return decideMany;
    }

    // User Events

    @Override
    protected AppEventOuterClass.Click launchClickEvent(AppEventOuterClass.Click click) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickEvent"), click.toString());
        AppEventOuterClass.UIID uiid = click.getUiid();
        switch (uiid.getIdType()) {
            case R_ID: Espresso.onView(withId(uiid.getRid()))
                                    .perform(click());
                       return click;
            case NAME_ID: Espresso.onView(withText(uiid.getNameid()))
                                    .perform(click());
                        return click;
            case WILD_CARD:
                // TODO: Should return click token with the UIID of the exact view clicked.

                Espresso.onView(withId(getClickableView().getId()))
                        .perform(click());

                return click;
        }

        return click;
    }

    @Override
    protected AppEventOuterClass.LongClick launchLongClickEvent(AppEventOuterClass.LongClick longClick) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchLongClickEvent"), longClick.toString());
        AppEventOuterClass.UIID uiid = longClick.getUiid();
        switch (uiid.getIdType()) {
            case R_ID:
                Espresso.onView(withId(uiid.getRid()))
                        .perform(longClick());
                return longClick;
            case NAME_ID:
                Espresso.onView(withText(uiid.getNameid()))
                        .perform(longClick());
                return longClick;
            case WILD_CARD:
                // TODO: Should return click token with the UIID of the exact view clicked.

                Espresso.onView(withId(getClickableView().getId()))
                        .perform(longClick());

                return longClick;
        }
        // TODO
        return longClick;
    }

    @Override
    protected AppEventOuterClass.Type launchTypeEvent(AppEventOuterClass.Type type) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchTypeEvent"), type.toString());
        AppEventOuterClass.UIID uiid = type.getUiid();
        String text = type.getInput();
        switch (uiid.getIdType()) {
            case R_ID:
                Espresso.onView(withId(uiid.getRid()))
                        .perform(typeText(text));
                return type;
            case NAME_ID:
                Espresso.onView(withText(uiid.getNameid()))
                        .perform(typeText(text));
                return type;
            case WILD_CARD:

                Espresso.onView(withId(getClickableView().getId()))
                        .perform(typeText(text));

                return type;
        }
        // TODO
        return type;
    }

    @Override
    protected AppEventOuterClass.Drag launchDragEvent(AppEventOuterClass.Drag drag) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchDragEvent"), drag.toString());
        // TODO
        return drag;
    }

    @Override
    protected AppEventOuterClass.Pinch launchPinchEvent(AppEventOuterClass.Pinch pinch) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchPinchEvent"), pinch.toString());
        // TODO
        return pinch;
    }

    @Override
    protected AppEventOuterClass.Swipe launchSwipeEvent(AppEventOuterClass.Swipe swipe) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchSwipeEvent"), swipe.toString());
        // TODO
        return swipe;
    }

    @Override
    protected AppEventOuterClass.Sleep launchSleepEvent(AppEventOuterClass.Sleep sleep) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchSleepEvent"), sleep.toString());
        try {
            Thread.sleep(sleep.getTime());
        } catch (Exception e) {
            Log.e(runner.chimpTag("EspressoChimpDriver@launchSleepEvent"), "Chimp messed up while sleeping:" + e.toString());
        }
        return sleep;
    }

    @Override
    protected void launchClickMenu() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickMenu"), "ClickMenu");
        //adb shell input keyevent KEYCODE_MENU
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_HOME));
        // TODO
    }

    @Override
    protected void launchClickHome() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickHome"), "ClickHome");
        //adb shell input keyevent KEYCODE_HOME
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_MENU));
        // TODO
    }

    @Override
    protected void launchClickBack() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "ClickBack");
        //adb shell input keyevent KEYCODE_BACK
        pressBack();
        // TODO
    }

    @Override
    protected void launchPullDownSettings() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchPullDownSettings"), "PullDownSettings");
        // TODO
    }

    @Override
    protected void launchReturnToApp() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchReturnToApp"), "ReturnToApp");
        //adb shell input keyevent KEYCODE_APP_SWITCH && adb shell input keyevent KEYCODE_DPAD_DOWN && adb shell input keyevent KEYCODE_ENTER

        // TODO
    }

    @Override
    protected void launchRotateLeft() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchRotateLeft"), "RotateLeft");

        Activity activity = getActivityInstance();
        int orientation = activity.getApplicationContext().getResources().getConfiguration().orientation;
        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

    @Override
    protected void launchRotateRight() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchRotateRight"), "RotateRight");
        launchRotateLeft();
    }

}
