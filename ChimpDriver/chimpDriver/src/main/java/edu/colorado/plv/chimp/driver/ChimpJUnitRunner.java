package edu.colorado.plv.chimp.driver;

/**
 * Created by edmund on 2/6/17.
 */

import android.os.Bundle;
import android.support.test.internal.runner.listener.InstrumentationResultPrinter;
import android.support.test.runner.AndroidJUnitRunner;

import android.util.Base64;
import android.util.Log;
import chimp.protobuf.*;
import com.google.protobuf.InvalidProtocolBufferException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ChimpJUnitRunner extends AndroidJUnitRunner {

    private static final String TAG = null;
    private EventTraceOuterClass.EventTrace eventTrace;
    private String appPackageName;
    private String chimpName;
    private String syncFile;
    private boolean inputIsWelformed = true;

    private Map<String, String> chimpReports = new HashMap<String, String>();

    /* (non-Javadoc)
     * @see android.test.InstrumentationTestRunner#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        if (arguments != null) {
            String base64EncodedEventTrace = arguments.getString("eventTrace");
            try {
                eventTrace = EventTraceOuterClass.EventTrace.parseFrom(
                        Base64.decode(base64EncodedEventTrace, Base64.DEFAULT)
                );
            } catch (InvalidProtocolBufferException e) {
                inputIsWelformed = false;
                Log.e("ChimpDriver",String.format("InvalidProtocolBufferException: %s", e.toString()));
            } catch (Exception e) {
                inputIsWelformed = false;
                Log.e("ChimpDriver",String.format("Unknown Exception during eventTrace extraction: %s", e.toString()));
            }

            appPackageName = arguments.getString("appPackageName");

            chimpName = arguments.getString("chimpName", "Caesar");

            syncFile = arguments.getString("syncFile", "sync-file");
        }
    }

    // This override is necessary to solve the 'missing ChimpCheck result issue'
    // Particularly, it is caused by UI Automator APIs: exception in the app is treated as exception in the tester.
    // This override intercepts such untimely exceptions and injects the ChimpCheck results.
    // Note: works in conjunction with 'preemptiveTraceReport' of ChimpDriver
    @Override
    public boolean onException(Object obj, Throwable e) {

        Log.e("ChimpDriver",String.format("ChimpJUnitRunner onException was called: %s \n super class: %s", e.toString(), super.getClass()));

        InstrumentationResultPrinter resultPrinter = null;
        boolean succ = false;

        // Use reflection to get the instrumentation result printer
        try {
            Method method = this.getClass().getSuperclass().getDeclaredMethod("getInstrumentationResultPrinter");
            method.setAccessible(true);
            resultPrinter = (InstrumentationResultPrinter) method.invoke(this);
            succ = true;
        } catch (Exception allEs) {
            Log.e("ChimpDriver", "Error while reflecting on ChimpJUnitRunner: " + allEs.toString());
        }

        Log.e("ChimpDriver", "Extracted Results: " + succ + " " + (resultPrinter != null) );

        // Use reflection to extract the result bundle from the instrumentation result printer,
        // then inject ChimpCheck results into the result bundle
        if (resultPrinter != null) {
            try {
                Field field = resultPrinter.getClass().getDeclaredField("mTestResult");
                field.setAccessible(true);
                Bundle bundle = (Bundle) field.get(resultPrinter);
                bundle.putString("ChimpCheck-ReflectionHack","Enabled");
                // Append ChimpCheck results into the result bundle
                for(Map.Entry<String,String> entry : chimpReports.entrySet()) {
                    bundle.putString(entry.getKey(), entry.getValue());
                }
            } catch (Exception allEs) {
                Log.e("ChimpDriver", "Error while reflecting on Instrumentation Result Printer: " + allEs.toString());
            }
        }

        return super.onException(obj,e);
    }

    @Override
    public void finish(int code, Bundle bundle) {
        // bundle.putString("chimp-check", "Little monkeys.");
        // bundle.putString("##EeeeWah", "The chimp is here!");
        for(Map.Entry<String,String> entry : chimpReports.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }
        super.finish(code, bundle);
    }

    public boolean inputIsWelformed() { return inputIsWelformed; }

    public EventTraceOuterClass.EventTrace getEventTrace() {
        return eventTrace;
    }

    public String getAppPackageName() { return appPackageName; }

    public void addReport(String key, String data) { chimpReports.put(key, data); }

    public void removeReport(String key) { chimpReports.remove(key); }

    public void flushReports() { chimpReports = new HashMap<String, String>(); }

    public String chimpTag(String tag) { return String.format("%s:%s",chimpName, tag); }

    public void printOnLogCat(String tag, String data) {
        Log.i(String.format("%s:%s",chimpName, tag), data);
    }

    public String getSyncFile() { return syncFile; }

}
