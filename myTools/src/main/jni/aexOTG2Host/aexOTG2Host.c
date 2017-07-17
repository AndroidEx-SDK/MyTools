#include <netinet/in.h>
#include <stdio.h>      /*标准输入输出定义*/
#include <unistd.h>     /*Unix标准函数定义*/
#include <fcntl.h>      /*文件控制定义*/
#include <jni.h>

#define TAG "MainActivity"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) //
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__) //
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__) //
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__) //
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__) //

#define MAX_CMD_LEN 256
typedef unsigned char BYTE;

int HexEncodeGetRequiredLength(int nSrcLen) {
    return 2 * nSrcLen + 1;
}

int HexDecodeGetRequiredLength(int nSrcLen) {
    return nSrcLen / 2;
}

int HexEncode(const unsigned char *pbSrcData, int nSrcLen, char *szDest, int *pnDestLen) {
    int nRead = 0;
    int nWritten = 0;
    BYTE ch;
    static const char s_chHexChars[16] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                          'A', 'B', 'C', 'D', 'E', 'F'};

    if (!pbSrcData || !szDest || !pnDestLen) {
        return 0;
    }

    if (*pnDestLen < HexEncodeGetRequiredLength(nSrcLen)) {
        return 0;
    }

    while (nRead < nSrcLen) {
        ch = *pbSrcData++;
        nRead++;
        *szDest++ = s_chHexChars[(ch >> 4) & 0x0F];
        *szDest++ = s_chHexChars[ch & 0x0F];
        nWritten += 2;
    }
    *pnDestLen = nWritten;
    return 1;
}

#define HEX_INVALID ((char)-1)

//Get the decimal value of a hexadecimal character
char GetHexValue(char ch) {
    if (ch >= '0' && ch <= '9')
        return (ch - '0');
    if (ch >= 'A' && ch <= 'F')
        return (ch - 'A' + 10);
    if (ch >= 'a' && ch <= 'f')
        return (ch - 'a' + 10);
    return HEX_INVALID;
}

int HexDecode(const char *pSrcData, int nSrcLen, unsigned char *pbDest, int *pnDestLen) {
    int nRead = 0;
    int nWritten = 0;

    if (!pSrcData || !pbDest || !pnDestLen) {
        return 0;
    }

    if (*pnDestLen < HexDecodeGetRequiredLength(nSrcLen)) {
        return 0;
    }

    while (nRead < nSrcLen) {
        char ch1, ch2;

        if ((char) *pSrcData == '\r' || (char) *pSrcData == '\n')break;
        ch1 = GetHexValue((char) *pSrcData++);
        ch2 = GetHexValue((char) *pSrcData++);
        if ((ch1 == HEX_INVALID) || (ch2 == HEX_INVALID)) {
            return 0;
        }
        *pbDest++ = (unsigned char) (16 * ch1 + ch2);
        nWritten++;
        nRead += 2;
    }

    *pnDestLen = nWritten;
    return 1;
}

JNIEXPORT jint JNICALL
Java_com_androidex_mytools_MainActivity_main(JNIEnv *env, jobject obj,
                                                 jstring argv) {
    char *arr[3];
    arr[0] = "writecmd";
    arr[1] = "/dev/uart2g";
    arr[2] = (char *) (*env)->GetStringUTFChars(env, argv, 0);
    return main(3, arr);
}

/**
 * writecmd /dev/uart2g FB00030000FE
 * writecmd /dev/uart2g FB00040000FE
 */
int main(int argc, char *argv[]) {
    if (argc < 3) {
        //printf("参数不够，使用方法：\r\n writecmd dev hexcmd\r\n");
        printf("how to use:\r\n writecmd dev hexcmd\r\n");
        printf("you can use like :\r\n writecmd /dev/rkey FB0100010000FE\r\n");
        //gpiotest H 17 1 1
    } else {
        char deccmd[MAX_CMD_LEN];
        int dlen = MAX_CMD_LEN;
        if (HexDecode(argv[2], strlen(argv[2]), deccmd, &dlen) <= 0) {
            printf("Parameters 2 is not a hex string.\n");
            return -1;
        } else {

            int fd = open(argv[1], O_WRONLY | O_NONBLOCK);
            if (fd <= 0) {
                printf("open %s error:%s\n", argv[1], strerror(errno));
                return -1;
            } else {
                if (write(fd, deccmd, dlen) != dlen) {
                    printf("write error :%s\n", strerror(errno));
                }
                else {
                    printf("write ok.\n");
                }
                close(fd);
            }
        }
    }
    printf("run end.\n");
    return 0;
}