LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := natives
LOCAL_SRC_FILES := natives.cpp

include $(BUILD_SHARED_LIBRARY)