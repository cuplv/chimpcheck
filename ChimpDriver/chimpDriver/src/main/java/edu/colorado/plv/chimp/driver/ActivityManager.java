package edu.colorado.plv.chimp.driver;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


import static org.hamcrest.Matchers.allOf;

import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;

/**
 * Created by edmund on 3/21/17.
 */
public class ActivityManager {

    protected Activity current;

    protected Activity getActivityInstance(){
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                for(Activity act : resumedActivity){
                    current = act;
                }
            }
        });
        return current;
    }

    protected View getDecorView(){
        return getActivityInstance().getWindow().getDecorView();
    }


    protected ArrayList<View> getAllViews() {
        View root = getDecorView();
        ArrayList<View> views = new ArrayList<>();
        for (View v : TreeIterables.breadthFirstViewTraversal(root)) {
                views.add(v);
        }
        return views;
    }

    protected ArrayList<View> getAllViews(Matcher<View> matcher) {
        ArrayList<View> views = new ArrayList<>();
        for(View v: getAllViews()) {
            if (matcher.matches(v)) {
                views.add(v);
            }
        }
        return views;
    }

    protected View getRandomView(ArrayList<View> views, String msg) throws NoViewEnabledException {
        if (views.isEmpty()) throw new NoViewEnabledException(msg);
        return views.get(ThreadLocalRandom.current().nextInt(0, views.size()));
    }

    /*
    protected ArrayList<View> getAllClickableViews() {
        View root = getDecorView();
        ArrayList<View> clickableViews = new ArrayList<>();
        for (View v : TreeIterables.breadthFirstViewTraversal(root)) {
            if (v.isClickable()) {
                clickableViews.add(v);
            }
        }
        return clickableViews;
    }
    protected  View getClickableView() throws NoViewEnabledException {
        ArrayList<View> clickableViews = getAllClickableViews();
        if(clickableViews.isEmpty()) {
            // WHY PEILUN? WHY IllegalStateException ?!
            // throw new IllegalStateException("No clickable events at current state");
            throw new NoViewEnabledException("No clickable events at current state");
        } else {
            return clickableViews.get(ThreadLocalRandom.current().nextInt(0, clickableViews.size()));
        }
    } */


    protected  View getClickableView() throws NoViewEnabledException {
        return getRandomView( getAllViews( allOf(isClickable(), isEnabled(), isDisplayed()) ), "No clickable views at current state");
    }

    protected View getTypeableView() throws NoViewEnabledException {
        return getRandomView( getAllViews( allOf(supportsInputMethods(), isEnabled(), isDisplayed()) ), "No views that supports input methods at current state" );
    }

    protected String getResName(View v){
        return v.getResources().getResourceEntryName(v.getId());
    }



}
