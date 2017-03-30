package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.util.Log;
import android.view.KeyEvent;


import android.view.View;
import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;



import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static edu.colorado.plv.chimp.driver.FingerGestures.drag;
import static edu.colorado.plv.chimp.driver.FingerGestures.swipeOnView;

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

        AppEventOuterClass.UIID uiid = swipe.getUiid();
        switch(uiid.getIdType()) {
            case R_ID:
                swipeOnView(Espresso.onView( withId(uiid.getRid()) ), swipe.getPos());
                return swipe;
            case NAME_ID:
                swipeOnView(Espresso.onView( withText(uiid.getNameid()) ), swipe.getPos()); // Display name type
                return swipe;
            case XY_ID:
                AppEventOuterClass.XYCoordin xy = uiid.getXyid(); // XY coordinate type
                drag(xy,  swipe.getPos());
            case WILD_CARD: // Wild card type
                return swipe;
            default:
                return swipe;
        }
    }

    @Override
    protected AppEventOuterClass.Sleep launchSleepEvent(AppEventOuterClass.Sleep sleep) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchSleepEvent"), sleep.toString());
        Espresso.onView(isRoot()).perform(waitFor(sleep.getTime()));
        return sleep;
    }

    @Override
    protected void launchClickMenu() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickMenu"), "ClickMenu");
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_MENU));
    }

    @Override
    protected void launchClickHome() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickHome"), "ClickHome");
        Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_HOME));
        // TODO
    }

    @Override
    protected void launchClickBack() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "ClickBack");
        try {
            Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_BACK));
        } catch(NoActivityResumedException e){
            try{
                Thread.sleep(100000);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
            e.printStackTrace();
        }

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
