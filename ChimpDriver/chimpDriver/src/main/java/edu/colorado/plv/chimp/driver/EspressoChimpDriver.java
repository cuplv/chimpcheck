package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.util.Log;


import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;
import edu.colorado.plv.chimp.exceptions.MalformedBuiltinPredicateException;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import edu.colorado.plv.chimp.exceptions.PropertyViolatedException;
import edu.colorado.plv.chimp.exceptions.ReflectionPredicateException;


import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.hamcrest.Matchers.allOf;

import edu.colorado.plv.chimp.performers.ClickPerformer;
import edu.colorado.plv.chimp.performers.LongClickPerformer;
import edu.colorado.plv.chimp.performers.SwipePerformer;
import edu.colorado.plv.chimp.performers.TypePerformer;
import edu.colorado.plv.chimp.viewactions.ChimpStagingAction;
import edu.colorado.plv.chimp.viewactions.OrientationChangeAction;

/**
 * Created by edmund on 3/13/17.
 */
public class EspressoChimpDriver /* <A extends Activity> */ extends ChimpDriver /* <A> */ {

    // Try Event Block

    @Override
    protected EventTraceOuterClass.TryEvent launchTryEvent(EventTraceOuterClass.TryEvent tryEvent)
    throws MalformedBuiltinPredicateException, ReflectionPredicateException, PropertyViolatedException {
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
        } catch (NoMatchingViewException nm){
            Log.i(runner.chimpTag("EspressoChimpDriver@launchTryEvent"), nm.toString());
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

    @Override
    protected EventTraceOuterClass.Qualifies launchQualifiesEvent(EventTraceOuterClass.Qualifies qualifies)
            throws MalformedBuiltinPredicateException, ReflectionPredicateException, PropertyViolatedException, NoViewEnabledException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchQualifiesEvent"), qualifies.toString());

        Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        PropResult res = check( qualifies.getCondition() );

        if (res.success) {
            for (EventTraceOuterClass.UIEvent uiEvent : qualifies.getTrace().getEventsList()) {
                executeEvent( uiEvent );
            }
        } else {
            Log.i(runner.chimpTag("EspressoChimpDriver@launchQualifiesEvent"),"Condition failed: " + qualifies.getCondition().toString());
        }

        return qualifies;
    }

    // User Events

    @Override
    protected AppEventOuterClass.Click launchClickEvent(AppEventOuterClass.Click click) throws NoViewEnabledException {

        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickEvent"), click.toString());
        ClickPerformer performer = new ClickPerformer(this, viewManager, wildCardManager, By.clickable(true), By.enabled(true), allOf(notSupportsInputMethods()));
        return performer.performAction(click);
    }

    @Override
    protected AppEventOuterClass.LongClick launchLongClickEvent(AppEventOuterClass.LongClick longClick) throws NoViewEnabledException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchLongClickEvent"), longClick.toString());
        LongClickPerformer performer = new LongClickPerformer(this, viewManager, wildCardManager, By.clickable(true), By.enabled(true), allOf(notSupportsInputMethods()));
        return performer.performAction(longClick);
    }

    @Override
    protected AppEventOuterClass.Type launchTypeEvent(AppEventOuterClass.Type type) throws NoViewEnabledException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchTypeEvent"), type.toString());
        TypePerformer performer = new TypePerformer(this, viewManager, wildCardManager, By.clickable(true), By.enabled(true), allOf(supportsInputMethods()));
        return performer.performAction(type);
    }

    @Override
    protected AppEventOuterClass.Pinch launchPinchEvent(AppEventOuterClass.Pinch pinch) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchPinchEvent"), pinch.toString());
        // TODO
        return pinch;
    }

    protected AppEventOuterClass.Swipe launchSwipeEvent(AppEventOuterClass.Swipe swipe) {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchSwipeEvent"), swipe.toString());
        Espresso.onView(isRoot()).perform(closeSoftKeyboard());
        SwipePerformer performer = new SwipePerformer(this, viewManager, wildCardManager, By.enabled(true), By.enabled(true), null);
        AppEventOuterClass.Swipe result = swipe;
        try {
             result = performer.performAction(swipe);
        } catch(Exception e){
            e.printStackTrace();
        }
        return result;
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

        Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        Espresso.onView(
                allOf(withContentDescription("More options"),
                        validPosition()))
                .perform(click());

    }

    @Override
    protected void launchClickHome() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickHome"), "ClickHome");

        // Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.pressHome();

        // Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_HOME));
        // kickBackExperiment();
    }

    @Override
    protected void launchClickBack() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "ClickBack");
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        mDevice.pressBack();


    }

    @Override
    protected void launchResume() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchReturnToApp"), "Resume");

        String packageName = runner.getAppPackageName();
        int launchTimeout = 5000;


        sleep(500);

        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(packageName);
        context.startActivity(intent);

        mDevice.wait(Until.hasObject(By.pkg(packageName).depth(0)), launchTimeout);

    }

    protected void sleep(int millisec) {
        try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            Log.e(runner.chimpTag("Sleep"), "Interrupted Exception caught while sleeping.",e);
        }
    }

    @Override
    protected void launchPullDownSettings() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchPullDownSettings"), "PullDownSettings");
        // TODO
    }


    @Override
    protected void launchRotate() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchRotate"), "Rotate");

        Activity activity = getActivityInstance();
        int orientation = activity.getApplicationContext().getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            Espresso.onView(isRoot()).perform(OrientationChangeAction.orientationLandscape());
        } else {
            Espresso.onView(isRoot()).perform(OrientationChangeAction.orientationPortrait());
        }


    }

    // Handling Properties
    @Override
    protected EventTraceOuterClass.Assert launchAssertEvent(EventTraceOuterClass.Assert assertProp)
                      throws MalformedBuiltinPredicateException, ReflectionPredicateException, PropertyViolatedException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchAssertEvent"), assertProp.getProps().toString());

        Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        PropResult res = check( assertProp.getProps() );
        if (res.success) {
            return assertProp;
        } else {
            throw new PropertyViolatedException("Assertion failed",res.violatedProp);
        }

    }

}
