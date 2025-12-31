package com.vibe.termplugin.tasker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Tasker Activity - Provides integration with Tasker automation app
 * Allows Tasker to execute shell commands through this app
 */
public class TaskerActivity extends Activity {
    
    private static final String TAG = "VibeX_Tasker";
    
    private TextView statusView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Setup simple UI
        setupUI();
        
        // Handle Tasker intent
        handleTaskerIntent();
    }
    
    /**
     * Setup simple status display UI
     */
    private void setupUI() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF1A1A1A);
        
        statusView = new TextView(this);
        statusView.setTextColor(Color.WHITE);
        statusView.setTextSize(14);
        statusView.setTypeface(Typeface.MONOSPACE);
        statusView.setPadding(40, 40, 40, 40);
        
        layout.addView(statusView);
        setContentView(layout);
    }
    
    /**
     * Handle incoming Tasker intent
     */
    private void handleTaskerIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            showStatus("No intent received");
            return;
        }
        
        String action = intent.getAction();
        showStatus("Tasker Action: " + action + "\n\n");
        
        if ("com.twohlix.tasker.ACTION_RUN_TASK".equals(action)) {
            // Get command from intent extras
            String command = intent.getStringExtra("command");
            if (command != null && !command.isEmpty()) {
                showStatus("Executing: " + command + "\n\n");
                // In a full implementation, this would execute the command
                // and return the result via result intent
                executeCommand(command);
            } else {
                showStatus("No command specified");
            }
        } else {
            showStatus("Unknown action: " + action);
        }
    }
    
    /**
     * Execute a shell command (placeholder implementation)
     */
    private void executeCommand(String command) {
        try {
            // This would integrate with the shell execution in RishShellActivity
            // For now, show that the command was received
            showStatus("Command received: " + command + "\n\n");
            showStatus("Note: Full shell execution requires Shizuku service");
            
            // Return result to Tasker
            Intent resultIntent = new Intent();
            resultIntent.putExtra("exit_code", 0);
            resultIntent.putExtra("output", "Command queued: " + command);
            setResult(RESULT_OK, resultIntent);
            
        } catch (Exception e) {
            showStatus("Error: " + e.getMessage());
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("exit_code", 1);
            resultIntent.putExtra("error", e.getMessage());
            setResult(RESULT_OK, resultIntent);
        }
    }
    
    /**
     * Display status message
     */
    private void showStatus(String message) {
        statusView.append(message + "\n");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
