#include <string.h>
#include <stdlib.h>
#include<pthread.h>

#include <jni.h>

#include "crftagger.h"

static pthread_mutex_t lock;
static crfsuite_model_t *model = NULL;

static char* make_buffer(int size)
{
    char *dst = (char*)malloc(size);
    
    memset(dst, 0, size);

    return dst;
}

static char* duplicate_string(const char *src)
{
    char *dst = (char*)malloc(strlen(src) + 1);

    if (dst != NULL) {
        strcpy(dst, src);
    }

    return dst;
}

JNIEXPORT int JNICALL Java_CrfScalaJniImpl_loadModel(JNIEnv *env, jobject obj, jstring modelPath)
{
    int ret;

    pthread_mutex_lock(&lock);

    if (model) {
        SAFE_RELEASE(model);        
    }

    const char *nativeModelPath = (*env)->GetStringUTFChars(env, modelPath, NULL);

    if (nativeModelPath == NULL) {
        return 1;
    }

    /* Create a model instance corresponding to the model file */
    ret = crfsuite_create_instance_from_file(nativeModelPath, (void**)&model);

    if (ret) {
        return 2;
    }

    pthread_mutex_unlock(&lock);

    return 0;
}

JNIEXPORT void JNICALL Java_CrfScalaJniImpl_unloadModel(JNIEnv *env, jobject obj)
{
    if (!model) {
        return;
    }

    if (model) {
        SAFE_RELEASE(model);        
    }
}

JNIEXPORT jstring JNICALL Java_CrfScalaJniImpl_tag(JNIEnv *env, jobject obj, jstring items)
{
    if (!model) {
        // TODO: throw
        return (*env)->NewStringUTF(env, "ERROR: No open model.");;
    }

    int ret;

    const char *nativeItems = (*env)->GetStringUTFChars(env, items, NULL);
    char *buffer = make_buffer(64 * 1000);

    jstring tags;

    ret = tag(nativeItems, model, buffer);

    if (ret) {
        // TODO: throw
        sprintf(buffer, "ERROR: Failed to tag (%d).", ret);
    }

    tags = (*env)->NewStringUTF(env, buffer);

force_exit:
    free(buffer);
    buffer = NULL;

    if (nativeItems) {
        (*env)->ReleaseStringUTFChars(env, items, nativeItems);
        nativeItems = NULL;
    }

    return tags;
} 
