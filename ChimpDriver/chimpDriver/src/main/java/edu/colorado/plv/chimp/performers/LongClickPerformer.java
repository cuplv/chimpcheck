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
import edu.colorado.plv.chimp.viewactions.ChimpActionFactory;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Created by edmundlam on 5/19/17.
 */

public class LongClickPerformer extends Performer<AppEventOuterClass.LongClick> {

    public LongClickPerformer(ChimpDriver chimpDriver, ViewManager viewManager,
                              WildCardManager wildCardManager, BySelector wildCardSelector,
                              Matcher<View> userDefinedMatcher) {
        super("Click", chimpDriver, viewManager, wildCardManager, wildCardSelector, userDefinedMatcher);
    }

    @Override
    public ArrayList<ViewManager.ViewTarget> getTargets(AppEventOuterClass.LongClick click) {
        return viewManager.retrieveTargets(click.getUiid());
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
    public AppEventOuterClass.LongClick performWildCardTargetAction(AppEventOuterClass.LongClick origin, WildCardTarget target) {
        Espresso.onView(target.uiMatcher).perform(longClick());

        AppEventOuterClass.LongClick.Builder builder = AppEventOuterClass.LongClick.newBuilder();
        builder.setUiid(AppEventOuterClass.UIID.newBuilder().setIdType(AppEventOuterClass.UIID.UIIDType.NAME_ID)
                .setNameid(MatcherManager.describeMatcherAsDisplay(target.uiObj)));
        return builder.build();

    }


}
