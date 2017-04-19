package edu.colorado.plv.chimp.components;

import android.view.View;
import org.hamcrest.Matcher;

import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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
        RID, DISPLAY_TEXT, CONTENT_DESC
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

}
