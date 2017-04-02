package com.peilunzhang.contractiontimerdistilled;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by edmund on 4/1/17.
 */

@RunWith(AndroidJUnit4.class)
public class TestUISync {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void test1() {



        // ChimpJUnitRunner driver = (ChimpJUnitRunner)   InstrumentationRegistry.getInstrumentation();
        // EventTraceOuterClass.EventTrace trace = driver.getEventTrace();

        // testClick(R.id.fragmentBtn1);

        // setRunner();
        // setEventTrace(trace);

        Log.i("test-sync", String.format("This is the first count: %s", activityTestRule.getActivity().getCount()) );

        for (int i=0; i < 100; i++) {
            onView(withId(R.id.fragmentBtn2)).perform(click());
            Log.i("test-sync", String.format("This is the %s count: %s", i, activityTestRule.getActivity().getCount()) );
        }

        onView(withId(R.id.fragmentBtn1)).perform(click());

        Log.i("test-sync", String.format("This is the last count: %s", activityTestRule.getActivity().getCount()) );




    }

}
