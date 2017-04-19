package edu.colorado.plv.chimp.viewactions;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.util.Log;
import android.view.View;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by edmund on 4/1/17.
 */
public class ChimpStagingAction implements ViewAction {

    public ChimpStagingAction() { }

    @Override
    public Matcher<View> getConstraints() {
        return isRoot();
    }

    @Override
    public String getDescription() {
        return "chimp waits for main thread to end";
    }

    @Override
    public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        Log.i("ChimpDriver-Staging","Main thread ended. Ready to go!");

    }
}
