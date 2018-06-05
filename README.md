# 安卓工控工具使用说明文档

    本程序为深圳市安卓工控设备有限公司提供的基于KK3X系列工控主板开发的安卓工具，主要功能包含开机自启动、设置静态IP、系统签名、静默升级、系统全屏、本机UUID、OTG/HOST切换、开启/关闭以太网、
    卸载应用、开关锁、后台监控等
###### 代码仓库地址：https://github.com/AndroidEx-SDK/MyTools.git

##### 联系作者：liyp@androdiex.cn/dev@androidex.cn

## 静默安装

    1、修改AndroidManifest.xml
    <manifest xmlns:android="http://schemas.android.com/apk/res/android"
    ++  android:sharedUserId="android.uid.system" //声明此应用为系统应用（拥有系统权限）
        package="com.androidex.mytools">

    ++ <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

    2、写测试代码MainActivity 194-199行

    3、写完代码后android studio 或eclipse 通过run无法完成安装，需要Android Studio通过Build-> Build APK （Build之前请把App/build/outputs/apk删除）

    4、Build生成目标Apk文件后，拷贝根目录下 platform.pk8、platform.x509.pem、signapk.jar、和目标Apk文件到本地。

    5、对目标apk文件签名：
        通过命令行切换到该目录我的路径F:\signapk\File

        先从C盘切换到F盘命令"F:"

        然后进入到存放签名和APP文件目录命令："cd signapk/File"

        然后对目标apk文件签名，我需要签名的apk的文件名："a.apk"，签名后生成的apk命名为"b.apk"，命令如下：

        "java -jar signapk.jar platform.x509.pem platform.pk8 a.apk b.apk"

        这样就生成了拥有系统权限的App，可以通过SilentInstall.install(String path) 进行安装。


## adb环境配置

    1、找到Android SDK 路径下的platform-tools文件夹，拷贝路径，我的路径：E:\SDK\platform-tools
    2、右键我的电脑->高级系统设置->高级->环境变量：修改PATH:在末尾增加对应路径（如果末尾没有 ; 号，需要先添加 ; 号，英文分号），E:\SDK\platform-tools;
    3、使用adb命令

## adb环境配置后仍旧无法正常连接设备：

    参考：https://jingyan.baidu.com/article/ce09321b5b76642bff858f31.html?qq-pf-to=pcqq.c2c

## 监听前台应用

    1、第一次运行的时候需要启动一次服务，才可以使应用开机监听广播；
    2、使用广播来设置所需要监听的程序；

    如：我需要监听计算器程序
    在ListenService开启的状态下使用adb 命令：
        adb shell am broadcast -a com.androidex.listen.update.action --es androidex_listen_package "com.android.calculator2" --es androidex_listen_activity "com.android.calculator2.Calculator"

    如：我需要移除对计算器程序的监听
    在ListenService开启的状态下使用adb 命令：
        adb shell am broadcast -a com.androidex.listen.update.action --es androidex_listen_package "com.android.calculator2"

    3、对应用程序进行签名；

## 卸载应用

    代码见MainActivity 202-207行

## 以太网开关控制

     两种方式：
        1.使用我司的appLibs.aar
        2.增加<uses-permission android:name="android.permission.WRITE_SETTINGS"></uses-permission>权限
            代码见MainActivity

## 设置以太网静态IP

    1.需要系统签名，签名方法请参考--静默安装功能
    2.需要引用classes.jar

## 设置WIFI静态IP和自动连接

    请联系作者： liyp@androdiex.cn/dev@androidex.cn