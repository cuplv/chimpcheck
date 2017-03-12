package com.peilunzhang.contractiontimerdistilled;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith (AndroidJUnit4.class)
public class TraceCrash {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    private Solo solo;

    @Before
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        solo = new Solo(InstrumentationRegistry.getInstrumentation(),
                activityTestRule.getActivity());
    }


    @After
    public void tearDown() throws Exception {
        //tearDown() is run after a test case has finished.
        //finishOpenedActivities() will finish all the activities that have been opened during the test execution.

        solo.sleep(3000);

        solo.finishOpenedActivities();

        solo.sleep(3000);

    }

    @Test
    public void testButton() throws Exception {

        solo.clickOnView( solo.getView( R.id.fragmentBtn1) );
        solo.sleep(5000);
        solo.clickOnView( solo.getView( R.id.fragmentBtn2) );
        solo.sleep(7000);

    }

}
