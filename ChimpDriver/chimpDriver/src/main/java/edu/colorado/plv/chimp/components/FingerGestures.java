package edu.colorado.plv.chimp.components;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.MotionEvent;
import android.view.View;

import org.hamcrest.Matcher;

import chimp.protobuf.AppEventOuterClass;

import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by Pezh on 3/28/17.
 */

public class FingerGestures {

    public static void swipeOnView(ViewInteraction vi, AppEventOuterClass.Orientation ori){
        switch(ori.getOrientType()){
            case UP: vi.perform(swipeUp()); break;
            case DOWN: vi.perform(swipeDown()); break;
            case LEFT: vi.perform(swipeLeft()); break;
            case RIGHT: vi.perform(swipeRight()); break;
            case XY_TYPE:
                break;
        }
    }
    public static void drag(AppEventOuterClass.XYCoordin fromXY, AppEventOuterClass.Orientation ori){
        float fromX = fromXY.getX();
        float fromY = fromXY.getY();

        float offset = 100;
        switch(ori.getOrientType()){
            case UP: drag(fromX, fromY, fromX, fromY - offset); break;
            case DOWN: drag(fromX, fromY, fromX, fromY + offset); break;
            case LEFT: drag(fromX, fromY, fromX - offset, fromY); break;
            case RIGHT: drag(fromX, fromY, fromX + offset, fromY); break;
            case XY_TYPE:
                AppEventOuterClass.XYCoordin ToXY = ori.getXy();
                float toX = ToXY.getX();
                float toY = ToXY.getY();
                drag(fromX, fromY, toX, toY);
        }

    }

    public static void drag(float fromX, float fromY, float toX, float toY){
        Instrumentation inst = InstrumentationRegistry.getInstrumentation();
        int stepCount = 10;
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
    }



}
