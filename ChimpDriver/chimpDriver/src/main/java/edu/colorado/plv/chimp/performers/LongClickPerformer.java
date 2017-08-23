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

import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by Pezh on 8/23/17.
 */

public class LongClickPerformer extends Performer<AppEventOuterClass.LongClick> {
    public LongClickPerformer(ChimpDriver chimpDriver, ViewManager viewManager, WildCardManager wildCardManager, UiSelector wildCardTopSelector, UiSelector wildCardChildSelector) {
        super("LongClick", chimpDriver, viewManager, wildCardManager, wildCardTopSelector, wildCardChildSelector);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.LongClick longClick) {
        return viewManager.retrieveTargets(longClick.getUiid());
    }


    @Override
    public AppEventOuterClass.LongClick performMatcherAction(AppEventOuterClass.LongClick origin, Matcher<View> matcher) {
        Espresso.onView(matcher).perform(longClick());
        return origin;
    }

    @Override
    public AppEventOuterClass.LongClick performXYAction(AppEventOuterClass.LongClick origin, int x, int y) {
        Espresso.onView(isRoot()).perform(ChimpActionFactory.longClickXY(x, y));
        return origin;
    }

    @Override
    public AppEventOuterClass.LongClick performUiObjectAction(AppEventOuterClass.LongClick origin, UiObject uiObject) throws UiObjectNotFoundException {
        String display = wildCardManager.getUiObjectDisplay(uiObject);
        Log.i(tag("UiObjectAction"), "Retrieving display bounds from UIObject");
        Rect rect = uiObject.getBounds();
        Log.i(tag("UiObjectAction"), "Executing espresso action");
        Espresso.onView(isRoot()).perform(ChimpActionFactory.longClickXY(rect.centerX(),rect.centerY()));

        AppEventOuterClass.LongClick.Builder builder = AppEventOuterClass.LongClick.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID).setNameid(display));

        Log.i(tag("UiObjectAction"), "Completed retrieval and execute.");
        return builder.build();
    }
}
