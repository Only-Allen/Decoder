#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <unistd.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <malloc.h>

#define RED_565(a)      ((((a) & 0x0000f800) >> 11) << 3)
#define GREEN_565(a)    ((((a) & 0x000007e0) >> 5) << 2)
#define BLUE_565(a)     ((((a) & 0x0000001f) << 3))

#define RED_8888(a)      (((a) & 0x00ff0000) >> 16)
#define GREEN_8888(a)    (((a) & 0x0000ff00) >> 8)
#define BLUE_8888(a)     (((a) & 0x000000ff))

#define RED_4444(a)      (((a) & 0x00000f00) >> 8)
#define GREEN_4444(a)    (((a) & 0x000000f0) >> 4)
#define BLUE_4444(a)     (((a) & 0x0000000f))

static const char* TAG = "native_decode";

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_chx_decoder_bitmap_BitmapTools_getBytesToDecode(JNIEnv *env, jclass type, jobject bitmap) {
    AndroidBitmapInfo info;
    int i = 0;
    uint32_t *rgb_buffer;
    static size_t image_size = 0;

    static unsigned char *ImageBuffer = NULL;

    // 重置result_ptr的地址为result_string起始地址，清空result_string

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

    if (ImageBuffer == NULL) {
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
    jbyteArray array = env->NewByteArray(image_size);
    env->SetByteArrayRegion(array, 0, image_size, (jbyte*) ImageBuffer);
    return array;
}
extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_chx_decoder_bitmap_BitmapTools_bmpFileToBytes(JNIEnv *env, jclass type, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    FILE *file = NULL;
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "file path: %s", path);
    file = fopen ("/storage/sdcard0/Tencent/QQfile_recv/cam.BMP", "rb+");

    if (NULL == file)
    {
        __android_log_print(ANDROID_LOG_DEBUG, TAG, "Failed to open file\n");
        return 0;
    }

    static unsigned char *ImageBuffer;
    ImageBuffer = (unsigned char*)malloc(832*640);
    fseek (file, 1078L, 0);  //跳过1078字节的BMP文件头
    fread (ImageBuffer, (832*640-1078), 1, file);   //从文件读入图片数据，字节大小尺寸等于解析度
    fclose (file);
    __android_log_print(ANDROID_LOG_DEBUG, TAG, "OK to read file");
    env->ReleaseStringUTFChars(path_, path);
    jbyteArray array = env->NewByteArray(832*640);
    env->SetByteArrayRegion(array, 0, 832*640, (jbyte*) ImageBuffer);
    return array;
}