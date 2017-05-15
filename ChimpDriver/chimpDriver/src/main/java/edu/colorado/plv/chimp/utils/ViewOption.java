package edu.colorado.plv.chimp.utils;

import android.view.View;

import org.hamcrest.Matcher;

import chimp.protobuf.AppEventOuterClass;

/**
 * Created by edmund on 5/2/17.
 */
abstract public class ViewOption {

    protected AppEventOuterClass.UIID uiid = null;
    protected Matcher<View> matcher = null;
    protected View view = null;
    protected String display = null;

    public boolean hasUIID()    { return uiid != null; }
    public boolean hasMatcher() { return matcher != null; }
    public boolean hasView()    { return view != null; }
    public boolean hasDisplay() { return display != null; }

    public AppEventOuterClass.UIID getUIID() { return uiid; }
    public Matcher<View> getMatcher() { return matcher; }
    public View getView() { return view; }
    public String getDisplay() { return display; }




}
