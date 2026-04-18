#include <jni.h>
#include <string>
#include <unistd.h>
#include <android/log.h>

#define LOG_TAG "HuabuNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_huabu_app_core_native_HuabuNative_getPageSize(JNIEnv* env, jobject /* this */) {
    long page_size = sysconf(_SC_PAGESIZE);
    LOGI("System page size: %ld bytes", page_size);
    std::string result = "Page size: " + std::to_string(page_size) + " bytes";
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_huabu_app_core_native_HuabuNative_is16KBAligned(JNIEnv* env, jobject /* this */) {
    long page_size = sysconf(_SC_PAGESIZE);
    return page_size >= 16384 ? JNI_TRUE : JNI_FALSE;
}
