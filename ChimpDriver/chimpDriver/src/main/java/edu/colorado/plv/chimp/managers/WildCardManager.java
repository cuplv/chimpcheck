package edu.colorado.plv.chimp.managers;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by edmundlam on 5/5/17.
 */

public class WildCardManager {

    protected UiDevice mDevice = null;
    protected Random seed = new Random();

    public void initUiDevice() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        mDevice.setCompressedLayoutHeirarchy(true);
    }

    public String uiObjectInfo(UiObject object) throws UiObjectNotFoundException {
        return object.hashCode() + " " + object.toString() + " " + object.getClassName() +
                object.getText() + " " + object.getContentDescription() + " " + object.getChildCount();
    }

    public String getUiObjectDisplay(UiObject object) {
        String display = "";
        try {
            display = object.getText();
            if (display == null || display.length() == 0) {
                display = object.getContentDescription();
            }
        } catch (UiObjectNotFoundException e) {
            Log.e("Chimp-wildCardManager","UiObject not found while getting display strings",e);
        }
        return display;
    }

    public UiObject popOne(ArrayList<UiObject> uiObjects) {
        if (uiObjects.size() > 0) {
            int randIdx = seed.nextInt(uiObjects.size());
            UiObject cand = uiObjects.get(randIdx);
            uiObjects.remove(randIdx);
            return cand;
        }
        return null;
    }

    public ArrayList<UiObject> retrieveTopLevelUiObjects(UiSelector uiSelector) throws UiObjectNotFoundException {
        int i = 0;
        ArrayList<UiObject> uiObjects = new ArrayList<>();
        while (true) {
            UiObject candidate = mDevice.findObject(uiSelector.instance(i++));
            if (candidate == null || !candidate.exists()) {
                break;
            } else {
                uiObjects.add( candidate );
            }
        }
        return uiObjects;
    }

    public ArrayList<UiObject> retrieveChildUiObjects(UiObject uiObject, UiSelector uiSelector) throws UiObjectNotFoundException {
        ArrayList<UiObject> list = new ArrayList<>();
        if (uiObject.getChildCount() == 0) {
            Log.i("Chimp-wildCardManager","Leaf UIObject: " + uiObjectInfo(uiObject));
            list.add(uiObject);
        } else {
            Log.i("retrieveUIObjects","Non-Leaf UIObject: " + uiObjectInfo(uiObject));
            int i = 0;
            while (true) {
                UiObject candidate = uiObject.getChild(uiSelector.instance(i++));
                if (candidate == null || !candidate.exists()) {
                    break;
                } else {
                    list.add( candidate );
                }
            }
        }
        return list;
    }

    public ArrayList<UiObject> retrieveUiObjects(UiSelector topLevelSelector, UiSelector childSelector, int depth) throws UiObjectNotFoundException {
        if (depth == 0) { return new ArrayList<>(); }
        ArrayList<UiObject> currUiObjects = retrieveTopLevelUiObjects(topLevelSelector);
        ArrayList<UiObject> baseUiObjects = new ArrayList<>();
        while (depth > 0 && currUiObjects.size() > 0) {
            ArrayList<UiObject> tempUiObjects = new ArrayList<>();
            for(UiObject currUiObject: currUiObjects) {
                if (currUiObject.getChildCount() != 0) {
                    tempUiObjects.addAll( retrieveChildUiObjects(currUiObject, childSelector) );
                } else {
                    baseUiObjects.add( currUiObject );
                }
            }
            currUiObjects = tempUiObjects;
            depth--;
        }

        String str = "/***** Inferred Actionable UI Objects *****/\n";
        for(UiObject candidate: new HashSet<UiObject>(baseUiObjects)) {
            str += "Found candidate: " + candidate.getClassName() + " " + candidate.getText() + " " + candidate.getContentDescription() + " UI Selector: " + candidate.getSelector().toString() + "\n";
        }
        str += "/******************************************/";
        Log.i("Chimp-wildCardManager",str);

        return baseUiObjects;
    }

    public ArrayList<UiObject> retrieveUiObjects(UiSelector topLevelSelector, UiSelector childSelector) throws UiObjectNotFoundException {
        return retrieveUiObjects(topLevelSelector, childSelector, 10);
    }

    public void randomRun(int step) throws UiObjectNotFoundException {
        if (step == 0) { return; }

        UiObject chosen = popOne( retrieveUiObjects(new UiSelector().clickable(true), new UiSelector().enabled(true)) );
        if (chosen == null) {
            Log.i("RandomRun", "Dead end");
            return;
        }

        Log.i("RandomRun", "Chosen step: " + uiObjectInfo(chosen) );
        chosen.click();

        randomRun(step - 1);
    }

    public ArrayList<UiObject> retrieveExistingLeafUIObjects(UiSelector rootSelector, UiSelector childSelector)
            throws UiObjectNotFoundException {

        int i = 0;
        ArrayList<UiObject> list = new ArrayList<>();
        while (true) {
            // UiObject candidate = mDevice.findObject(new UiSelector().candidate(true).instance(i++));
            UiObject candidate = mDevice.findObject(rootSelector.instance(i++));
            if (candidate == null || !candidate.exists()) {
                break;
            } else {
                // Log.i("retrieveUIObjects","Root level UIObject: " + uiObjectInfo(candidate));
                list.addAll( retrieveExistingLeafUIObjects( candidate, childSelector, 0 ) );
            }
        }

        String str = "/***** Inferred Actionable UI Objects *****/\n";
        for(UiObject candidate: new HashSet<UiObject>(list)) {
            str += "Found candidate: " + candidate.getClassName() + " " + candidate.getText() + " " + candidate.getContentDescription() + "\n";
        }
        str += "/******************************************/";
        Log.i("Chimp-wildCardManager",str);

        return list;
    }

    public ArrayList<UiObject> retrieveExistingLeafUIObjects(UiObject uiObject, UiSelector childSelector, int level)
            throws UiObjectNotFoundException {
        ArrayList<UiObject> list = new ArrayList<>();
        if (uiObject.getChildCount() == 0) {
            Log.i("Chimp-wildCardManager","Leaf UIObject: " + uiObjectInfo(uiObject) + " Level: " + level);
            list.add(uiObject);
        } else {
            Log.i("Chimp-wildCardManager","Non-Leaf UIObject: " + uiObjectInfo(uiObject) + " Level: " + level);
            int i = 0;
            while (true) {
                // UiObject candidate = uiObject.getChild(new UiSelector().enabled(true).instance(i++));
                UiObject candidate = uiObject.getChild(childSelector.instance(i++));
                if (candidate == null || !candidate.exists()) {
                    break;
                } else {
                    list.addAll( retrieveExistingLeafUIObjects( candidate, childSelector, level + 1 ) );
                }
            }
        }
        return list;
    }


    public ArrayList<UiObject2> retrieveTopLevelUIObjects2(BySelector top){
        return (ArrayList<UiObject2>)mDevice.findObjects(top);
    }

}
