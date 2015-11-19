#include "com_schspa_SysfsInterface.h"
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <dirent.h>
#include <string.h>
#include <sys/stat.h>
#include <stdlib.h>

#include <android/log.h>

#define  LOG_TAG    "sysfs_interface"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)


void printdir(char *dir, int depth)
{
    DIR *dp;
    struct dirent *entry;
    struct stat statbuf;

    if ((dp = opendir(dir)) == NULL) {
        LOGD("Can`t open directory %s\n", dir);
        return ;
    }

    chdir(dir);
    while ((entry = readdir(dp)) != NULL) {
        lstat(entry->d_name, &statbuf);
        if (S_ISDIR(statbuf.st_mode)) {
            if (strcmp(entry->d_name, ".") == 0 ||
                strcmp(entry->d_name, "..") == 0 )
                continue;
            LOGD("%*s%s/\n", depth, "", entry->d_name);
            printdir(entry->d_name, depth+4);
        } else
            LOGD("%*s%s\n", depth, "", entry->d_name);
    }
    chdir("..");
    closedir(dp);
}

JNIEXPORT void JNICALL Java_com_schspa_SysfsInterface_test
        (JNIEnv * env, jobject obj) {
    printdir("/sys/module/kernel/parameters", 0);
}

//获取，返回 string []
JNIEXPORT jobjectArray JNICALL Java_com_schspa_SysfsInterface_getfile(JNIEnv * env,
                                                                 jobject obj, jstring str) {
    char * dir = (char *) (*env)->GetStringUTFChars(env, str, 0);
    DIR *dp;
    struct dirent *entry;
    struct stat statbuf;

    if ((dp = opendir(dir)) == NULL) {
        LOGD("Can`t open directory %s\n", dir);
        return NULL;
    }
    jobjectArray args = NULL;
    jsize size = 0;
    chdir(dir);
    while ((entry = readdir(dp)) != NULL) {
        lstat(entry->d_name, &statbuf);
        if (!S_ISDIR(statbuf.st_mode)) {
            size++;
        }
    }

    jclass objClass = (*env)->FindClass(env, "java/lang/String");
    args = (*env)->NewObjectArray(env, size, objClass, 0);
    jstring temp_str;
    int j = 0;
    closedir(dp);
    if ((dp = opendir(dir)) == NULL) {
        LOGD("Can`t open directory %s\n", dir);
        return NULL;
    }
    while ((entry = readdir(dp)) != NULL) {
        lstat(entry->d_name, &statbuf);
        if (!S_ISDIR(statbuf.st_mode)) {
            temp_str = (*env)->NewStringUTF(env, entry->d_name);
            (*env)->SetObjectArrayElement(env, args, j++, temp_str);
            if (j >= size)
                break;
        }
    }
    closedir(dp);
    return args;
}
/*
 * Class:     com_jnimobile_www_myjnidemo_MainActivity
 * Method:    getStringFromNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_schspa_SysfsInterface_read
  (JNIEnv * env, jobject obj, jstring str){
        int fd, len, i;
        char buf[20];
        char *path =  (char *) (*env)->GetStringUTFChars(env, str, 0);
        LOGD("read:%s", path);
        fd = open(path, O_RDONLY);
        if (fd <= 0)
            return NULL;
        len = read(fd, buf, sizeof(buf));
        if (len > 0) {
            buf[len] = '\0';
            for (i=0; i<len; i++) {
                if (buf[i] > 127 || buf[i] < 32)
                    buf[i] = '\0';
            }
            return (*env)->NewStringUTF(env, buf);
        } else {
            return (*env)->NewStringUTF(env, "No Data");
        }
  }

JNIEXPORT jboolean JNICALL Java_com_schspa_SysfsInterface_writeable
        (JNIEnv * env, jobject obj, jstring str) {
    int fd;
    char *path =  (char *) (*env)->GetStringUTFChars(env, str, 0);
    fd = open(path, O_RDWR);
    if (fd <= 0) {
        return 0;
    } else {
        return 1;
    }
}

JNIEXPORT jstring JNICALL Java_com_schspa_SysfsInterface_write
        (JNIEnv * env, jobject obj, jstring str, jstring wbuf){
    int fd, len, i;
    char *buf = (char *) (*env)->GetStringUTFChars(env, wbuf, 0);
    char *path =  (char *) (*env)->GetStringUTFChars(env, str, 0);
    len = (char *) (*env)->GetStringUTFLength(env, wbuf);
    fd = open(path, O_RDWR);
    if (fd <= 0) {
        LOGE("open RDWR failed");
        return NULL;
    }
    len = write(fd, buf, len);
    close(fd);
    if (len > 0) {
        return (*env)->NewStringUTF(env, "failed");
    } else {
        return (*env)->NewStringUTF(env, "sucess");
    }
}
