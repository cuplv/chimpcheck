package edu.colorado.plv.chimp.viewmatchers;

import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created by Pezh on 9/12/17.
 */

public class AmbiguousCounter extends TypeSafeMatcher<View> {

    protected Matcher<View> matcher;
    private static int counter;

    public static void resetCounter(){
        counter = 0;
    }
    public static int getCounter(){
        return counter;
    }
    public AmbiguousCounter(Matcher<View> matcher) {
        this.matcher = matcher;
        this.counter = 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Click on the " + Integer.toString(counter+1) + " view");
        matcher.describeTo(description);
    }

    @Override
    public boolean matchesSafely(View view) {
        if(matcher.matches(view)){
            ++counter;
            return true;
        } else {
            return false;
        }
    }

}
