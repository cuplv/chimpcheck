package edu.colorado.plv.chimp.managers;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
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
    public String uiObject2Info(UiObject2 object) throws UiObjectNotFoundException {
        return object.toString() + " " + object.getClassName() + object.getResourceName()+
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

    public <T> T popOne(ArrayList<T> uiObjects) {
        if (uiObjects.size() > 0) {
            int randIdx = seed.nextInt(uiObjects.size());
            T cand = uiObjects.get(randIdx);
            uiObjects.remove(randIdx);
            return cand;
        }
        return null;
    }



    public ArrayList<UiObject2> retrieveTopLevelUiObjects(BySelector uiSelector) throws UiObjectNotFoundException {
        int i = 0;
        ArrayList<UiObject2> uiObjects = (ArrayList<UiObject2>) mDevice.findObjects(uiSelector);
        return uiObjects;
    }
    public ArrayList<UiObject2> retrieveChildUiObjects(UiObject2 uiObject, BySelector uiSelector) throws UiObjectNotFoundException {
        ArrayList<UiObject2> list = new ArrayList<>();
        if (uiObject.getChildCount() == 0) {
            Log.i("Chimp-wildCardManager","Leaf UIObject: " + uiObject2Info(uiObject));
            list.add(uiObject);
        } else {
            Log.i("retrieveUIObjects","Non-Leaf UIObject: " + uiObject2Info(uiObject));
            int i = 0;
            list.addAll(uiObject.getChildren());
        }
        return list;
    }
    public ArrayList<UiObject2> retrieveUiObject2s(BySelector topLevelSelector, BySelector childSelector, int depth) throws UiObjectNotFoundException {
        if (depth == 0) { return new ArrayList<>(); }
        ArrayList<UiObject2> currUiObjects = retrieveTopLevelUiObjects(topLevelSelector);
        ArrayList<UiObject2> baseUiObjects = new ArrayList<>();
        while (depth > 0 && currUiObjects.size() > 0) {
            ArrayList<UiObject2> tempUiObjects = new ArrayList<>();
            for(UiObject2 currUiObject: currUiObjects) {
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
        for(UiObject2 candidate: new HashSet<UiObject2>(baseUiObjects)) {
            str += "Found candidate: " + candidate.getClassName() + " " + candidate.getText() + " " + candidate.getContentDescription() + "\n";
        }
        str += "/******************************************/";
        Log.i("Chimp-wildCardManager",str);

        return baseUiObjects;
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
