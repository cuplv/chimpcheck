package edu.colorado.plv.chimp.components;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
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

    public static void swipeOnView(AppEventOuterClass.UIID uiid, ViewInteraction vi, AppEventOuterClass.Orientation ori){
        switch(ori.getOrientType()){
            case UP: vi.perform(swipeUp()); break;
            case DOWN: vi.perform(swipeDown()); break;
            case LEFT: vi.perform(swipeLeft()); break;
            case RIGHT: vi.perform(swipeRight()); break;
            case XY_TYPE:
                ActivityManager activityManager = new ActivityManager();
                View v = new View(activityManager.getActivityInstance().getApplicationContext());
                switch(uiid.getIdType()){
                    case R_ID:
                        v = activityManager.getDecorView().findViewById(uiid.getRid());
                        break;
                    case NAME_ID:
                        View decorView = activityManager.getDecorView();
                        int resId = decorView.getResources().getIdentifier(uiid.getNameid(), "id", activityManager.getActivityInstance().getPackageName() );
                        v = decorView.findViewById(resId);
                        break;
                }

                if(v == null) {
                    return;
                }

                float x = v.getX();
                float y = v.getY();
                float centerX = x + v.getWidth() / 2;
                float centerY = y + v.getHeight() / 2;
                AppEventOuterClass.XYCoordin desXY = ori.getXy();
                vi.perform(swipeOnCoord(centerX, centerY, desXY.getX(), desXY.getY()));
                
                break;
        }
    }
    public static void swipeOnCoord(ViewInteraction vi, AppEventOuterClass.XYCoordin fromXY, AppEventOuterClass.Orientation ori){
        float fromX = fromXY.getX();
        float fromY = fromXY.getY();

        float offset = 100;
        switch(ori.getOrientType()){
            case UP: vi.perform(swipeOnCoord(fromX, fromY, fromX, fromY - offset));
            case DOWN: vi.perform(swipeOnCoord(fromX, fromY, fromX, fromY + offset));
            case LEFT: vi.perform(swipeOnCoord(fromX, fromY, fromX - offset, fromY));
            case RIGHT: vi.perform(swipeOnCoord(fromX, fromY, fromX + offset, fromY));
            case XY_TYPE:
                AppEventOuterClass.XYCoordin ToXY = ori.getXy();
                float toX = ToXY.getX();
                float toY = ToXY.getY();
                vi.perform(swipeOnCoord(fromX, fromY, toX, toY));
            default:
                break;
        }

    }
    public static ViewAction swipeOnCoord(final float fromX, final float fromY, final float toX, final float toY) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {

                return "Swipe from" + fromX + ", " + fromY + " to " + toX + ", " + toY;
            }

            @Override
            public void perform(UiController uiController, final View view) {
                drag(fromX, fromY, toX, toY);
            }
        };
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

    public static void pinch(float fromX1, float fromY1, float toX1, float toY1, float fromX2, float fromY2, float toX2, float toY2){
        Instrumentation inst = InstrumentationRegistry.getInstrumentation();
        int stepCount = 10;
        long downTime = SystemClock.uptimeMillis();
        long eventTime= SystemClock.uptimeMillis();
        float y = fromY1;
        float x = fromX1;
        float yStep = (toY1 - fromY1) / stepCount;
        float xStep = (toX1 - fromX1) / stepCount;
        MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, fromX1, fromY1, 0);
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
        event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_UP, toX1, toY1, 0);
        try {
            inst.sendPointerSync(event);
        } catch (SecurityException ignored) {System.out.println("error 3");}
    }

}
