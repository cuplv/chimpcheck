package edu.colorado.plv.chimp.performers;

import android.support.test.uiautomator.UiObject2;
import android.view.View;

import org.hamcrest.Matcher;

/**
 * Created by Pezh on 8/31/17.
 */

public class WildCardTarget {
    public Matcher<View> uiMatcher;
    public UiObject2 uiObj;

    public WildCardTarget(Matcher<View> uiMatcher, UiObject2 uiObj) {
        this.uiMatcher = uiMatcher;
        this.uiObj = uiObj;
    }
}
