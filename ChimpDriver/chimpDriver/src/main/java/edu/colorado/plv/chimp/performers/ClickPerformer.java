package edu.colorado.plv.chimp.performers;

import android.graphics.Rect;
import android.support.test.espresso.Espresso;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;

import chimp.protobuf.AppEventOuterClass;
import edu.colorado.plv.chimp.driver.ChimpDriver;
import edu.colorado.plv.chimp.managers.ViewManager;
import edu.colorado.plv.chimp.managers.WildCardManager;
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by edmundlam on 5/19/17.
 */

public class ClickPerformer extends Performer<AppEventOuterClass.Click> {

    public ClickPerformer(ChimpDriver chimpDriver, ViewManager viewManager, WildCardManager wildCardManager, UiSelector wildCardTopSelector, UiSelector wildCardChildSelector) {
        super("Click", chimpDriver, viewManager, wildCardManager, wildCardTopSelector, wildCardChildSelector);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.Click click) {
        return viewManager.retrieveTargets(click.getUiid());
    }

    @Override
    public AppEventOuterClass.Click performMatcherAction(AppEventOuterClass.Click origin, Matcher<View> matcher) {
        Espresso.onView(matcher).perform(click());
        return origin;
    }

    @Override
    public AppEventOuterClass.Click performXYAction(AppEventOuterClass.Click origin, int x, int y) {
        Espresso.onView(isRoot()).perform(ChimpActionFactory.clickXY(x, y));
        return origin;
    }

    @Override
    public AppEventOuterClass.Click performUiObjectAction(AppEventOuterClass.Click origin, UiObject uiObject) throws UiObjectNotFoundException {
        String display = wildCardManager.getUiObjectDisplay(uiObject);
        // uiObject.click();
        Log.i(tag("UiObjectAction"), "Retrieving display bounds from UIObject");
        Rect rect = uiObject.getBounds();
        Log.i(tag("UiObjectAction"), "Executing espresso action");
        Espresso.onView(isRoot()).perform(ChimpActionFactory.clickXY(rect.centerX(),rect.centerY()));

        AppEventOuterClass.Click.Builder builder = AppEventOuterClass.Click.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID).setNameid(display));

        Log.i(tag("UiObjectAction"), "Completed retrieval and execute.");
        return builder.build();
    }

}
