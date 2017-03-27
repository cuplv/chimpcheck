package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.util.Log;
import android.view.KeyEvent;


import android.view.MotionEvent;
import android.view.View;
import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;

import java.util.*;

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

        try {
            EventTraceOuterClass.TryEvent.TryType tryType = tryEvent.getTryType();
            switch (tryType) {
                case APPEVENT: executeEvent( tryEvent.getAppEvent() ); break;
                case EXTEVENT: executeEvent( tryEvent.getExtEvent() ); break;
                case TRACE:
                    for (EventTraceOuterClass.UIEvent uiEvent : tryEvent.getTrace().getEventsList()) {
                        executeEvent( uiEvent );
                    }
                    break;
            }
        } catch (NoViewEnabledException e) {
            Log.i(runner.chimpTag("EspressoChimpDriver@launchTryEvent"), e.toString());
        }
        /* Probably more catching here to do. Find out what Espresso throws when the view you click is not there.
        // Exception: android.support.test.espresso.NoMatchingViewException:
         catch (NoMatchingViewException e) { }
        */

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
    protected AppEventOuterClass.Click launchClickEvent(AppEventOuterClass.Click click) throws NoViewEnabledException {
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
                View view = getClickableView();

                Espresso.onView(withId(view.getId()))
                        .perform(click());

                // Should return click token with the UIID of the exact view clicked.
                // That's what below is doing. However, find out if there is better way to retain human-readable information on
                // this view (Display Text?)
                AppEventOuterClass.Click.Builder builder = AppEventOuterClass.Click.newBuilder();
                builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.R_ID).setRid(view.getId()));

                return builder.build();
        }

        return click;
    }

    @Override
    protected AppEventOuterClass.LongClick launchLongClickEvent(AppEventOuterClass.LongClick longClick) throws NoViewEnabledException {
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

                View view = getClickableView();

                Espresso.onView(withId(view.getId()))
                        .perform(longClick());

                // Should return click token with the UIID of the exact view clicked.
                AppEventOuterClass.LongClick.Builder builder = AppEventOuterClass.LongClick.newBuilder();
                builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.R_ID).setRid(view.getId()));

                return builder.build();
        }
        return longClick;
    }

    @Override
    protected AppEventOuterClass.Type launchTypeEvent(AppEventOuterClass.Type type) throws NoViewEnabledException {
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

                View view = getTypeableView(); // getClickableView();

                Espresso.onView(withId(view.getId()))
                        .perform(typeText(text));

                AppEventOuterClass.Type.Builder builder = AppEventOuterClass.Type.newBuilder();
                builder
                  .setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.R_ID).setRid(view.getId()))
                  .setInput(text);

                return builder.build();
        }
        // TODO
        return type;
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
        Instrumentation inst = InstrumentationRegistry.getInstrumentation();

        /*
        float fromY = swipe.getStart().getY();
        float fromX = swipe.getStart().getX();
        float toY = swipe.getEnd().getY();
        float toX = swipe.getEnd().getX();
        */

        AppEventOuterClass.UIID uiid = swipe.getUiid();
        switch(uiid.getIdType()) {
            case R_ID: uiid.getRid(); // R.id.XXX type
            case NAME_ID: uiid.getNameid(); // Display name type
            case WILD_CARD: // Wild card type
            case XY_ID:  uiid.getXyid(); // XY coordinate type
        }

        AppEventOuterClass.Orientation orientation = swipe.getPos();
        switch(orientation.getOrientType()) {
            case XY_TYPE: orientation.getXy(); // XY coordinate type
            case LEFT:
            case RIGHT:
            case UP:
            case DOWN:
        }

        /*
        //
            int stepCount = 10;
        //

        System.out.println("this drag actions");
        long downTime = SystemClock.uptimeMillis();
        long eventTime= SystemClock.uptimeMillis();
        float y = fromY;
        float x = fromX;
        float yStep = (toY - fromY) / stepCount;
        float xStep = (toX - fromX) / stepCount;
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, fromX, fromY, 0);
        try {
            inst.sendPointerSync(event);
        } catch (SecurityException ignored) {System.out.println("error 1");}
        for (int i = 0; i < stepCount; ++i){
            y += yStep;
            x += xStep;
            eventTime = SystemClock.uptimeMillis();
            event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, x, y, 0);
            try{
                inst.sendPointerSync(event);
            } catch (SecurityException ignored){System.out.println("error 2");}
        }
        eventTime = SystemClock.uptimeMillis();
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP,toX, toY, 0);
        try {
            inst.sendPointerSync(event);
        } catch (SecurityException ignored) {System.out.println("error 3");}
        */

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
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_MENU));
        // TODO
    }

    @Override
    protected void launchClickHome() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickHome"), "ClickHome");
        //adb shell input keyevent KEYCODE_HOME
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_HOME));
        // TODO
    }

    @Override
    protected void launchClickBack() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "ClickBack");
        //adb shell input keyevent KEYCODE_BACK
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_BACK));
        pressBack();

        // TODO
    }

    @Override
    protected void launchPullDownSettings() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchPullDownSettings"), "PullDownSettings");
        // TODO
    }

    @Override
    protected void launchResume() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchReturnToApp"), "Resume");
        //adb shell input keyevent KEYCODE_APP_SWITCH && adb shell input keyevent KEYCODE_DPAD_DOWN && adb shell input keyevent KEYCODE_ENTER
    }

    @Override
    protected void launchRotate() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchRotateLeft"), "Rotate");

        Activity activity = getActivityInstance();
        int orientation = activity.getApplicationContext().getResources().getConfiguration().orientation;
        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


    }

}
