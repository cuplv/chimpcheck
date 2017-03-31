package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.rule.ActivityTestRule;
import android.util.Base64;
import android.util.Log;

import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;
import chimp.protobuf.ExtEventOuterClass;
import chimp.protobuf.Property;
import edu.colorado.plv.chimp.components.PropertyActivityManager;
import edu.colorado.plv.chimp.exceptions.MalformedBuiltinPredicateException;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import edu.colorado.plv.chimp.exceptions.PropertyViolatedException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by edmund on 3/10/17.
 */
abstract public class ChimpDriver<A extends Activity> extends PropertyActivityManager {

    enum Outcome {
        UNKNOWN, SUCCESS, CRASHED, ASSERTFAILED, BLOCKED, DRIVEREXCEPT
    }

    protected EventTraceOuterClass.EventTrace trace = null;
    protected ChimpJUnitRunner runner = null;
    protected ActivityTestRule<A> chimpActivityTestRule = null;

    protected void setEventTrace(EventTraceOuterClass.EventTrace trace) { this.trace = trace; }
    protected void setRunner() { runner = (ChimpJUnitRunner) InstrumentationRegistry.getInstrumentation(); }
    protected void setActivityTestRule(Class<A> activityClass) {
        chimpActivityTestRule = new ActivityTestRule<A>(activityClass);
    }

    protected boolean traceCompleted = false;
    protected boolean noOp = false;

    protected Outcome outcome = Outcome.UNKNOWN;
    protected Property.Prop violatedProp = null;
    protected String exceptMsg = null;

    protected List<EventTraceOuterClass.UIEvent> completedEvents = null;

    protected boolean isReady() { return trace != null && runner != null; }

    @Before
    public void init() {
        setRunner();
        setEventTrace(runner.getEventTrace());
    }

    @Test
    public void runChimpTrace() {
        if (runner == null) setRunner();
        if (runner != null && trace == null) setEventTrace(runner.getEventTrace());
        if (!isReady()) {
            Log.e(runner.chimpTag("@runTrace"), "Chimp driver not ready.");
            return;
        }

        traceCompleted = false;
        noOp = false;
        outcome = Outcome.UNKNOWN;

        completedEvents = new ArrayList<EventTraceOuterClass.UIEvent>();

        for(EventTraceOuterClass.UIEvent event :trace.getEventsList()) {
            try {
                executeEvent(event);
            } catch (NoViewEnabledException e) {
                Log.e(runner.chimpTag("@runTrace"), "ChimpDriver view exception: " + e.toString());
                outcome = Outcome.BLOCKED;
                noOp = true;
                return;
            } catch (NoMatchingViewException e) {
                Log.e(runner.chimpTag("@runTrace"), "Espresso no matching view exception: " + e.toString());
                outcome = Outcome.BLOCKED;
                noOp = true;
                return;
            } catch (PropertyViolatedException e) {
                Log.e(runner.chimpTag("@runTrace"), "Property assertion failed: " + "\n" + e.toString());
                outcome = Outcome.ASSERTFAILED;
                violatedProp = e.getProp();
                return;
            } catch (MalformedBuiltinPredicateException e) {
                outcome = Outcome.DRIVEREXCEPT;
                exceptMsg = e.toString();
                return;
            } catch (Exception e) {
                // Trying to intercept all exceptions now.
                /* TODO: My hunch is that we cannot distinguish between App or Chimp Driver crash here.
                   TODO: Investigate more... */
                Log.e(runner.chimpTag("@runTrace"), "Either App or Chimp Driver crashed: " + e.toString());
                outcome = Outcome.CRASHED;
                // Catch and release, because we want the Test Driver to recognize the exception and react accordingly.
                throw e;
            }
        }

        Log.i(runner.chimpTag("@runTrace"), "Trace Completed!");
        traceCompleted = true;
        outcome = Outcome.SUCCESS;
    }

    @After
    public void processTraceReport() {
        Log.i(runner.chimpTag("@processTraceReport"), "Processing trace report...");

        // Append Executed Trace Report
        EventTraceOuterClass.EventTrace.Builder builder = EventTraceOuterClass.EventTrace.newBuilder();
        for(EventTraceOuterClass.UIEvent event: completedEvents) {
            builder.addEvents( event );
        }

        String base64Output = Base64.encodeToString(builder.build().toByteArray(), Base64.DEFAULT);
        runner.addReport("ChimpDriver-ExecutedTrace", base64Output);

        // Append Assert failure report if applicable.
        if (outcome == Outcome.ASSERTFAILED) {
            String base64Prop = Base64.encodeToString(violatedProp.toByteArray(), Base64.DEFAULT);
            runner.addReport("ChimpDriver-ViolatedProperty", base64Prop);
        }

        if (outcome == Outcome.DRIVEREXCEPT) {
            runner.addReport("ChimpDriver-Exception", exceptMsg);
        }

        // Append Trace Outcome Report
        String outcomeStr = "Unknown";
        switch (outcome) {
            case CRASHED: outcomeStr  = "Crashed"; break;
            case BLOCKED: outcomeStr  = "Blocked"; break;
            case ASSERTFAILED: outcomeStr = "AssertFailed"; break;
            case UNKNOWN: outcomeStr  = "Unknown"; break;
            case SUCCESS: outcomeStr  = "Success"; break;
            case DRIVEREXCEPT: outcomeStr = "DriverExcept"; break;
        }
        runner.addReport("ChimpDriver-Outcome", outcomeStr);

        Log.i(runner.chimpTag("@processTraceReport"), "Trace report completed!");
    }


    /* TO BE DEPRECATED SOON */
    /*
    @After
    public void compileTraceReport() {
        if (traceCompleted) {
            runner.addReport("ChimpTraceResult", "Success");
        } else {
            runner.addReport("ChimpTraceResult", "Failed");
        }
        if (noOp) {
            runner.addReport("ChimpTraceBlocked", "Yes");
        } else {
            runner.addReport("ChimpTraceBlocked", "No");
        }

        // runner.addReport("ChimpTraceCompleted", Base64.encodeToString(trace.toByteArray(), Base64.DEFAULT));

        EventTraceOuterClass.EventTrace.Builder builder = EventTraceOuterClass.EventTrace.newBuilder();
        for(EventTraceOuterClass.UIEvent event: completedEvents) {
            builder.addEvents( event );
        }

        String base64Output = Base64.encodeToString(builder.build().toByteArray(), Base64.DEFAULT);
        runner.addReport("ChimpTraceCompleted", base64Output);
    }*/

    // Abstract Launch event methods

    abstract protected EventTraceOuterClass.TryEvent launchTryEvent(EventTraceOuterClass.TryEvent tryevent)
            throws MalformedBuiltinPredicateException, PropertyViolatedException;
    abstract protected EventTraceOuterClass.Decide launchDecideEvent(EventTraceOuterClass.Decide decide);
    abstract protected EventTraceOuterClass.DecideMany launchDecideManyEvent(EventTraceOuterClass.DecideMany decideMany);

    abstract protected EventTraceOuterClass.Assert launchAssertEvent(EventTraceOuterClass.Assert assertProp)
            throws MalformedBuiltinPredicateException, PropertyViolatedException;

    abstract protected AppEventOuterClass.Click launchClickEvent(AppEventOuterClass.Click click) throws NoViewEnabledException;
    abstract protected AppEventOuterClass.LongClick launchLongClickEvent(AppEventOuterClass.LongClick longClick) throws NoViewEnabledException;
    abstract protected AppEventOuterClass.Type launchTypeEvent(AppEventOuterClass.Type type) throws NoViewEnabledException;
    abstract protected AppEventOuterClass.Pinch launchPinchEvent(AppEventOuterClass.Pinch pinch);
    abstract protected AppEventOuterClass.Swipe launchSwipeEvent(AppEventOuterClass.Swipe swipe);
    abstract protected AppEventOuterClass.Sleep launchSleepEvent(AppEventOuterClass.Sleep sleep);

    abstract protected void launchClickMenu();
    abstract protected void launchClickHome();
    abstract protected void launchClickBack();
    abstract protected void launchPullDownSettings();
    abstract protected void launchResume();
    abstract protected void launchRotate();

    // Execute Event methods. Implemented in a very sad way, because of how ProtoBufs handle union type.
    // Better ideas are welcomed...

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.Click click) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.CLICK)
                .setClick(click).build();
    }

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.LongClick longClick) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.LONGCLICK)
                .setLongclick(longClick).build();
    }

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.Sleep sleep) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.SLEEP)
                .setSleep(sleep).build();
    }

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.Type type) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.TYPE)
                .setType(type).build();
    }

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.Pinch pinch) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.PINCH)
                .setPinch(pinch).build();
    }

    protected AppEventOuterClass.AppEvent mkAppEvent(AppEventOuterClass.Swipe swipe) {
        return AppEventOuterClass.AppEvent.newBuilder().setEventType(AppEventOuterClass.AppEvent.AppEventType.SWIPE)
                .setSwipe(swipe).build();
    }

    protected EventTraceOuterClass.UIEvent mkUIEvent(AppEventOuterClass.AppEvent appevent) {
        return EventTraceOuterClass.UIEvent.newBuilder().setEventType(EventTraceOuterClass.UIEvent.UIEventType.APPEVENT)
                .setAppEvent( appevent ).build();
    }

    protected EventTraceOuterClass.UIEvent mkUIEvent(ExtEventOuterClass.ExtEvent extevent) {
        return EventTraceOuterClass.UIEvent.newBuilder().setEventType(EventTraceOuterClass.UIEvent.UIEventType.EXTEVENT)
                .setExtEvent( extevent ).build();
    }


    protected void executeEvent(EventTraceOuterClass.UIEvent event)
            throws NoViewEnabledException,MalformedBuiltinPredicateException,PropertyViolatedException {
        switch (event.getEventType()) {
            case APPEVENT: executeEvent(event.getAppEvent()); break;
            case EXTEVENT: executeEvent(event.getExtEvent()); break;
            case TRYEVENT: executeEvent(event.getTryEvent()); break;
            case DECIDE: executeEvent(event.getDecide()); break;
            case DECIDEMANY: executeEvent(event.getDecideMany()); break;
            case ASSERT: executeEvent(event.getAssert()); break;
        }
    }

    protected void executeEvent(AppEventOuterClass.AppEvent appevent) throws NoViewEnabledException {
        switch (appevent.getEventType()) {
            case CLICK: executeEvent(appevent.getClick()); break;
            case LONGCLICK: executeEvent( appevent.getLongclick() ); break;
            case TYPE: executeEvent( appevent.getType() ); break;
            case PINCH: executeEvent( appevent.getPinch() ); break;
            case SWIPE: executeEvent( appevent.getSwipe() ); break;
            case SLEEP: executeEvent( appevent.getSleep() ); break;
        }

    }

    protected void executeEvent(ExtEventOuterClass.ExtEvent extevent) {
        switch (extevent.getEventType()) {
            case CLICKBACK: executeEvent( ExtEventOuterClass.ClickBack.getDefaultInstance() ); break;
            case CLICKHOME: executeEvent( ExtEventOuterClass.ClickHome.getDefaultInstance() ); break;
            case CLICKMENU: executeEvent( ExtEventOuterClass.ClickMenu.getDefaultInstance() ); break;
            case PULLDOWNSETTINGS: executeEvent( ExtEventOuterClass.PullDownSettings.getDefaultInstance() ); break;
            case RESUME: executeEvent( ExtEventOuterClass.Resume.getDefaultInstance() ); break;
            case ROTATE:  executeEvent( ExtEventOuterClass.Rotate.getDefaultInstance() ); break;
        }
    }

    // Try Event Block

    protected void executeEvent(EventTraceOuterClass.TryEvent tryEvent)
                throws MalformedBuiltinPredicateException, PropertyViolatedException {
        Log.i(runner.chimpTag("@executeEvent"), tryEvent.toString());
        EventTraceOuterClass.TryEvent newTryEvent = launchTryEvent(tryEvent);

        // Skip logging try events (child events are already logged)
        /*
        completedEvents.add(
           EventTraceOuterClass.UIEvent.newBuilder().setEventType(EventTraceOuterClass.UIEvent.UIEventType.TRYEVENT)
                .setTryEvent(newTryEvent).build()
        ); */
    }

    protected void executeEvent(EventTraceOuterClass.Decide decide) {
        Log.i(runner.chimpTag("@executeEvent"), decide.toString());
        EventTraceOuterClass.Decide newDecide = launchDecideEvent(decide);
        completedEvents.add(
           EventTraceOuterClass.UIEvent.newBuilder().setEventType(EventTraceOuterClass.UIEvent.UIEventType.DECIDE)
                .setDecide(newDecide).build()
        );
    }

    protected void executeEvent(EventTraceOuterClass.DecideMany decideMany) {
        Log.i(runner.chimpTag("@executeEvent"), decideMany.toString());
        EventTraceOuterClass.DecideMany newDecideMany = launchDecideManyEvent(decideMany);
        completedEvents.add(
           EventTraceOuterClass.UIEvent.newBuilder().setEventType(EventTraceOuterClass.UIEvent.UIEventType.DECIDEMANY)
                .setDecideMany(newDecideMany).build()
        );
    }

    protected void executeEvent(EventTraceOuterClass.Assert assertProp)
            throws MalformedBuiltinPredicateException,PropertyViolatedException {
        Log.i(runner.chimpTag("@executeEvent"), assertProp.toString());
        launchAssertEvent(assertProp);
        // Currently throwing away the response: For now, don't log assert P's.
    }

    // User Events

    protected void executeEvent(AppEventOuterClass.Click click) throws NoViewEnabledException {
       Log.i(runner.chimpTag("@executeEvent"), click.toString());
       AppEventOuterClass.Click launchedClick = launchClickEvent(click);
       completedEvents.add( mkUIEvent(mkAppEvent(launchedClick)) );
    }

    protected void executeEvent(AppEventOuterClass.LongClick longClick) throws NoViewEnabledException {
        Log.i(runner.chimpTag("@executeEvent"), longClick.toString());
        AppEventOuterClass.LongClick launchedLongClick = launchLongClickEvent(longClick);
        completedEvents.add( mkUIEvent(mkAppEvent(launchedLongClick)) );
    }

    protected void executeEvent(AppEventOuterClass.Type type) throws NoViewEnabledException {
        Log.i(runner.chimpTag("@executeEvent"), type.toString());
        AppEventOuterClass.Type launchedType = launchTypeEvent(type);
        completedEvents.add( mkUIEvent(mkAppEvent(launchedType)) );
    }

    protected void executeEvent(AppEventOuterClass.Pinch pinch) {
        Log.i(runner.chimpTag("@executeEvent"), pinch.toString());
        AppEventOuterClass.Pinch launchedPinch = launchPinchEvent(pinch);
        completedEvents.add( mkUIEvent(mkAppEvent(launchedPinch)) );
    }

    protected void executeEvent(AppEventOuterClass.Swipe swipe) {
        Log.i(runner.chimpTag("@executeEvent"), swipe.toString());
        AppEventOuterClass.Swipe launchedSwipe = launchSwipeEvent(swipe);
        completedEvents.add( mkUIEvent(mkAppEvent(launchedSwipe)) );
    }

    protected void executeEvent(AppEventOuterClass.Sleep sleep) {
        Log.i(runner.chimpTag("@executeEvent"), sleep.toString());
        AppEventOuterClass.Sleep launchedSleep = launchSleepEvent(sleep);
        completedEvents.add( mkUIEvent(mkAppEvent(launchedSleep)) );
    }


    // External Events

    protected void executeEvent(ExtEventOuterClass.ClickBack clickBack) {
        Log.i(runner.chimpTag("@executeEvent"), clickBack.toString());
        launchClickBack();
        completedEvents.add(
                mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.CLICKBACK).build())
        );
    }

    protected void executeEvent(ExtEventOuterClass.ClickHome clickHome) {
        Log.i(runner.chimpTag("@executeEvent"), clickHome.toString());
        launchClickHome();
        completedEvents.add(
                mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.CLICKHOME).build())
        );
    }

    protected void executeEvent(ExtEventOuterClass.ClickMenu clickMenu) {
        Log.i(runner.chimpTag("@executeEvent"), clickMenu.toString());
        launchClickMenu();
        completedEvents.add(
                mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.CLICKMENU).build())
        );
    }

    protected void executeEvent(ExtEventOuterClass.PullDownSettings pullDownSettings) {
        Log.i(runner.chimpTag("@executeEvent"), pullDownSettings.toString());
        launchPullDownSettings();
        completedEvents.add(
                mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.PULLDOWNSETTINGS).build())
        );
    }

    protected void executeEvent(ExtEventOuterClass.Resume resume) {
        Log.i(runner.chimpTag("@executeEvent"), resume.toString());
        launchResume();
        completedEvents.add(
                mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.RESUME).build())
        );
    }

    protected void executeEvent(ExtEventOuterClass.Rotate rotate) {
        Log.i(runner.chimpTag("@executeEvent"), rotate.toString());
        launchRotate();
        completedEvents.add(
           mkUIEvent(ExtEventOuterClass.ExtEvent.newBuilder().setEventType(ExtEventOuterClass.ExtEvent.ExtEventType.ROTATE).build())
        );
    }

}
