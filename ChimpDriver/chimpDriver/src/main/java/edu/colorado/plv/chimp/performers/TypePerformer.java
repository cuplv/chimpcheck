package edu.colorado.plv.chimp.performers;

import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.components.ActivityManager;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.managers.MatcherManager;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;
import edu.colorado.plv.chimp.viewactions.ChimpStagingAction;

import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static edu.colorado.plv.chimp.components.ActivityManager.getResIdFromResName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.describedAs;

/**
 * Created by Pezh on 8/24/17.
 */

public class TypePerformer extends Performer<AppEventOuterClass.Type> {
    public TypePerformer(ChimpDriver chimpDriver, ViewManager viewManager,
                          WildCardManager wildCardManager, BySelector wildCardSelector,
                          Matcher<View> userDefinedMatcher) {
        super("Type", chimpDriver, viewManager, wildCardManager, wildCardSelector, userDefinedMatcher);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.Type type) {
        return viewManager.retrieveTargets(type.getUiid());
    }

    public AppEventOuterClass.Type performMatcherAction(AppEventOuterClass.Type origin, Matcher<View> matcher)
            throws AmbiguousViewMatcherException, NoMatchingViewException {

        Espresso.onView(matcher).perform(clearText(), typeText(origin.getInput()), closeSoftKeyboard());
        return origin;
    }

    @Override
    public AppEventOuterClass.Type performXYAction(AppEventOuterClass.Type origin, int x, int y) {

        return origin;
    }




}

