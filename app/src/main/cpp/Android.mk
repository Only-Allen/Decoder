# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE    := libansx
LOCAL_SRC_FILES := $(LOCAL_PATH)/decoder/armeabi/libansx.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := native-lib
LOCAL_SRC_FILES := native-lib.c
LOCAL_LDLIBS    := -landroid -llog -ldl -lz -ljnigraphics -L$(LOCAL_PATH)
APP_CPPFLAGS += -fpermissive
LOCAL_STATIC_LIBRARIES := libansx
include $(BUILD_SHARED_LIBRARY)
