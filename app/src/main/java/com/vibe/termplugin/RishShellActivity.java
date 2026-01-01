package com.vibe.termplugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Pure Android SDK implementation - NO EXTERNAL DEPENDENCIES
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize UI handler and executor
        uiHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newFixedThreadPool(2);

        // Initialize ADRT logging with the debugger package
        ADRTLogCatReader.onContext(this, "com.aide.ui.crustacean");

        // Setup the custom UI
        setupUI();

        // Show welcome dialog directly
        showWelcomeDialog();

        // Copy shell files and start the shell
        executorService.execute(this::copyRishFilesAndStartShell);
    }

    /**
     * Setup custom user interface using standard Android views
     * NO EXTERNAL LIBRARIES - Pure Android SDK implementation
     */
    private void setupUI() {
        // Create root container with custom background
        rootContainer = new LinearLayout(this);
        rootContainer.setOrientation(LinearLayout.VERTICAL);
        rootContainer.setBackgroundColor(getColorFromTheme(android.R.attr.windowBackground));
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        );
        rootContainer.setLayoutParams(rootParams);

        // Create header
        headerContainer = new LinearLayout(this);
        headerContainer.setOrientation(LinearLayout.HORIZONTAL);
        headerContainer.setGravity(Gravity.CENTER_VERTICAL);
        headerContainer.setBackgroundColor(getColorFromTheme(android.R.attr.colorPrimary));
        int headerPadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()
        );
        headerContainer.setPadding(headerPadding, headerPadding, headerPadding, headerPadding);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int headerMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
        );
        headerParams.bottomMargin = headerMargin;
        headerContainer.setLayoutParams(headerParams);

        // Create title TextView
        titleView = new TextView(this);
        titleView.setText("VibeX Rish Shell");
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        titleView.setTextColor(getColorFromTheme(android.R.attr.textColorPrimary));
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        // Add title to header
        headerContainer.addView(titleView);
        rootContainer.addView(headerContainer);

        // Create scrollable terminal output area
        terminalScrollView = new ScrollView(this);
        terminalScrollView.setBackgroundColor(0xFF1E1E1E);
        terminalScrollView.setClipToPadding(false);
        int terminalPadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
        );
        terminalScrollView.setPadding(terminalPadding, terminalPadding, terminalPadding, terminalPadding);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        );
        int scrollMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
        );
        int smallMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()
        );
        scrollParams.setMargins(scrollMargin, 0, scrollMargin, smallMargin);
        terminalScrollView.setLayoutParams(scrollParams);

        // Create terminal container for output
        terminalContainer = new LinearLayout(this);
        terminalContainer.setOrientation(LinearLayout.VERTICAL);

        // Create terminal output TextView
        terminalOutputView = new TextView(this);
        terminalOutputView.setTextColor(0xFF00FF00);
        terminalOutputView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        terminalOutputView.setTypeface(android.graphics.Typeface.MONOSPACE);
        float lineSpacing = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()
        );
        terminalOutputView.setLineSpacing(lineSpacing, 1.0f);
        terminalOutputView.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Enable long press to copy
        terminalOutputView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyAllOutputToClipboard();
                return true;
            }
        });

        terminalContainer.addView(terminalOutputView);
        terminalScrollView.addView(terminalContainer);
        rootContainer.addView(terminalScrollView);

        // Create input container
        inputContainer = new LinearLayout(this);
        inputContainer.setOrientation(LinearLayout.HORIZONTAL);
        inputContainer.setGravity(Gravity.CENTER_VERTICAL);
        inputContainer.setBackgroundColor(getColorFromTheme(android.R.attr.colorBackground));
        int inputPadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()
        );
        inputContainer.setPadding(inputPadding, inputPadding, inputPadding, inputPadding);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.setMargins(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()),
            0,
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()),
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())
        );
        inputContainer.setLayoutParams(inputParams);

        // Create command input EditText
        commandInputEdit = new EditText(this);
        commandInputEdit.setHint("Enter command...");
        int hintColor = getColorFromTheme(android.R.attr.textColorSecondary);
        commandInputEdit.setHintTextColor(hintColor);
        commandInputEdit.setTextColor(getColorFromTheme(android.R.attr.textColorPrimary));
        commandInputEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        commandInputEdit.setTypeface(android.graphics.Typeface.MONOSPACE);
        commandInputEdit.setBackgroundResource(android.R.drawable.editbox_background);
        int editPadding = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()
        );
        commandInputEdit.setPadding(editPadding, editPadding, editPadding, editPadding);
        commandInputEdit.setLayoutParams(new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ));

        // Handle keyboard action (Enter key)
        commandInputEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    sendCommand();
                    return true;
                }
                return false;
            }
        });

        inputContainer.addView(commandInputEdit);

        // Create send button
        sendButton = new ImageButton(this);
        sendButton.setImageResource(android.R.drawable.ic_menu_send);
        sendButton.setBackgroundResource(android.R.drawable.btn_default);
        sendButton.setContentDescription("Send command");
        int touchTarget = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()
        );
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            touchTarget,
            touchTarget
        );
        int buttonMargin = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()
        );
        buttonParams.setMargins(buttonMargin, 0, 0, 0);
        sendButton.setLayoutParams(buttonParams);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand();
            }
        });

        inputContainer.addView(sendButton);

        // Create clear button
        clearButton = new ImageButton(this);
        clearButton.setImageResource(android.R.drawable.ic_menu_delete);
        clearButton.setBackgroundResource(android.R.drawable.btn_default);
        clearButton.setContentDescription("Clear terminal");
        clearButton.setLayoutParams(buttonParams);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearTerminal();
            }
        });

        inputContainer.addView(clearButton);

        rootContainer.addView(inputContainer);

        // Set content view
        setContentView(rootContainer);
    }

    /**
     * Get color from theme without AndroidX
     */
    private int getColorFromTheme(int attr) {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
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
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                terminalOutputView.append(text);
                // Auto-scroll to bottom
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        terminalScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                terminalScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                });
            }
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
     * Show welcome dialog
     * This method was previously invoked by the native library
     */
    public void showDialogFromNative() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                showWelcomeDialog();
            }
        });
    }

    /**
     * Show the styled welcome dialog
     */
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to VibeX Rish Shell");
        builder.setMessage("You have successfully loaded the VibeX terminal.\n\n" +
                          "This shell provides full Shizuku integration for advanced terminal operations.\n\n" +
                          "Type 'help' to see available commands.");
        builder.setPositiveButton("Get Started", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appendOutput("\nWelcome! Type 'help' to begin.\n\n");
            }
        });
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
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
