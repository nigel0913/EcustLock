LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := EcustLock
LOCAL_SRC_FILES := EcustLock.cpp

include $(BUILD_SHARED_LIBRARY)
