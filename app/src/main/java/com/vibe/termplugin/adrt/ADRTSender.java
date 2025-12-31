package com.vibe.termplugin.adrt;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * ADRT Sender - Broadcasts debug events to a debugger package
 * Provides integration with external debuggers for breakpoint handling,
 * variable inspection, and logcat streaming
 */
public class ADRTSender {
    
    private static final String TAG = "ADRTSender";
    
    private static Context context;
    private static String debuggerPackageName;
    
    /**
     * Initialize the ADRT sender with context and debugger package
     */
    public static void onContext(Context ctx, String packageName) {
        context = ctx.getApplicationContext();
        debuggerPackageName = packageName;
    }
    
    /**
     * Send a connect event to the debugger
     */
    public static void sendConnect(String packageName) {
        if (context == null || debuggerPackageName == null) {
            Log.w(TAG, "ADRT not initialized");
            return;
        }
        
        try {
            Intent intent = new Intent();
            intent.setPackage(debuggerPackageName);
            intent.setAction("com.adrt.CONNECT");
            intent.putExtra("package", packageName);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending connect", e);
        }
    }
    
    /**
     * Send a stop event to the debugger
     */
    public static void sendStop(String packageName) {
        if (context == null || debuggerPackageName == null) {
            Log.w(TAG, "ADRT not initialized");
            return;
        }
        
        try {
            Intent intent = new Intent();
            intent.setPackage(debuggerPackageName);
            intent.setAction("com.adrt.STOP");
            intent.putExtra("package", packageName);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending stop", e);
        }
    }
    
    /**
     * Send a breakpoint hit event with variable information
     */
    public static void sendBreakpointHit(
            String packageName,
            ArrayList<String> variables,
            ArrayList<String> variableValues,
            ArrayList<String> variableKinds,
            ArrayList<String> stackMethods,
            ArrayList<String> stackLocations,
            ArrayList<String> stackLocationKinds) {
        
        if (context == null || debuggerPackageName == null) {
            Log.w(TAG, "ADRT not initialized");
            return;
        }
        
        try {
            Intent intent = new Intent();
            intent.setPackage(debuggerPackageName);
            intent.setAction("com.adrt.BREAKPOINT_HIT");
            intent.putExtra("package", packageName);
            intent.putExtra("variables", variables);
            intent.putExtra("variableValues", variableValues);
            intent.putExtra("variableKinds", variableKinds);
            intent.putExtra("stackMethods", stackMethods);
            intent.putExtra("stackLocations", stackLocations);
            intent.putExtra("stackLocationKinds", stackLocationKinds);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending breakpoint hit", e);
        }
    }
    
    /**
     * Send field information to the debugger
     */
    public static void sendFields(
            String packageName,
            String path,
            ArrayList<String> fields,
            ArrayList<String> fieldValues,
            ArrayList<String> fieldKinds) {
        
        if (context == null || debuggerPackageName == null) {
            Log.w(TAG, "ADRT not initialized");
            return;
        }
        
        try {
            Intent intent = new Intent();
            intent.setPackage(debuggerPackageName);
            intent.setAction("com.adrt.FIELDS");
            intent.putExtra("package", packageName);
            intent.putExtra("path", path);
            intent.putExtra("fields", fields);
            intent.putExtra("fieldValues", fieldValues);
            intent.putExtra("fieldKinds", fieldKinds);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending fields", e);
        }
    }
    
    /**
     * Send logcat lines to the debugger
     */
    public static void sendLogcatLines(String[] lines) {
        if (context == null || debuggerPackageName == null) {
            Log.w(TAG, "ADRT not initialized");
            return;
        }
        
        try {
            Intent intent = new Intent();
            intent.setPackage(debuggerPackageName);
            intent.setAction("com.adrt.LOGCAT_ENTRIES");
            intent.putExtra("lines", lines);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error sending logcat lines", e);
        }
    }
}
