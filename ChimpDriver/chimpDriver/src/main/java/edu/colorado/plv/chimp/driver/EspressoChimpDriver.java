package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.util.Log;
import chimp.protobuf.AppEventOuterClass;
import chimp.protobuf.EventTraceOuterClass;
import chimp.protobuf.ExtEventOuterClass;

/**
 * Created by edmund on 3/13/17.
 */
public class EspressoChimpDriver<A extends Activity> extends ChimpDriver<A> {

    @Override
    protected void init() {  }

    // Try Event Block

    @Override
    protected void executeEvent(EventTraceOuterClass.TryEvent tryEvent) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), tryEvent.toString());
        // TODO
    }

    @Override
    protected void executeEvent(EventTraceOuterClass.Decide decide) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), decide.toString());
        // TODO
    }

    @Override
    protected void executeEvent(EventTraceOuterClass.DecideMany decideMany) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), decideMany.toString());
        // TODO
    }

    // User Events

    @Override
    protected void executeEvent(AppEventOuterClass.Click click) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), click.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.LongClick longClick) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), longClick.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.Type type) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), type.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.Drag drag) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), drag.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.Pinch pinch) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), pinch.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.Swipe swipe) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), swipe.toString());
        // TODO
    }

    @Override
    protected void executeEvent(AppEventOuterClass.Sleep sleep) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), sleep.toString());
        // TODO
    }


    // External Events

    @Override
    protected void executeEvent(ExtEventOuterClass.ClickBack clickBack) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), clickBack.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.ClickHome clickHome) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), clickHome.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.ClickMenu clickMenu) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), clickMenu.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.PullDownSettings pullDownSettings) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), pullDownSettings.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.ReturnToApp returnToApp) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), returnToApp.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.RotateLeft rotateLeft) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), rotateLeft.toString());
        // TODO
    }

    @Override
    protected void executeEvent(ExtEventOuterClass.RotateRight rotateRight) {
        Log.i(runner.chimpTag("EspressoChimpDriver@executeEvent"), rotateRight.toString());
        // TODO
    }

}
