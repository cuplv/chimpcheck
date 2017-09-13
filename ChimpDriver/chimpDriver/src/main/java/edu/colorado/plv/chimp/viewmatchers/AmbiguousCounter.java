package edu.colorado.plv.chimp.viewmatchers;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by Pezh on 9/12/17.
 */

public class AmbiguousCounter extends TypeSafeMatcher<View> {

    protected Matcher<View> parentMatcher;
    protected static int counter;

    public static void resetCounter(){
        counter = 0;
    }
    public static int getCounter(){
        return counter;
    }
    public AmbiguousCounter(Matcher<View> parentMatcher) {
        this.parentMatcher = parentMatcher;
        this.counter = 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Click on the " + Integer.toString(counter+1) + " view");
        parentMatcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
        if(parentMatcher.matches(view)){
            counter++;
            return true;
        } else {
            return false;
        }
    }

}
