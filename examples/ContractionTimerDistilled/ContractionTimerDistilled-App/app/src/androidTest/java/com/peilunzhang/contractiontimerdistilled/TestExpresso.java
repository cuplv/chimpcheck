package com.peilunzhang.contractiontimerdistilled;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.EspressoException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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

import chimp.protobuf.EventTraceOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.driver.ChimpJUnitRunner;
import edu.colorado.plv.chimp.driver.EspressoChimpDriver;

/**
 * Created by edmund on 3/10/17.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestExpresso extends EspressoChimpDriver<MainActivity> {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    public boolean reflectTestPass() { return true; }

    public boolean reflectTestAlwaysFail() { return false; }

    public boolean moreThanThree(int i) { return i > 3; }

    /*
    @Test
    public void test1() {



        // ChimpJUnitRunner driver = (ChimpJUnitRunner)   InstrumentationRegistry.getInstrumentation();
        // EventTraceOuterClass.EventTrace trace = driver.getEventTrace();

        // testClick(R.id.fragmentBtn1);

        // setRunner();
        // setEventTrace(trace);

        runTrace();



        // onView(withId(R.id.fragmentBtn1)).perform(click());

        // onView(withId(R.id.fragmentBtn2)).perform(click());



    } */

}
