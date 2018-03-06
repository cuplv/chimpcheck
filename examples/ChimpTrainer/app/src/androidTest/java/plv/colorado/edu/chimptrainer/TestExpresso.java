package plv.colorado.edu.chimptrainer;

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
public class TestExpresso extends EspressoChimpDriver {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);
}
