package edu.colorado.plv.chimp.driver;

/**
 * Created by edmund on 2/6/17.
 */

import android.os.Bundle;
import android.support.test.runner.AndroidJUnitRunner;

import android.util.Base64;
import android.util.Log;
import chimp.protobuf.*;
import com.google.protobuf.InvalidProtocolBufferException;

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

    public String chimpTag(String tag) { return String.format("%s:%s",chimpName, tag); }

    public void printOnLogCat(String tag, String data) {
        Log.i(String.format("%s:%s",chimpName, tag), data);
    }

    public String getSyncFile() { return syncFile; }

}
