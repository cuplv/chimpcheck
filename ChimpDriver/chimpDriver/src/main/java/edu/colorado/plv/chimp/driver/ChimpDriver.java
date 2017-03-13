package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.util.Base64;
import android.util.Log;
import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;
import chimp.protobuf.ExtEventOuterClass;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by edmund on 3/10/17.
 */
public class ChimpDriver<A extends Activity> {

    protected EventTraceOuterClass.EventTrace trace = null;
    protected ChimpJUnitRunner runner = null;
    protected ActivityTestRule<A> chimpActivityTestRule = null;

    protected void setEventTrace(EventTraceOuterClass.EventTrace trace) { this.trace = trace; }
    protected void setRunner() { runner = (ChimpJUnitRunner) InstrumentationRegistry.getInstrumentation(); }
    protected void setActivityTestRule(Class<A> activityClass) {
        chimpActivityTestRule = new ActivityTestRule<A>(activityClass);
    }

    protected boolean isReady() { return trace != null && runner != null; }

    protected void init() { }

    protected void runTrace() {
        if (runner == null) setRunner();
        if (runner != null && trace == null) setEventTrace(runner.getEventTrace());
        if (!isReady()) {
            Log.e(runner.chimpTag("@runTrace"), "Chimp driver not ready.");
            return;
        }
        for(EventTraceOuterClass.UIEvent event :trace.getEventsList()) {
            executeEvent(event);
        }

        runner.addReport("Ran-Trace", Base64.encodeToString(trace.toByteArray(), Base64.DEFAULT));
    }

    // Execute Event methods. Implemented in a very sad way, because of how ProtoBufs handle union type.
    // Better ideas are welcomed...

    protected void executeEvent(EventTraceOuterClass.UIEvent event) {
        switch (event.getEventType()) {
            case APPEVENT: executeEvent(event.getAppEvent()); break;
            case EXTEVENT: executeEvent(event.getExtEvent()); break;
            case TRYEVENT: executeEvent(event.getTryEvent()); break;
            case DECIDE: executeEvent(event.getDecide()); break;
            case DECIDEMANY: executeEvent(event.getDecideMany()); break;
        }
    }

    protected void executeEvent(AppEventOuterClass.AppEvent appevent) {
        switch (appevent.getEventType()) {
            case CLICK: executeEvent(appevent.getClick()); break;
            case LONGCLICK: executeEvent( appevent.getLongclick() ); break;
            case TYPE: executeEvent( appevent.getType() ); break;
            case DRAG: executeEvent( appevent.getDrag() ); break;
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
            case RETURNTOAPP: executeEvent( ExtEventOuterClass.ReturnToApp.getDefaultInstance() ); break;
            case ROTATELEFT:  executeEvent( ExtEventOuterClass.RotateLeft.getDefaultInstance() ); break;
            case ROTATERIGHT: executeEvent( ExtEventOuterClass.RotateRight.getDefaultInstance() ); break;
        }
    }

    // Try Event Block

    protected void executeEvent(EventTraceOuterClass.TryEvent tryEvent) {
        Log.i(runner.chimpTag("@executeEvent"), tryEvent.toString());
        // TODO
    }

    protected void executeEvent(EventTraceOuterClass.Decide decide) {
        Log.i(runner.chimpTag("@executeEvent"), decide.toString());
        // TODO
    }

    protected void executeEvent(EventTraceOuterClass.DecideMany decideMany) {
        Log.i(runner.chimpTag("@executeEvent"), decideMany.toString());
        // TODO
    }

    // User Events

    protected void testClick(int id) {
        onView(withId(id)).perform(click());
    }

    protected void executeEvent(AppEventOuterClass.Click click) {
       Log.i(runner.chimpTag("@executeEvent"), click.toString());
       // TODO
    }

    protected void executeEvent(AppEventOuterClass.LongClick longClick) {
        Log.i(runner.chimpTag("@executeEvent"), longClick.toString());
        // TODO
    }

    protected void executeEvent(AppEventOuterClass.Type type) {
        Log.i(runner.chimpTag("@executeEvent"), type.toString());
        // TODO
    }

    protected void executeEvent(AppEventOuterClass.Drag drag) {
        Log.i(runner.chimpTag("@executeEvent"), drag.toString());
        // TODO
    }

    protected void executeEvent(AppEventOuterClass.Pinch pinch) {
        Log.i(runner.chimpTag("@executeEvent"), pinch.toString());
        // TODO
    }

    protected void executeEvent(AppEventOuterClass.Swipe swipe) {
        Log.i(runner.chimpTag("@executeEvent"), swipe.toString());
        // TODO
    }

    protected void executeEvent(AppEventOuterClass.Sleep sleep) {
        Log.i(runner.chimpTag("@executeEvent"), sleep.toString());
        // TODO
    }


    // External Events

    protected void executeEvent(ExtEventOuterClass.ClickBack clickBack) {
        Log.i(runner.chimpTag("@executeEvent"), clickBack.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.ClickHome clickHome) {
        Log.i(runner.chimpTag("@executeEvent"), clickHome.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.ClickMenu clickMenu) {
        Log.i(runner.chimpTag("@executeEvent"), clickMenu.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.PullDownSettings pullDownSettings) {
        Log.i(runner.chimpTag("@executeEvent"), pullDownSettings.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.ReturnToApp returnToApp) {
        Log.i(runner.chimpTag("@executeEvent"), returnToApp.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.RotateLeft rotateLeft) {
        Log.i(runner.chimpTag("@executeEvent"), rotateLeft.toString());
        // TODO
    }

    protected void executeEvent(ExtEventOuterClass.RotateRight rotateRight) {
        Log.i(runner.chimpTag("@executeEvent"), rotateRight.toString());
        // TODO
    }

    public String test() { return "This chimp is wonky!"; }

}
