package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;


import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;
import edu.colorado.plv.chimp.components.ActivityManager;
import edu.colorado.plv.chimp.components.ViewID;
import edu.colorado.plv.chimp.exceptions.MalformedBuiltinPredicateException;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import edu.colorado.plv.chimp.exceptions.PropertyViolatedException;
import edu.colorado.plv.chimp.exceptions.ReflectionPredicateException;


import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static edu.colorado.plv.chimp.components.FingerGestures.swipeOnCoord;
import static edu.colorado.plv.chimp.components.FingerGestures.swipeOnView;
import static edu.colorado.plv.chimp.components.ViewID.ViewIDType.RID;
import static edu.colorado.plv.chimp.components.ViewID.validOptionsMenu;
import static org.hamcrest.Matchers.allOf;

import edu.colorado.plv.chimp.performers.ClickPerformer;
import edu.colorado.plv.chimp.performers.LongClickPerformer;
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;
import edu.colorado.plv.chimp.viewactions.ChimpStagingAction;
import edu.colorado.plv.chimp.viewactions.OrientationChangeAction;

import java.util.ArrayList;

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
        ClickPerformer performer = new ClickPerformer(this, viewManager, wildCardManager, new UiSelector().clickable(true), new UiSelector().enabled(true));
        return performer.performAction(click);
    }

    @Override
    protected AppEventOuterClass.LongClick launchLongClickEvent(AppEventOuterClass.LongClick longClick) throws NoViewEnabledException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchLongClickEvent"), longClick.toString());
        LongClickPerformer performer = new LongClickPerformer(this, viewManager, wildCardManager, new UiSelector().longClickable(true), new UiSelector().enabled(true));
        return performer.performAction(longClick);
    }

    @Override
    protected AppEventOuterClass.Type launchTypeEvent(AppEventOuterClass.Type type) throws NoViewEnabledException {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchTypeEvent"), type.toString());
        AppEventOuterClass.UIID uiid = type.getUiid();
        String text = type.getInput();
        switch (uiid.getIdType()) {
            case R_ID:
                Espresso.onView(withId(uiid.getRid()))
                        .perform(clearText())
                        .perform(typeText(text)).perform(closeSoftKeyboard());
                return type;
            case NAME_ID:
                Espresso.onView(withText(uiid.getNameid()))
                        .perform(clearText())
                        .perform(typeText(text));
                Espresso.onView(isRoot())
                        .perform(closeSoftKeyboard());
                return type;
            case WILD_CARD:

                /*
                Espresso.onView(isRoot()).perform( new ChimpStagingAction() );
                ViewID vid = pickOne(getTypeableViewIDs(), "No available typeable views");
                try{
                    Espresso.onView(vid.matcher()).perform(typeText(text)).perform(closeSoftKeyboard());
                } catch(NoMatchingViewException nmve){
                    launchClickBack();
                }

                AppEventOuterClass.Type.Builder builder = AppEventOuterClass.Type.newBuilder();
                switch(vid.type()) {
                    case RID:
                        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.R_ID).setRid(vid.getID())).setInput(text);
                        break;
                    case DISPLAY_TEXT:
                        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID).setNameid(vid.getText())).setInput(text); break;
                    case CONTENT_DESC:
                        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID).setNameid(vid.getDesc())).setInput(text); break;
                }

                return builder.build();
                */

                try {
                    ArrayList<UiObject> editableObjects = new ArrayList<>();

                    ArrayList<UiObject> editTextObjects = wildCardManager.retrieveUiObjects(new UiSelector().className("android.widget.EditText")
                                                                                           ,new UiSelector().enabled(true));
                    editableObjects.addAll( editTextObjects );
                    // ArrayList<UiObject> imageViewObjects = wildCardManager.retrieveUiObjects(new UiSelector().className("android.widget..ImageView").enabled(true)
                    //                                                                         ,new UiSelector().enabled(true));


                    // TODO find other Android classes with editable text fields

                    while(editableObjects.size() > 0) {
                        UiObject uiObject = wildCardManager.popOne(editableObjects);
                        boolean succ = false;
                        String display = "";
                        try {
                            display = wildCardManager.getUiObjectDisplay(uiObject);
                            uiObject.setText(text);
                            succ = true;
                        } catch (UiObjectNotFoundException e) {
                            Log.e(runner.chimpTag("EspressoChimpDriver@launchTypeEvent"), "Failed typing on UIObject: " + wildCardManager.uiObjectInfo(uiObject), e);
                        }
                        if (succ) {
                            AppEventOuterClass.Type.Builder builder = AppEventOuterClass.Type.newBuilder();
                            builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID).setNameid(display))
                                   .setInput(text);
                            return builder.build();
                        }
                    }

                } catch (UiObjectNotFoundException e) {
                    Log.e(runner.chimpTag("EspressoChimpDriver@launchLongClickEvent"), "Error occurred at wild card top-level", e);
                }

                Log.e(runner.chimpTag("EspressoChimpDriver@launchLongClickEvent"), "Exhausted all wild card options. Throwing exception.");
                throw new NoViewEnabledException("Exhausted all wild card options.");


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
                swipeOnView(uiid, Espresso.onView( withId(uiid.getRid()) ), swipe.getPos());
                return swipe;
            case NAME_ID:
                swipeOnView(uiid, Espresso.onView( withText(uiid.getNameid()) ), swipe.getPos()); // Display name type
                return swipe;
            case XY_ID:
                AppEventOuterClass.XYCoordin xy = uiid.getXyid(); // XY coordinate type
                swipeOnCoord(Espresso.onView(isRoot()), xy, swipe.getPos());
            case WILD_CARD: // Wild card type
                ViewInteraction vi;
                try {
                    View view = getSwipeableView();
                    if(view.getId() != -1) {
                        if(getResName(view).equals("statusBarBackground") ){
                            return swipe;
                        }
                        vi = Espresso.onView(withId(view.getId()));
                    } else if(view.getContentDescription() != null) {
                        vi = Espresso.onView(withContentDescription(view.getContentDescription().toString()));
                    }else{
                        throw new NoViewEnabledException("can't find a view to swipe");
                    }
                    swipeOnView(uiid, vi, swipe.getPos());

                } catch(NoViewEnabledException nvee){
                    nvee.printStackTrace();
                }

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

        Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressMenu();

        // onView(validOptionsMenu("More options")).perform(click());
        // kickBackExperiment();
    }

    @Override
    protected void launchClickHome() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickHome"), "ClickHome");

        // Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.pressHome();

        // Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_HOME));
        // kickBackExperiment();
    }

    @Override
    protected void launchClickBack() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "ClickBack");
        // Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_BACK));

        // Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        try {
            Espresso.onView(isRoot()).perform(pressKey(KeyEvent.KEYCODE_BACK));
        } catch(NoActivityResumedException e){
            /*
            try{
                Thread.sleep(100000);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
            }
            e.printStackTrace();
            */
            Log.i(runner.chimpTag("EspressoChimpDriver@launchClickBack"), "App Exited. ChimpDriver Remaining calm.");
        }

    }

    @Override
    protected void launchResume() {
        Log.i(runner.chimpTag("EspressoChimpDriver@launchReturnToApp"), "Resume");

        String packageName = runner.getAppPackageName(); // "com.ianhanniballake.contractiontimer";
        int launchTimeout = 5000;

        // Espresso.onView(isRoot()).perform( new ChimpStagingAction() );

        sleep(500);

        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
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

        /*
        activity.setRequestedOrientation(
                (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        */

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
