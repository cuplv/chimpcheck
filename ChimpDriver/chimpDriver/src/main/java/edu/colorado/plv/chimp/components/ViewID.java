package edu.colorado.plv.chimp.components;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/**
 * Created by edmund on 4/3/17.
 */
abstract public class ViewID {

    abstract public Matcher<View> matcher();

    abstract public ViewIDType type();

    public int getID() { return -1; }

    public String getText() { return "<No Text>"; }

    public String getDesc() { return "<No Description>"; }

    public static enum ViewIDType {
        RID, DISPLAY_TEXT, CONTENT_DESC, LIST_VIEW
    }

    public static ViewID mkRID(final int rid) {
      return new ViewID() {
          @Override
          public Matcher<View> matcher() { return withId(rid); }
          @Override
          public ViewIDType type() { return ViewIDType.RID; }
          @Override
          public String toString() { return String.format("View(RID:%s)", rid); }
          @Override
          public int getID() { return rid; }
      };
    }

    public static ViewID mkText(final String text) {
        return new ViewID() {
            @Override
            public Matcher<View> matcher() { return withText(text); }
            @Override
            public ViewIDType type() { return ViewIDType.DISPLAY_TEXT; }
            @Override
            public String toString() { return String.format("View(TEXT:%s)", text); }
            @Override
            public String getText() { return text; }
        };
    }

    public static ViewID mkDesc(final String description) {
        return new ViewID() {
            @Override
            public Matcher<View> matcher() { return withContentDescription(description); }
            @Override
            public ViewIDType type() { return ViewIDType.CONTENT_DESC; }
            @Override
            public String toString() { return String.format("View(CONTENT_DESC:%s)", description); }
            @Override
            public String getDesc() { return description; }
        };
    }

    public static ViewID mkList(final int rid, final int child, final String resid) {
        return new ViewID() {
            @Override
            public Matcher<View> matcher() { return ViewID.childAtPosition(withId(rid), child); }
            @Override
            public ViewIDType type() { return ViewIDType.LIST_VIEW; }
            @Override
            public String toString() { return String.format("View(child %d of %s)", child, resid); }
            @Override
            public int getID() { return rid; }
        };
    }
    public static ViewID mkList(final String rid, final int child) {
        return new ViewID() {
            @Override
            public Matcher<View> matcher() { return ViewID.childAtPosition(withContentDescription(rid), child); }
            @Override
            public ViewIDType type() { return ViewIDType.LIST_VIEW; }
            @Override
            public String toString() { return String.format("View(child %d of CONTENT_DESC: %s)", child, rid); }
            @Override
            public String getDesc() { return rid; }
        };
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
    public static Matcher<View> validOptionsMenu(String text){
        return validOptionsMenu(is(text));

    }
    public static Matcher<View> validOptionsMenu(
            final Matcher<? extends CharSequence> charSequenceMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with content description and XY: ");
                charSequenceMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return charSequenceMatcher.matches(view.getContentDescription()) && view.getX()!=0.0 && view.getY() !=0.0;
            }
        };
    }
}
