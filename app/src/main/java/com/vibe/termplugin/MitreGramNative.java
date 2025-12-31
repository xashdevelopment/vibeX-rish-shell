package com.vibe.termplugin;

import android.os.Handler;
import android.os.Looper;

/**
 * MitreGramNative - JNI Wrapper for libQASM.so (MitreGramNative)
 * 
 * This class provides the native interface to the MitreGram library
 * which handles dialogs, callbacks, and native utilities.
 */
public class MitreGramNative {
    
    private static final String TAG = "MitreGramNative";
    
    // Reference to the activity for callbacks
    private static RishShellActivity activity;
    private static Handler uiHandler;
    
    // Load the native library
    static {
        System.loadLibrary("QASM");
    }
    
    /**
     * Initialize the native library bridge
     * @param activity The activity to receive callbacks
     */
    public void init(RishShellActivity activity) {
        MitreGramNative.activity = activity;
        uiHandler = new Handler(Looper.getMainLooper());
        nativeInit();
    }
    
    /**
     * Initialize the welcome protocol
     * Called from RishShellActivity to trigger the native welcome dialog
     */
    public void initWelcomeProtocol() {
        // This will trigger the native JNI_OnLoad which will call back to showDialogFromNative
        nativeInit();
    }
    
    /**
     * Called from native code to trigger the welcome dialog
     * This is the callback method that native code invokes
     */
    public void onShowWelcomeDialog() {
        if (uiHandler != null && activity != null) {
            uiHandler.post(() -> {
                if (activity != null) {
                    activity.showDialogFromNative();
                }
            });
        }
    }
    
    /**
     * Handle qers action from native code
     * @param mode Action mode (0=exit, else=dismiss)
     */
    public void handleQersAction(int mode) {
        if (mode == 0) {
            // Exit application
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }
    
    /**
     * Show a dialog with title and message from native code
     */
    public void showMessage(String title, String message) {
        nativeShowMessage(title, message);
    }
    
    /**
     * Log a message from native code
     */
    public void log(String message) {
        nativeLog(message);
    }
    
    /**
     * Cleanup native resources
     */
    public void cleanup() {
        nativeCleanup();
    }
    
    // Native method declarations
    private native void nativeInit();
    private native void nativeShowMessage(String title, String message);
    private native void nativeLog(String message);
    private native void nativeCleanup();
    
    /**
     * Static method called from native code to trigger UI callback
     * This is called via JNI when the native library wants to show the welcome dialog
     */
    private static void triggerWelcomeDialogCallback() {
        if (uiHandler != null && activity != null) {
            uiHandler.post(() -> {
                if (activity != null) {
                    activity.showDialogFromNative();
                }
            });
        }
    }
}
