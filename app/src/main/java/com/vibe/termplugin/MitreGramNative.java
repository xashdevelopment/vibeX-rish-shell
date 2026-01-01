package com.vibe.termplugin;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * MitreGramNative - Java-only implementation
 *
 * Native library integration has been removed.
 * All methods now provide stub implementations.
 */
public class MitreGramNative {

    private static final String TAG = "MitreGramNative";

    // Reference to the activity for callbacks
    private RishShellActivity activity;
    private Handler uiHandler;

    /**
     * Initialize the native library bridge
     * @param activity The activity to receive callbacks
     */
    public void init(RishShellActivity activity) {
        this.activity = activity;
        uiHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "MitreGramNative initialized (Java-only mode)");
    }

    /**
     * Initialize the welcome protocol
     */
    public void initWelcomeProtocol(RishShellActivity activity) {
        this.activity = activity;
        uiHandler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "Welcome protocol initialized (Java-only mode)");
    }

    /**
     * Called to trigger the welcome dialog
     */
    public void onShowWelcomeDialog() {
        if (uiHandler != null && activity != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (activity != null) {
                        activity.showDialogFromNative();
                    }
                }
            });
        }
    }

    /**
     * Handle qers action
     * @param mode Action mode
     */
    public void handleQersAction(int mode) {
        Log.d(TAG, "handleQersAction called with mode: " + mode);
        if (mode == 0) {
            // Exit application
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * Show a dialog with title and message
     */
    public void showMessage(String title, String message) {
        Log.d(TAG, "showMessage: " + title + " - " + message);
    }

    /**
     * Log a message
     */
    public void log(String message) {
        Log.d(TAG, message);
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        Log.d(TAG, "cleanup called (Java-only mode)");
    }
}
