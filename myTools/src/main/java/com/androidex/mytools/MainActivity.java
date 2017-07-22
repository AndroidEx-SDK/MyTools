package com.androidex.mytools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidex.common.AndroidExActivityBase;
import com.androidex.logger.Log;

public class MainActivity extends AndroidExActivityBase implements View.OnClickListener {
    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {
            Log.d("KMY350", "appDevicesLibs.so library not found!");
        }
    }

    private LinearLayout mainView;
    private Button otg_usb, reboot_to, reboot, shutdown;
    public static final String USB_OTG = "FB00030000FE";
    public static final String USB_HOST = "FB00040000FE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = (LinearLayout) findViewById(R.id.activity_main);
        EnterFullScreen();
        setFullScreen(true);
        mainView.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);

        // 控件实例化
        otg_usb = (Button) findViewById(R.id.otg_usb);
        reboot_to = (Button) findViewById(R.id.reboot_to);
        reboot = (Button) findViewById(R.id.reboot);
        shutdown = (Button) findViewById(R.id.shutdown);
        otg_usb.setOnClickListener(this);
        reboot_to.setOnClickListener(this);
        reboot.setOnClickListener(this);
        shutdown.setOnClickListener(this);
    }

    public void EnterFullScreen() {
        sendBroadcast(new Intent("com.android.action.hide_navigationbar"));
    }

    protected void setFullScreen(boolean value) {
        if (this.mainView != null) {
            if (value) {
                this.mainView.setSystemUiVisibility(13063);
                this.mFullScreenDisabled = false;
            } else {
                this.mainView.setSystemUiVisibility(1536);
                this.mFullScreenDisabled = true;
            }
        }
    }

    /**
     * writecmd /dev/uart2g FB00030000FE
     * writecmd /dev/uart2g FB00040000FE
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.otg_usb://OTG-USB模式切换
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("OTG-USB模式切换");
                builder.setPositiveButton("HOST模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = new Intent();
//                        intent.setAction("com.androidex.action.OTGHOST");
//                        sendBroadcast(intent);
                        int main = writeCmd(USB_HOST);
                        if (main == 0) {
                            Toast.makeText(MainActivity.this, "USB为HOST模式成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "USB为HOST模式失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("OTG模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = new Intent();
//                        intent.setAction("com.androidex.action.OTGDEVICE");
//                        sendBroadcast(intent);
                        int main = writeCmd(USB_OTG);
                        if (main == 0) {
                            Toast.makeText(MainActivity.this, "USB为OTG模式成功", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "USB为OTG模式失败", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
                break;
            case R.id.reboot_to://升级
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("是否升级");
                builder1.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("com.androidex.action.reboot_to_adfu");
                        sendBroadcast(intent);
                        Toast.makeText(MainActivity.this, "正在升级中...", Toast.LENGTH_SHORT).show();
                    }
                });
                builder1.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder1.create().show();
                break;
            case R.id.reboot:// 重启
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                builder2.setTitle("是否重启");
                builder2.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("com.androidex.action.reboot");
                        sendBroadcast(intent);
                        Toast.makeText(MainActivity.this, "正在重新启动中...", Toast.LENGTH_SHORT).show();
                    }
                });
                builder2.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder2.create().show();
                break;
            case R.id.shutdown:// 关机
                AlertDialog.Builder builder3 = new AlertDialog.Builder(MainActivity.this);
                builder3.setTitle("是否关机");
                builder3.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("com.androidex.action.shutdown");
                        sendBroadcast(intent);
                        Toast.makeText(MainActivity.this, "正在关机中...", Toast.LENGTH_SHORT).show();
                    }
                });
                builder3.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder3.create().show();
                break;
        }
    }

    public native int writeCmd(String cmd);
}

