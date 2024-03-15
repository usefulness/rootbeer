/****************************************************************************
 * File:   toolChecker.cpp
 * Author: Matthew Rollings
 * Date:   19/06/2015
 *
 * Description : Root checking JNI NDK code
 *
 ****************************************************************************/

/****************************************************************************
 *>>>>>>>>>>>>>>>>>>>>>>>>> System Includes <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 ****************************************************************************/

// Android headers
#include <jni.h>
#include <android/log.h>

// String / file headers
#include <string.h>
#include <stdio.h>
#include "io_github_usefulness_rootbeer_RootBeerNative.h"
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <pwd.h>
#include <grp.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/un.h>

/****************************************************************************
 *>>>>>>>>>>>>>>>>>>>>>>>>>> User Includes <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 ****************************************************************************/

/****************************************************************************
 *>>>>>>>>>>>>>>>>>>>>>>>>>> Constant Macros <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 ****************************************************************************/

// LOGCAT
#define  LOG_TAG    "RootBeerLib"
#define  LOGD(...)  if (DEBUG) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__);

#define BUFSIZE 1024

/* Set to 1 to enable debug log traces. */
static int DEBUG = 1;

extern "C" {

/*****************************************************************************
 * Description: Sets if we should log debug messages
 *
 * Parameters: env - Java environment pointer
 *      this - javaobject
 * 	bool - true to log debug messages
 *
 *****************************************************************************/
void
Java_io_github_usefulness_rootbeer_RootBeerNative_setLogDebugMessages(JNIEnv *env, jobject thiz,
                                                                      jboolean debug) {
    if (debug) {
        DEBUG = 1;
    } else {
        DEBUG = 0;
    }
}


/*****************************************************************************
 * Description: Checks if a file exists
 *
 * Parameters: fname - filename to check
 *
 * Return value: 0 - non-existant / not visible, 1 - exists
 *
 *****************************************************************************/
int exists(const char *fname) {
    FILE *file;
    if ((file = fopen(fname, "r"))) {
        LOGD("LOOKING FOR BINARY: %s PRESENT!!!", fname);
        fclose(file);
        return 1;
    }
    LOGD("LOOKING FOR BINARY: %s Absent :(", fname);
    return 0;
}


/*****************************************************************************
 * Description: Check file stat
 *
 * Parameters: fname - filename to check
 *
 * Return value: 0 - non-existant / not visible, 1 - exists
 *
 *****************************************************************************/
extern int stat(const char *, struct stat *);

/*****************************************************************************
 * Description: Parsing the mode_t structure
 *
 * Parameters: mode - mode_t structure, buf - Parsing result
 *
 *****************************************************************************/
void strmode(mode_t mode, char *buf) {
    const char chars[] = "rwxrwxrwx";

    for (size_t i = 0; i < 9; i++) {
        buf[i] = (mode & (1 << (8 - i))) ? chars[i] : '-';
    }

    buf[9] = '\0';
}

int checkFileStat(const char *fname) {
    struct stat file_info = {0};

    if (!fname) {
        return 0;
    }

    if (stat(fname, &file_info) == -1) {
        return 0;
    }

    return 1;
}

/*****************************************************************************
 * Description: Check the Unix Domain Socket used by Magisk
 *
 * Parameters: none
 *
 * Return value: 0 - non-existant / not visible, 1 or more - exists
 *
 *****************************************************************************/
jboolean
Java_io_github_usefulness_rootbeer_RootBeerNative_checkForMagiskUDS(JNIEnv *env, jobject thiz) {
    int uds_detect_count = 0;
    int magisk_file_detect_count = 0;

    // Magisk UDS(Unix Domain Socket) Detection Method.
    // The unix domain socket is typically used for local communications, ie IPC.
    // At least Android 8.0 can look up unix domain sockets.
    // You need to be sure that you can query the unix domain socket on Android 9.0 or later.
    FILE *fh = fopen("/proc/net/unix", "r");
    if (fh) {
        for (;;) {
            char filename[BUFSIZE] = {0};
            uint32_t a, b, c, d, e, f, g;
            int count = fscanf(fh, "%x: %u %u %u %u %u %u ",
                               &a, &b, &c, &d, &e, &f, &g);
            if (count == 0) {
                if (!fgets(filename, BUFSIZE, fh)) {
                    break;
                }
                continue;
            } else if (count == -1) {
                break;
            } else if (!fgets(filename, BUFSIZE, fh)) {
                break;
            }

            magisk_file_detect_count += checkFileStat("/dev/.magisk.unblock");

            magisk_file_detect_count += checkFileStat("/sbin/magiskinit");
            magisk_file_detect_count += checkFileStat("/sbin/magisk");
            magisk_file_detect_count += checkFileStat("/sbin/.magisk");

            magisk_file_detect_count += checkFileStat("/data/adb/magisk.img");
            magisk_file_detect_count += checkFileStat("/data/adb/magisk.db");
            magisk_file_detect_count += checkFileStat("/data/adb/.boot_count");
            magisk_file_detect_count += checkFileStat("/data/adb/magisk_simple");
            magisk_file_detect_count += checkFileStat("/data/adb/magisk");

            magisk_file_detect_count += checkFileStat("/cache/.disable_magisk");
            magisk_file_detect_count += checkFileStat("/cache/magisk.log");

            magisk_file_detect_count += checkFileStat("/init.magisk.rc");

            // The name of the unix domain socket created by the daemon is prefixed with an @ symbol.
            char *ptr = strtok(filename, "@");
            if (ptr) {
                // On Android, the / character, space, and dot characters are the names of the normal unix domain sockets.
                if (strstr(ptr, "/")) {
                } else if (strstr(ptr, " ")) {
                } else if (strstr(ptr, ".")) {
                } else { // Magisk replaces the name of the unix domain socket with a random string of 32 digits.
                    int len = strlen(ptr);
                    if (len >= 32) {
                        // Magisk was detected.
                        uds_detect_count++;
                    }
                }
            }
        }

        fclose(fh);
    }

    LOGD("uds_detect_count=%d", uds_detect_count)
    LOGD("magisk_file_detect_count=%d", magisk_file_detect_count)
    if (uds_detect_count == 0 && magisk_file_detect_count == 0) {
        return 0;
    } else {
        return 1;
    }
}

extern "C"
/*****************************************************************************
 * Description: Checks for root binaries
 *
 * Parameters: env - Java environment pointer
 *      thiz - javaobject
 *
 * Return value: int number of su binaries found
 *
 *****************************************************************************/
jboolean Java_io_github_usefulness_rootbeer_RootBeerNative_checkForRoot(JNIEnv *env, jobject thiz,
                                                                    jobjectArray pathsArray) {

    int binariesFound = 0;

    int stringCount = (env)->GetArrayLength(pathsArray);

    for (int i = 0; i < stringCount; i++) {
        jstring string = (jstring) (env)->GetObjectArrayElement(pathsArray, i);
        const char *pathString = (env)->GetStringUTFChars(string, 0);

        binariesFound += exists(pathString);

        (env)->ReleaseStringUTFChars(string, pathString);
    }

    return binariesFound > 0;
}
}
