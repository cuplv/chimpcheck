package edu.colorado.plv.chimp.components;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.*;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import android.view.View;
import edu.colorado.plv.chimp.exceptions.NoViewEnabledException;
import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Random;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by edmund on 3/21/17.
 */
public class ActivityManager {

    protected Activity current;
    protected Random seed = new Random();

    protected Activity getActivityInstance(){
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                for(Activity act : resumedActivity){
                    // Log.i("Chimp-needs-to-know","Got activity : " + act.toString());
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
            // Log.i("Chimp-needs-to-know","Active View: " + v.toString());
            views.add(v);
        }

        /*
        View focus = getActivityInstance().getCurrentFocus();
        if (focus != null) {
            for (View v : TreeIterables.breadthFirstViewTraversal(focus)) {
                Log.i("Chimp-needs-to-know", "In focus View: " + v.toString());
            }
        } */

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
        // return views.get(ThreadLocalRandom.current().nextInt(0, views.size()));
        return views.get(seed.nextInt(views.size())) ;
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

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }


    // Dialog box stuff

    protected ArrayList<ViewID> getClickableViewIDs() throws NoViewEnabledException {
        ArrayList<ViewID> ids = new ArrayList();
        if (hasDialogInFocus()) {
            // A Dialog box is determined to be in focus. Randomly pick between known dialog default buttons.
            // TODO: Does not work for custom dialog views. Will need to investigate how to obtain dialog box view hierarchy.
            Log.i("Chimp@getViews", "Returning default clickable dialog views");

            int[] dids = { android.R.id.button1, android.R.id.button2, android.R.id.button3 };
            for(int did: dids) {
                if (isMatch(did, allOf(isClickable(), isDisplayed()))) {
                   Log.i("Chimp@getViews", "Clickable view: " + did);
                   ids.add( ViewID.mkRID(did) );
                }
            }
        } else {
            // Default case: Revert to the standard view hierarchy
            Log.i("Chimp@getViews", "Using view hierarchy to obtain clickable views");
            for(View v: getAllViews( allOf(isClickable(), isEnabled(), isDisplayed()))) {
                if (v.getId() != -1) {
                    Log.i("Chimp@getViews", "Clickable view with RID: " + v.toString());
                    ids.add(ViewID.mkRID(v.getId()));
                } else {
                    Log.i("Chimp@getViews", "Clickable view with no RID (revert to content desc): " + v.toString());
                    ids.add(ViewID.mkDesc(v.getContentDescription().toString()));
                }
            }
        }
        return ids;
    }

    protected <A> A pickOne(ArrayList<A> arr, String msg) throws NoViewEnabledException {
        if (arr.size() == 0) throw new NoViewEnabledException(msg);
        return arr.get(seed.nextInt(arr.size())) ;
    }

    protected boolean hasDialogInFocus() {
        int titleId = getActivityInstance().getResources()
                .getIdentifier("alertTitle", "id", "android");
        try {
            onView(withId(titleId)).inRoot(isDialog()).check(matches(isDisplayed()));
            Log.i("Chimp@hasDialogInFocus", "A dialog box is determined to be in focus");
            return true;
        } catch (NoMatchingRootException e) {
            Log.i("Chimp@hasDialogInFocus", "No dialog box in focus: No matching root");
            return false;
        } catch (NoMatchingViewException e) {
            Log.i("Chimp@hasDialogInFocus", "No dialog box in focus: No matching view");
            return false;
        } catch (AssertionError e) {
            Log.i("Chimp@hasDialogInFocus", "No dialog box in focus: Found but not displayed.");
            return false;
        }
    }

    protected static boolean isMatch(int viewId, Matcher<View> mt) {
        try {
            onView( withId(viewId) ).check(matches(mt));
            Log.i("Chimp@isMatch",viewId + " matched.");
            return true;
        } catch (NoMatchingViewException e) {
            Log.i("Chimp@isMatch", viewId + "did not match: NoMatchingViewException" );
            return false;
        } catch (AssertionError e) {
            Log.i("Chimp@isMatch", viewId + "did not match: AssertionError" );
            return false;
        }
    }

}
