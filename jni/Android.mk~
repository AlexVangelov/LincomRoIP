# $Id$
PJPROJECT_DIR := /home/vangelov/pjandroid/android2

LOCAL_PATH	:= $(call my-dir)
include $(CLEAR_VARS)

# Get PJ build settings
include $(PJPROJECT_DIR)/build.mak
include $(PJDIR)/build/common.mak

# Path to SWIG
MY_SWIG		:= swig


# Constants
MY_JNI_WRAP	:= lincomroip_wrap.cpp
MY_JNI_DIR	:= jni

# Android build settings
LOCAL_MODULE    := lincomroip
LOCAL_CFLAGS    := -Werror $(APP_CFLAGS) -frtti
LOCAL_LDFLAGS   := $(APP_LDFLAGS)
LOCAL_LDLIBS    := $(MY_MODULES) $(APP_LDLIBS)
LOCAL_SRC_FILES := $(MY_JNI_WRAP) lincomroip.cpp

# Invoke SWIG
$(MY_JNI_DIR)/$(MY_JNI_WRAP):
	@echo "Invoking SWIG..."
	$(MY_SWIG) -c++ -o $(MY_JNI_DIR)/$(MY_JNI_WRAP) -package com.lincomengineering.lincomroip -outdir src/com/lincomengineering/lincomroip -java $(MY_JNI_DIR)/lincomroip.i

.PHONY: $(MY_JNI_DIR)/$(MY_JNI_WRAP)

include $(BUILD_SHARED_LIBRARY)
