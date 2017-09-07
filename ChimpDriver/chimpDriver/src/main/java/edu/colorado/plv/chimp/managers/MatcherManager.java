package edu.colorado.plv.chimp.managers;

import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import edu.colorado.plv.chimp.performers.WildCardTarget;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static edu.colorado.plv.chimp.components.ActivityManager.getResIdFromResName;
import static org.hamcrest.Matchers.allOf;
/**
 * Created by Pezh on 8/28/17.
 */

public class MatcherManager {
    private static Matcher<View> getViewMatchers(int resid, String text, String content, Matcher<? super View> ...matcher){
        ArrayList<Matcher<? super View>> list = new ArrayList<>();

        if(resid == 0 && text == null && content == null){
            return null;
        }
        if(resid != 0){
            list.add(withId(resid));
        }
        if(text != null){
            list.add(withText(text));
        }
        if(content != null){
            list.add(withContentDescription(content));
        }

        list.add(isDisplayed());

        for (int i = 0; i < matcher.length; ++i) {
            list.add(matcher[i]);
        }
        return allOf(list);
    }



    public static ArrayList<WildCardTarget> getViewMatchers(List<UiObject2> objs, Matcher<? super View> ...matcher){
        ArrayList<WildCardTarget> list = new ArrayList<>();
        Matcher<View> matchers = null;
        for(UiObject2 obj : objs) {
            matchers = MatcherManager.getViewMatchers(getResIdFromResName(obj.getResourceName()), obj.getText(), obj.getContentDescription(), matcher);
            //TODO: Refactor this
            if(matchers == null){
                continue;
            }
            list.add(new WildCardTarget(matchers, obj));

        }
        return list;
    }

    public static String uiObject2Info(UiObject2 object) {
        return object.toString() + " " + object.getClassName() + " " + object.getResourceName() + " " +
                object.getText() + " " + object.getContentDescription() + " " + object.getChildCount();
    }

    public static String describeMatcherAsDisplay(UiObject2 uiObject){
        try {
            String display = edu.colorado.plv.chimp.components.ActivityManager.getResEntryName(uiObject.getResourceName());
            if (display != null) {
                display = "R.id." + display;
                return display;
            } else {
                String contentDescription = uiObject.getContentDescription();
                if (contentDescription != null) {
                    display = "Content Desc: " + contentDescription;
                    return display;
                } else {
                    String text = uiObject.getText();
                    if (text != null) {
                        display = "Text: " + text;
                        return display;
                    }
                }
            }
        } catch (StaleObjectException soe){
            Log.e("MatchManager", "Cannot Describe UiObject2");
        }
        return "No Display";
    }

}
