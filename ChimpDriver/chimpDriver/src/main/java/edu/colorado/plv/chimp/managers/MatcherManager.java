package edu.colorado.plv.chimp.managers;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.uiautomator.UiObject2;
import android.view.View;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import edu.colorado.plv.chimp.components.*;

import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
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



    public static ArrayList<Matcher<View>> getViewMatchers(List<UiObject2> objs, Matcher<? super View> ...matcher){
        ArrayList<Matcher<View>> list = new ArrayList<>();
        Matcher<View> matchers = null;
        for(UiObject2 obj : objs) {
            matchers = MatcherManager.getViewMatchers(getResIdFromResName(obj.getResourceName()), obj.getText(), obj.getContentDescription(), matcher);
            list.add(matchers);

        }
        return list;
    }


    public static  String describeMatcherAsDisplay(UiObject2 uiObject){
        String display = edu.colorado.plv.chimp.components.ActivityManager.getResEntryName(uiObject.getResourceName());
        if(display == null) display = "Content Desc: ".concat(uiObject.getContentDescription());
        if(display == null) display = "Text: ".concat(uiObject.getText());
        return display;
    }

}
