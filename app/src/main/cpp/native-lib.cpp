#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/bitmap.h>
#include "include/SD.h"

#define RED_565(a)      ((((a) & 0x0000f800) >> 11) << 3)
#define GREEN_565(a)    ((((a) & 0x000007e0) >> 5) << 2)
#define BLUE_565(a)     ((((a) & 0x0000001f) << 3))

#define RED_8888(a)      (((a) & 0x00ff0000) >> 16)
#define GREEN_8888(a)    (((a) & 0x0000ff00) >> 8)
#define BLUE_8888(a)     (((a) & 0x000000ff))

#define RED_4444(a)      (((a) & 0x00000f00) >> 8)
#define GREEN_4444(a)    (((a) & 0x000000f0) >> 4)
#define BLUE_4444(a)     (((a) & 0x0000000f))

static char result_string[10000], *result_ptr;
static const char *TAG = "swift_decode";

static void resultCallback(int handle);
static int checkSDSet(int handle, int property, void *value);
static unsigned char *convertJByteArrayToChars(JNIEnv *env, jbyteArray byteArray);

extern "C" JNIEXPORT jint JNICALL
Java_com_chx_decoder_decoder_SwiftDecoder_createSD(JNIEnv *env, jobject instance) {
    int handle = SD_Create();
    if (handle == 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "failed to create SD!!!!error = %d", SD_GetLastError());
        return 0;
    }
    int ENA = 1;

    // Set the result handler callback address
    if ((0 == checkSDSet(handle, SD_PROP_CALLBACK_RESULT, (void *) &resultCallback))
        || (0 == checkSDSet(handle, SD_PROP_CALLBACK_PROGRESS, nullptr))
        || (0 == checkSDSet(handle, SD_PROP_C128_ENABLED, (void *) ENA))
        || (0 == checkSDSet(handle, SD_PROP_C39_ENABLED, (void *) ENA))
        || (0 == checkSDSet(handle, SD_PROP_UPC_ENABLED, (void *) ENA))
        || (0 == checkSDSet(handle, SD_PROP_DM_ENABLED, (void *) ENA))
        || (0 == checkSDSet(handle, SD_PROP_QR_ENABLED, (void *) ENA))
        || (0 == checkSDSet(handle, SD_PROP_AZ_ENABLED, (void *) (SD_CONST_INVERSE_ENABLED + SD_CONST_ENABLED)))) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "failed to set properties!!!!");
        return 0;
    }
    return handle;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chx_decoder_decoder_SwiftDecoder_decode(JNIEnv *env, jobject instance, jint handle,
                                                 jobject bitmap) {
    AndroidBitmapInfo info;
    int i = 0;
    uint32_t *rgb_buffer;
    static size_t image_size = 0;

    static unsigned char *ImageBuffer = nullptr;

    // 重置result_ptr的地址为result_string起始地址，清空result_string
    result_ptr = result_string;
    memset(result_string, 0, sizeof(result_string));

    //Open Image file --> Convert to Bitmap --> Convert to greyscale buffer --> send to decoder --> decode
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Can't get bitmap pixels");
        return 0;
    }

    if (AndroidBitmap_lockPixels(env, bitmap, (void **) &rgb_buffer) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Can't get bitmap pixels");
        return 0;
    }

    image_size = (size_t) info.width * info.height;
    ImageBuffer = (unsigned char *) malloc(image_size);

    if (ImageBuffer == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Can't allocate image buffer");
        AndroidBitmap_unlockPixels(env, bitmap);
        return 0;
    }

    switch (info.format) {
        case ANDROID_BITMAP_FORMAT_NONE:
            memcpy(ImageBuffer, rgb_buffer, image_size);
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_8888:
            for (i = 0; i < image_size; i++)
                ImageBuffer[i] = (GREEN_8888(rgb_buffer[i]) + RED_8888(rgb_buffer[i]) +
                                  BLUE_8888(rgb_buffer[i])) / 3;
            break;
        case ANDROID_BITMAP_FORMAT_RGB_565:
            for (i = 0; i < image_size; i++)
                ImageBuffer[i] = (RED_565(rgb_buffer[i]) + GREEN_565(rgb_buffer[i]) +
                                  BLUE_565(rgb_buffer[i])) / 3;
            break;
        case ANDROID_BITMAP_FORMAT_RGBA_4444:
            for (i = 0; i < image_size; i++)
                ImageBuffer[i] = (GREEN_4444(rgb_buffer[i]) + RED_4444(rgb_buffer[i]) +
                                  BLUE_4444(rgb_buffer[i])) / 3;
            break;
        default:
            __android_log_print(ANDROID_LOG_INFO, TAG, "Bitmap Format unknown");
            AndroidBitmap_unlockPixels(env, bitmap);
            free(ImageBuffer);
            return 0;
    }

    AndroidBitmap_unlockPixels(env, bitmap);

    /* Set image related properties to SwiftDecoder library*/
    if ((0 == checkSDSet(handle, SD_PROP_IMAGE_HEIGHT, (void *) info.height))
        || (0 == checkSDSet(handle, SD_PROP_IMAGE_LINE_DELTA, (void *) info.width))
        || (0 == checkSDSet(handle, SD_PROP_IMAGE_POINTER, (void *) ImageBuffer))
        || (0 == checkSDSet(handle, SD_PROP_IMAGE_WIDTH, (void *) info.width))) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Problem passing image to SD");
        free(ImageBuffer);
        return 0;
    }

    if (0 == SD_Decode(handle)) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "SD_Decode failed, Error = %d",
                            SD_GetLastError());
        free(ImageBuffer);
        return 0;
    }

    free(ImageBuffer);
    return 1;

}

extern "C" JNIEXPORT jstring JNICALL
Java_com_chx_decoder_decoder_SwiftDecoder_getResult(JNIEnv *env, jobject instance) {
    return env->NewStringUTF(result_string);
}

extern "C" JNIEXPORT jint JNICALL
Java_com_chx_decoder_decoder_SwiftDecoder_destroySD(JNIEnv *env, jobject instance, jint handle) {
    if (handle != 0) {
        int result = SD_Destroy(handle);
        if (result == 0) {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "failed to destroy SD!!!!error = %d", SD_GetLastError());
            return 0;
        }
    } else {
        __android_log_print(ANDROID_LOG_WARN, TAG, "handle is 0 when destroy!!!!error = %d", SD_GetLastError());
        return 0;
    }
    return 1;
}

static void resultCallback(int handle) {
    int length;
    SD_STRUCT_POINT center;
    SD_STRUCT_BOUNDS bounds;

    SD_Get(handle, SD_PROP_RESULT_LENGTH, &length);
    SD_Get(handle, SD_PROP_RESULT_BOUNDS, &bounds);
//    SD_Get(handle, SD_PROP_RESULT_STRING, result_ptr);
    SD_Get(handle, SD_PROP_RESULT_CENTER, &center);

    result_ptr += sprintf(result_ptr,
                          "{\"length\":%d,"
                          "\"center\":{\"x\":%d,\"y\":%d},"
                          "\"bounds\":"
                          "{\"topLeft\":{\"x\":%d,\"y\":%d},"
                          "\"topRight\":{\"x\":%d,\"y\":%d},"
                          "\"bottomRight\":{\"x\":%d,\"y\":%d},"
                          "\"bottomLeft\":{\"x\":%d,\"y\":%d}},"
                          "\"result\":\"", length, center.X, center.Y,
                          bounds.Point[0].X, bounds.Point[0].Y, bounds.Point[1].X, bounds.Point[1].Y,
                          bounds.Point[2].X, bounds.Point[2].Y, bounds.Point[3].X, bounds.Point[3].Y);
    SD_Get(handle, SD_PROP_RESULT_STRING, result_ptr);
    result_ptr += length;
    result_ptr += sprintf(result_ptr, "\"}\n");
}

unsigned char *convertJByteArrayToChars(JNIEnv *env, jbyteArray byteArray) {
    unsigned char *chars = nullptr;
    jbyte *bytes;
    bytes = env->GetByteArrayElements(byteArray, nullptr);
    auto chars_len = (size_t) env->GetArrayLength(byteArray);
    chars = new unsigned char[chars_len + 1];
    memset(chars, 0, chars_len + 1);
    memcpy(chars, bytes, chars_len);
    chars[chars_len] = 0;
    env->ReleaseByteArrayElements(byteArray, bytes, 0);
    return chars;
}

int checkSDSet(int handle, int property, void *value) {
    int result = SD_Set(handle, property, value);
    if (result == 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "set property of %d failed, error = %d",
                            property, SD_GetLastError());
    }
    return result;
}