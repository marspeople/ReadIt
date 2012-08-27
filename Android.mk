LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_STATIC_JAVA_LIBRARIES := gson

LOCAL_SRC_FILES := $(call all-java-files-under,src)
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

LOCAL_PACKAGE_NAME := ReadIt

LOCAL_PROGUARD_FLAG_FILES := proguard.cfg  

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson:lib/gson-1.7.1.jar

include $(BUILD_MULTI_PREBUILT)

