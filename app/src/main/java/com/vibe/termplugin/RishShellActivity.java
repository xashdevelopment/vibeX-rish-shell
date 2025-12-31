package com.vibe.termplugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.vibe.termplugin.adrt.ADRTLogCatReader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * VibeX Rish Shell Activity - Main terminal interface for shell execution
 * Provides a Material Design 3 styled terminal UI for executing commands via Shizuku shell
 */
public class RishShellActivity extends Activity {
    
    private static final String TAG = "VibeXRishShell";
    
    // UI Components
    private LinearLayout rootContainer;
    private LinearLayout headerContainer;
    private TextView titleView;
    private ScrollView terminalScrollView;
    private LinearLayout terminalContainer;
    private TextView terminalOutputView;
    private LinearLayout inputContainer;
    private EditText commandInputEdit;
    private ImageButton sendButton;
    private ImageButton clearButton;
    
    private Handler uiHandler;
    private ExecutorService executorService;
    
    // Shell process components
    private Process shellProcess;
    private DataOutputStream shellInputStream;
    private InputStream shellOutputStream;
    
    // Native library integration
    private MitreGramNative nativeBridge;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize UI handler and executor
        uiHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialize ADRT logging with the debugger package
        ADRTLogCatReader.onContext(this, "com.aide.ui.crustacean");
        
        // Initialize native library bridge
        nativeBridge = new MitreGramNative();
        
        // Setup the Material Design 3 UI
        setupMD3UI();
        
        // Load native QASM library and initialize welcome protocol
        System.loadLibrary("QASM");
        nativeBridge.initWelcomeProtocol();
        
        // Copy shell files and start the shell
        executorService.execute(this::copyRishFilesAndStartShell);
    }
    
    /**
     * Setup Material Design 3 styled user interface
     * Implements MD3 design tokens from dimens.xml and colors.xml
     */
    private void setupMD3UI() {
        // Create root container with MD3 background
        rootContainer = new LinearLayout(this);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setBackgroundResource(R.drawable.md3_surface_container);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        rootContainer.setLayoutParams(rootParams);
        
        // Create header with MD3 style
        headerContainer = new LinearLayout(this);
        headerContainer.setOrientation(LinearLayout.HORIZONTAL);
        headerContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        headerContainer.setBackgroundResource(R.drawable.md3_header_background);
        int headerPadding = (int) getResources().getDimension(R.dimen.md3_spacing_large);
        headerContainer.setPadding(headerPadding, headerPadding, headerPadding, headerPadding);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        headerParams.bottomMargin = (int) getResources().getDimension(R.dimen.md3_spacing_medium);
        headerContainer.setLayoutParams(headerParams);
        
        // Create title TextView with MD3 headline style
        titleView = new TextView(this);
        titleView.setText("VibeX Rish Shell");
        titleView.setTextAppearance(R.style.MD3_HeadlineMedium);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.md3_on_surface));
        
        // Add title to header
        headerContainer.addView(titleView);
        rootContainer.addView(headerContainer);
        
        // Create scrollable terminal output area
        terminalScrollView = new ScrollView(this);
        terminalScrollView.setBackgroundResource(R.drawable.md3_terminal_background);
        terminalScrollView.setClipToPadding(false);
        int terminalPadding = (int) getResources().getDimension(R.dimen.md3_spacing_medium);
        terminalScrollView.setPadding(terminalPadding, terminalPadding, terminalPadding, terminalPadding);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        );
        scrollParams.setMargins(
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            0,
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            (int) getResources().getDimension(R.dimen.md3_spacing_small)
        );
        terminalScrollView.setLayoutParams(scrollParams);
        
        // Create terminal container for output
        terminalContainer = new LinearLayout(this);
        terminalContainer.setOrientation(LinearLayout.VERTICAL);
        
        // Create terminal output TextView
        terminalOutputView = new TextView(this);
        terminalOutputView.setTextColor(ContextCompat.getColor(this, R.color.md3_terminal_text));
        terminalOutputView.setTextSize(14);
        terminalOutputView.setTypeface(android.graphics.Typeface.MONOSPACE);
        terminalOutputView.setLineSpacing(
            getResources().getDimension(R.dimen.md3_spacing_extra_small),
            1.0f
        );
        terminalOutputView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // Enable long press to copy
        terminalOutputView.setOnLongClickListener(v -> {
            copyAllOutputToClipboard();
            return true;
        });
        
        terminalContainer.addView(terminalOutputView);
        terminalScrollView.addView(terminalContainer);
        rootContainer.addView(terminalScrollView);
        
        // Create input container with MD3 style
        inputContainer = new LinearLayout(this);
        inputContainer.setOrientation(LinearLayout.HORIZONTAL);
        inputContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);
        inputContainer.setBackgroundResource(R.drawable.md3_input_container_background);
        int inputPadding = (int) getResources().getDimension(R.dimen.md3_spacing_small);
        inputContainer.setPadding(inputPadding, inputPadding, inputPadding, inputPadding);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            0,
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            (int) getResources().getDimension(R.dimen.md3_spacing_medium)
        );
        inputContainer.setLayoutParams(inputParams);
        
        // Create command input EditText with MD3 styling
        commandInputEdit = new EditText(this);
        commandInputEdit.setHint("Enter command...");
        commandInputEdit.setHintTextColor(ContextCompat.getColor(this, R.color.md3_on_surface_variant));
        commandInputEdit.setTextColor(ContextCompat.getColor(this, R.color.md3_on_surface));
        commandInputEdit.setTextSize(14);
        commandInputEdit.setTypeface(android.graphics.Typeface.MONOSPACE);
        commandInputEdit.setBackgroundResource(R.drawable.md3_text_field_background);
        commandInputEdit.setPadding(
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            (int) getResources().getDimension(R.dimen.md3_spacing_small),
            (int) getResources().getDimension(R.dimen.md3_spacing_medium),
            (int) getResources().getDimension(R.dimen.md3_spacing_small)
        );
        commandInputEdit.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ));
        
        // Handle keyboard action (Enter key)
        commandInputEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                sendCommand();
                return true;
            }
            return false;
        });
        
        inputContainer.addView(commandInputEdit);
        
        // Create send button with MD3 icon button style
        sendButton = new ImageButton(this);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setBackgroundResource(R.drawable.md3_icon_button_background);
        sendButton.setContentDescription("Send command");
        sendButton.setImageTintList(ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.md3_primary)
        ));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            (int) getResources().getDimension(R.dimen.md3_touch_target_min),
            (int) getResources().getDimension(R.dimen.md3_touch_target_min)
        );
        int buttonMargin = (int) getResources().getDimension(R.dimen.md3_spacing_small);
        buttonParams.setMargins(buttonMargin, 0, 0, 0);
        sendButton.setLayoutParams(buttonParams);
        sendButton.setOnClickListener(v -> sendCommand());
        
        inputContainer.addView(sendButton);
        
        // Create clear button with MD3 style
        clearButton = new ImageButton(this);
        clearButton.setImageResource(R.drawable.ic_clear);
        clearButton.setBackgroundResource(R.drawable.md3_icon_button_background);
        clearButton.setContentDescription("Clear terminal");
        clearButton.setImageTintList(ColorStateList.valueOf(
            ContextCompat.getColor(this, R.color.md3_on_surface_variant)
        ));
        clearButton.setLayoutParams(buttonParams);
        clearButton.setOnClickListener(v -> clearTerminal());
        
        inputContainer.addView(clearButton);
        
        rootContainer.addView(inputContainer);
        
        // Set content view
        setContentView(rootContainer);
    }
    
    /**
     * Copy rish shell files from resources to app's private directory
     * and start the shell process
     */
    private void copyRishFilesAndStartShell() {
        appendOutput("=== VibeX Rish Shell ===\n");
        appendOutput("Initializing shell environment...\n\n");
        
        // Copy rish shell script
        boolean rishCopied = copyFileFromResources("rish", "rish");
        
        // Copy rish Shizuku DEX file
        boolean dexCopied = copyFileFromResources("rish_shizuku_dex", "rish_shizuku.dex");
        
        if (rishCopied && dexCopied) {
            appendOutput("Shell files copied successfully\n");
            // Start the rish shell
            startRishShell();
        } else {
            appendOutput("Failed to copy shell files\n");
        }
    }
    
    /**
     * Copy a file from raw resources to the app's files directory
     */
    private boolean copyFileFromResources(String resourceName, String outputName) {
        try {
            // Find the resource ID
            int resourceId = getResources().getIdentifier(resourceName, "raw", getPackageName());
            
            if (resourceId == 0) {
                appendOutput("Resource not found: " + resourceName + "\n");
                return false;
            }
            
            // Open the resource as an input stream
            InputStream inputStream = getResources().openRawResource(resourceId);
            
            // Create output file
            FileOutputStream outputStream = openFileOutput(outputName, Context.MODE_PRIVATE);
            
            // Copy the file
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            outputStream.close();
            inputStream.close();
            
            // For the rish script, set executable permissions
            if (outputName.equals("rish")) {
                File rishFile = new File(getFilesDir(), "rish");
                rishFile.setExecutable(true);
                rishFile.setReadable(true);
                rishFile.setWritable(true);
                
                // Also try chmod via Runtime
                try {
                    Runtime.getRuntime().exec("chmod 755 " + rishFile.getAbsolutePath());
                } catch (Exception e) {
                    appendOutput("chmod attempt: " + e.getMessage() + "\n");
                }
            }
            
            appendOutput("Copied: " + outputName + " (" + totalBytes + " bytes)\n");
            return true;
            
        } catch (Exception e) {
            appendOutput("Error copying " + outputName + ": " + e.getMessage() + "\n");
            android.util.Log.e(TAG, "Failed to copy " + outputName, e);
            return false;
        }
    }
    
    /**
     * Start the rish shell process
     */
    private void startRishShell() {
        try {
            File rishFile = new File(getFilesDir(), "rish");
            File dexFile = new File(getFilesDir(), "rish_shizuku.dex");
            
            appendOutput("Verifying shell files...\n");
            appendOutput(" - Shell script: " + (rishFile.exists() ? "OK" : "MISSING") + 
                        " (" + rishFile.length() + " bytes)\n");
            appendOutput(" - DEX library: " + (dexFile.exists() ? "OK" : "MISSING") + 
                        " (" + dexFile.length() + " bytes)\n");
            
            if (!rishFile.exists() || !dexFile.exists()) {
                appendOutput("Shell files missing, cannot start\n");
                return;
            }
            
            appendOutput("Starting rish shell via sh...\n");
            
            // Create process with sh and the rish script
            ProcessBuilder processBuilder = new ProcessBuilder("sh", rishFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            
            // Set CLASSPATH environment variable for DEX execution
            Map<String, String> env = processBuilder.environment();
            env.put("CLASSPATH", dexFile.getAbsolutePath());
            
            // Start the process
            shellProcess = processBuilder.start();
            
            // Get input/output streams
            shellInputStream = new DataOutputStream(shellProcess.getOutputStream());
            shellOutputStream = shellProcess.getInputStream();
            
            appendOutput("Shell initialized successfully\n");
            appendOutput("Ready for commands...\n\n");
            
            // Start thread to read output
            executorService.execute(new ShellOutputReader());
            
        } catch (Exception e) {
            appendOutput("Error starting shell: " + e.getMessage() + "\n");
            android.util.Log.e(TAG, "Shell startup failed", e);
        }
    }
    
    /**
     * Send a command to the shell
     */
    private void sendCommand() {
        String command = commandInputEdit.getText().toString().trim();
        if (command.isEmpty()) {
            return;
        }
        
        // Add command to output with prompt
        appendOutput("$ " + command + "\n");
        
        // Clear input
        commandInputEdit.setText("");
        
        // Send command to shell
        if (shellInputStream != null) {
            try {
                shellInputStream.writeBytes(command + "\n");
                shellInputStream.flush();
            } catch (IOException e) {
                appendOutput("Error sending command: " + e.getMessage() + "\n");
            }
        }
    }
    
    /**
     * Clear the terminal output
     */
    private void clearTerminal() {
        terminalOutputView.setText("");
        appendOutput("Terminal cleared\n");
    }
    
    /**
     * Append text to the terminal output TextView
     */
    private void appendOutput(final String text) {
        uiHandler.post(() -> {
            terminalOutputView.append(text);
            // Auto-scroll to bottom
            uiHandler.post(() -> {
                terminalScrollView.post(() -> {
                    terminalScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                });
            });
        });
    }
    
    /**
     * Copy all terminal output to clipboard
     */
    private void copyAllOutputToClipboard() {
        String output = terminalOutputView.getText().toString();
        if (!output.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(output);
            Toast.makeText(this, "Output copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Called from native code to show welcome dialog
     * This method is invoked by the MitreGramNative library
     */
    public void showDialogFromNative() {
        uiHandler.post(() -> {
            showWelcomeDialog();
        });
    }
    
    /**
     * Show the Material Design 3 styled welcome dialog
     */
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MD3_DialogTheme);
        builder.setTitle("Welcome to VibeX Rish Shell");
        builder.setMessage("You have successfully loaded the MitreGram native protocol.\n\n" +
                          "This shell provides full Shizuku integration for advanced terminal operations.\n\n" +
                          "Type 'help' to see available commands.");
        builder.setPositiveButton("Get Started", (dialog, which) -> {
            appendOutput("\nWelcome! Type 'help' to begin.\n\n");
        });
        builder.setCancelable(true);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Style the dialog buttons
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                ContextCompat.getColor(this, R.color.md3_primary)
            );
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Shutdown executor service
        executorService.shutdownNow();
        
        // Destroy shell process when activity is destroyed
        if (shellProcess != null) {
            shellProcess.destroy();
        }
    }
    
    /**
     * Thread to read shell output asynchronously
     */
    private class ShellOutputReader implements Runnable {
        @Override
        public void run() {
            if (shellOutputStream == null) {
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(shellOutputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    appendOutput(line + "\n");
                }
            } catch (IOException e) {
                appendOutput("Shell output error: " + e.getMessage() + "\n");
            }
        }
    }
}
