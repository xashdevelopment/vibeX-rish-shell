/**
 * MitreGram Native Library Reconstruction
 * Originally named "libQASM.so" - A Telegram client native component
 * 
 * This library provides:
 * - JNI initialization with welcome dialog callback
 * - Dialog interaction via qers class
 * - Native callback system to Java
 * 
 * Built with Android NDK C++
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <stdlib.h>

// Logging macros
#define LOG_TAG "MitreGramNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

// Global references for JNI callbacks
static JavaVM* g_jvm = nullptr;
static jobject g_callback_object = nullptr;
static jmethodID g_show_dialog_method = nullptr;

// ============================================
// Forward declarations
// ============================================
void show_welcome_dialog(JNIEnv* env, jobject activity);
void native_callback(JNIEnv* env, const char* method_name, const char* param);

// ============================================
// JNI OnLoad - Library Initialization
// ============================================
extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_jvm = vm;
    
    LOGI("MitreGram native library loaded");
    LOGD("Build info: NDK compiled native library");
    
    return JNI_VERSION_1_6;
}

// ============================================
// JNI Method Implementations
// ============================================

extern "C" JNIEXPORT void JNICALL
Java_com_vibe_termplugin_MitreGramNative_nativeInit(JNIEnv* env, jobject thiz) {
    LOGD("nativeInit called");
    
    // Store callback reference
    g_callback_object = env->NewGlobalRef(thiz);
    
    // Get the show dialog method ID
    jclass clazz = env->GetObjectClass(thiz);
    g_show_dialog_method = env->GetMethodID(clazz, "onShowWelcomeDialog", "()V");
    
    LOGD("nativeInit completed, method ID acquired");
}

extern "C" JNIEXPORT void JNICALL
Java_com_vibe_termplugin_MitreGramNative_nativeCleanup(JNIEnv* env, jobject thiz) {
    LOGD("Cleaning up native library");
    
    if (g_callback_object != nullptr) {
        env->DeleteGlobalRef(g_callback_object);
        g_callback_object = nullptr;
    }
    
    g_show_dialog_method = nullptr;
    LOGD("Native cleanup complete");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_vibe_termplugin_MitreGramNative_nativeGetLibraryName(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("MitreGramNative");
}

// Native logging function
extern "C" JNIEXPORT void JNICALL
Java_com_vibe_termplugin_MitreGramNative_nativeLog(JNIEnv* env, jobject thiz, jstring message) {
    const char* msg = env->GetStringUTFChars(message, nullptr);
    LOGI("Native Log: %s", msg);
    env->ReleaseStringUTFChars(message, msg);
}

// Show message from native code
extern "C" JNIEXPORT void JNICALL
Java_com_vibe_termplugin_MitreGramNative_nativeShowMessage(JNIEnv* env, jobject thiz, jstring title, jstring message) {
    const char* title_str = env->GetStringUTFChars(title, nullptr);
    const char* msg_str = env->GetStringUTFChars(message, nullptr);
    
    LOGD("Showing message: %s - %s", title_str, msg_str);
    LOGI("Message: %s", msg_str);
    
    env->ReleaseStringUTFChars(title, title_str);
    env->ReleaseStringUTFChars(message, msg_str);
}

// ============================================
// Qers Class Implementation (C++)
// ============================================

namespace qers {
    
    /**
     * Qers - Dialog action handler
     * mode = 0: Exit application
     * mode != 0: Dismiss dialog
     */
    class QersAction {
    private:
        int mode;
        
    public:
        QersAction(int m) : mode(m) {
            LOGD("QersAction created with mode: %d", mode);
        }
        
        ~QersAction() {
            LOGD("QersAction destroyed");
        }
        
        void execute(JNIEnv* env) {
            if (mode == 0) {
                LOGD("Qers mode 0: Application exit requested");
                // Send callback for Java to handle System.exit()
                if (g_callback_object != nullptr) {
                    jstring jmsg = env->NewStringUTF("EXIT_APP|||0");
                    jclass clazz = env->GetObjectClass(g_callback_object);
                    jmethodID callback = env->GetMethodID(clazz, "handleQersAction", "(I)V");
                    env->CallVoidMethod(g_callback_object, callback, mode);
                    env->DeleteLocalRef(jmsg);
                }
            } else {
                LOGD("Qers mode %d: Dialog dismiss", mode);
            }
        }
        
        int getMode() const {
            return mode;
        }
    };
    
    /**
     * Create a qers action from native code
     */
    extern "C" JNIEXPORT jobject JNICALL
    Java_com_vibe_termplugin_MitreGramNative_nativeCreateQers(JNIEnv* env, jobject thiz, jint mode) {
        // Create a QersAction instance
        // In real implementation, this would return a Java object
        // For now, we just log and return null (Java handles object creation)
        LOGD("Creating qers with mode: %d", mode);
        return nullptr;
    }
    
}

// ============================================
// Welcome Dialog Implementation
// ============================================

void show_welcome_dialog(JNIEnv* env, jobject activity) {
    LOGD("Preparing to show welcome dialog");
    
    // Call the onShowWelcomeDialog method in Java
    if (g_show_dialog_method != nullptr) {
        LOGD("Calling onShowWelcomeDialog in Java");
        env->CallVoidMethod(activity, g_show_dialog_method);
        LOGD("Welcome dialog callback sent");
    } else {
        LOGE("show_dialog_method is null, cannot show dialog");
    }
}
