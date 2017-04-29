package plv.colorado.edu.chimpsample;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.runner.RunWith;

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
