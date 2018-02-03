package edu.colorado.plv.chimp.performers;

import android.support.test.espresso.Espresso;
import android.support.test.uiautomator.BySelector;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.managers.MatcherManager;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;

import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by Pezh on 8/24/17.
 */

public class TypePerformer extends Performer<AppEventOuterClass.Type> {
    public TypePerformer(ChimpDriver chimpDriver, ViewManager viewManager,
                          WildCardManager wildCardManager, BySelector wildCardSelector, BySelector wildCardChildSelector,
                          Matcher<View> userDefinedMatcher) {
        super("Type", chimpDriver, viewManager, wildCardManager, wildCardSelector, wildCardChildSelector,userDefinedMatcher);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.Type type) {
        return viewManager.retrieveTargets(type.getUiid());
    }

    @Override
    public AppEventOuterClass.Type performMatcherAction(AppEventOuterClass.Type origin, Matcher<View> matcher) {
        Espresso.onView(matcher).perform(typeText(origin.getInput()));
        Espresso.onView(isRoot()).perform(closeSoftKeyboard());
        return origin;
    }
    @Override
    public AppEventOuterClass.Type performWildCardTargetAction(AppEventOuterClass.Type origin, WildCardTarget target) {
        Espresso.onView(target.uiMatcher).perform(replaceText(origin.getInput()));
        Espresso.onView(isRoot()).perform(closeSoftKeyboard());

        AppEventOuterClass.Type.Builder builder = AppEventOuterClass.Type.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID)
                .setNameid(MatcherManager.describeMatcherAsDisplay(target.uiObj))).setInput(origin.getInput());
        return builder.build();
    }

    @Override
    public AppEventOuterClass.Type performXYAction(AppEventOuterClass.Type origin, int x, int y) {

        return origin;
    }




}

