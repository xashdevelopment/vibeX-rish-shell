package com.vibe.termplugin.adrt;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ADRT LogCat Reader - Continuously reads Android logcat output
 * and broadcasts it to an external debugger for debugging purposes
 */
public class ADRTLogCatReader implements Runnable {
    
    private static final String TAG = "ADRTLogCatReader";
    
    private static Context context;
    private static String debuggerPackageName;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);
    
    /**
     * Initialize the logcat reader with context and debugger package
     */
    public static void onContext(Context ctx, String packageName) {
        // Only initialize once
        if (context != null) {
            return;
        }
        
        context = ctx.getApplicationContext();
        
        // Check if app is debuggable
        ApplicationInfo appInfo = ctx.getApplicationInfo();
        boolean isDebuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        
        if (!isDebuggable) {
            Log.d(TAG, "App is not debuggable, ADRT disabled");
            return;
        }
        
        try {
            // Verify debugger package is installed
            PackageManager pm = ctx.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            debuggerPackageName = packageName;
            
            // Initialize ADRTSender
            ADRTSender.onContext(context, debuggerPackageName);
            
            // Start logcat reader thread
            Thread logCatThread = new Thread(new ADRTLogCatReader(), "LogCat");
            logCatThread.start();
            
            Log.d(TAG, "ADRT LogCat reader started");
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Debugger package not found: " + packageName);
        }
    }
    
    /**
     * Read logcat output and send to debugger
     */
    @Override
    public void run() {
        if (context == null || debuggerPackageName == null) {
            return;
        }
        
        isRunning.set(true);
        
        Process logcatProcess = null;
        BufferedReader reader = null;
        
        try {
            // Start logcat with threadtime format
            logcatProcess = Runtime.getRuntime().exec("logcat -v threadtime");
            reader = new BufferedReader(new InputStreamReader(
                logcatProcess.getInputStream()), 8192);
            
            String line;
            while (isRunning.get() && (line = reader.readLine()) != null) {
                try {
                    // Send each logcat line to the debugger
                    String[] lines = new String[]{line};
                    ADRTSender.sendLogcatLines(lines);
                } catch (Exception e) {
                    Log.w(TAG, "Error sending logcat line", e);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "LogCat reading error", e);
        } finally {
            isRunning.set(false);
            
            // Cleanup
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
            if (logcatProcess != null) {
                logcatProcess.destroy();
            }
        }
    }
    
    /**
     * Stop the logcat reader
     */
    public static void stop() {
        isRunning.set(false);
    }
}
