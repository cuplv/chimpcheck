/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.testing.uiautomator.BasicSample;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.util.Log;
import android.view.View;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.colorado.plv.chimp.components.ActivityManager;
import plv.colorado.edu.clickingtestapp.MainActivity;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.supportsInputMethods;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Basic sample for unbundled UiAutomator.
 */
@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
public class ExampleInstrumentedTest{

    private static final String BASIC_SAMPLE_PACKAGE
            = "plv.colorado.edu.clickingtestapp";

    private static final int LAUNCH_TIMEOUT = 5000;

    private static final String STRING_TO_BE_TYPED = "UiAutomator";

    private UiDevice mDevice;

    @Before
    public void startMainActivityFromHomeScreen() {
        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the blueprint app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(BASIC_SAMPLE_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BASIC_SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT);
    }

    protected Activity current;
    protected Activity getActivityInstance() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);

                Activity act;
                for(Iterator var2 = resumedActivity.iterator(); var2.hasNext(); ExampleInstrumentedTest.this.current = act) {
                    act = (Activity)var2.next();
                }

            }
        });
        return this.current;
    }
    protected View getDecorView() {
        return this.getActivityInstance().getWindow().getDecorView();
    }

    class Wrapper{
        String name;
        String defType;
        String defPackage;
        public Wrapper(String a, String b, String c){
            name = a;
            defType = b;
            defPackage = c;
        }
    }
    public Wrapper helper(String res){
        String[] strs = res.split(":");
        String[] strs2 = strs[1].split("\\/");
        String defPackage = strs[0];
        String defType = strs2[0];
        String name = strs2[1];
        return new Wrapper(name, defType, defPackage);
    }
    @Test
    public void clickTest(){
        List<UiObject2> objects = mDevice.findObjects(By.clickable(true));
        UiObject2 obj = objects.get(1);
        Log.d("TAG", obj.getResourceName());
        Resources r = getActivityInstance().getResources();
        Wrapper wr = helper(obj.getResourceName());
        int id = r.getIdentifier(wr.name, wr.defType, wr.defPackage);
        Log.d("TAG", Integer.toString(id));
        Espresso.onView(withId(id)).perform(click());

        objects = mDevice.findObjects(By.clickable(true));
        obj = objects.get(1);
        wr = helper(obj.getResourceName());
        id = r.getIdentifier(wr.name, wr.defType, wr.defPackage);
        Espresso.onView(allOf(withId(id), supportsInputMethods() )).perform(typeText("string to be typed"));

    }
    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.`
     */
    private String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}
