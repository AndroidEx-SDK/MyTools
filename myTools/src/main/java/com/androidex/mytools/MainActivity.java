package com.androidex.mytools;

import android.annotation.SuppressLint;
import android.net.EthernetManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.androidex.common.AndroidExActivityBase;
import com.androidex.mytools.ui.SetEthernetIPActivity;
import com.androidex.mytools.utils.ProcCpuInfo;
import com.androidex.mytools.utils.SilentInstall;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AndroidExActivityBase implements View.OnClickListener {
    static {
        try {
            System.loadLibrary("appDevicesLibs");
        } catch (UnsatisfiedLinkError e) {

        }
    }
    private LinearLayout mainView;
    private Button otg_usb, btn_Ethernet, setEthernetIP, setWifiIP, reboot_to, reboot, shutdown, getUUID, finish;
    public static final String USB_OTG = "FB00030000FE";
    public static final String USB_HOST = "FB00040000FE";

    private Switch ethernetSwitch;
    private EthernetManager mEthManager;
    private Method ethernetStart,ethernetStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EnterFullScreen();//隐藏底部
        setContentView(R.layout.activity_main);
        mainView = (LinearLayout) findViewById(R.id.activity_main);
        //setFullScreenView(mainView);
        //setFullScreen(true);//kk34全屏
        // mainView.setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);//禁止下拉
        // 控件实例化
        otg_usb = (Button) findViewById(R.id.otg_usb);
        btn_Ethernet = (Button) findViewById(R.id.btn_Ethernet);
        reboot_to = (Button) findViewById(R.id.reboot_to);
        reboot = (Button) findViewById(R.id.reboot);
        shutdown = (Button) findViewById(R.id.shutdown);
        getUUID = (Button) findViewById(R.id.getUUID);
        setEthernetIP = (Button) findViewById(R.id.setEthernetIP);
        setWifiIP = (Button) findViewById(R.id.setWifiIP);
        finish = (Button) findViewById(R.id.finish);
        otg_usb.setOnClickListener(this);
        reboot_to.setOnClickListener(this);
        reboot.setOnClickListener(this);
        setEthernetIP.setOnClickListener(this);
        setWifiIP.setOnClickListener(this);
        shutdown.setOnClickListener(this);
        btn_Ethernet.setOnClickListener(this);
        getUUID.setOnClickListener(this);
        finish.setOnClickListener(this);

        //以太网控制
        initMethod();
        initEthernet();

        String chipIDHex = ProcCpuInfo.getChipIDHex();
        Log.e(TAG, "chipIDHex===" + chipIDHex);
        String chipID = ProcCpuInfo.getChipID();
        Log.e(TAG, "chipID===" + chipID);
    }

    private void initEthernet(){
        ethernetSwitch = (Switch) findViewById(R.id.ethernet_switch);
        int state = Settings.Global.getInt(this.getContentResolver(),"ethernet_on",0); //0 1 2
        if (state == 1){
            //以太网关闭
            ethernetSwitch.setChecked(false);
        }else if(state == 2){
            //以太网打开
            ethernetSwitch.setChecked(true);
        }
        ethernetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    //if(mEthDialog != null)
                    //mEthDialog.show();
                } else {
                    if(mEthManager != null && ethernetStop!=null)
                        try {
                            ethernetStop.invoke(mEthManager);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                }
                try {
                    Settings.Global.putInt(MainActivity.this.getContentResolver(), "ethernet_on", b ? 2 : 1); //2是打开 1是关闭
                    if (b) {
                        if (mEthManager != null && ethernetStart != null)
                            try {
                                ethernetStart.invoke(mEthManager);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            }
                    }
                } catch (SecurityException e) {
                    Toast.makeText(MainActivity.this, "请进行系统签名", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressLint("WrongConstant")
    private void initMethod(){
        mEthManager = (EthernetManager) getSystemService("ethernet");
        try {
            ethernetStart = mEthManager.getClass().getMethod("start",getParameterTypes(mEthManager.getClass(),"start"));
            ethernetStop = mEthManager.getClass().getMethod("stop",getParameterTypes(mEthManager.getClass(),"stop"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Class[] getParameterTypes(Class c,String methodName){
        Method ms[] = c.getMethods();
        for (int i = 0; i < ms.length; i++) {
            if(ms[i].getName().equals(methodName)){
                return  ms[i].getParameterTypes();
            }
        }
        return null;
    }


    /**
     * 隐藏底部导航栏
     */
    public void EnterFullScreen() {
        sendBroadcast(new Intent("com.android.action.hide_navigationbar"));
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
                        Intent intent = new Intent();
                        intent.setAction("com.androidex.action.OTGHOST");
                        sendBroadcast(intent);
                        Toast.makeText(MainActivity.this, "切换HOST模式成功", Toast.LENGTH_SHORT).show();
//                        int main = writeCmd(USB_HOST);
//                        if (main == 0) {
//                            Toast.makeText(MainActivity.this, "USB为HOST模式成功", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(MainActivity.this, "USB为HOST模式失败", Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
                builder.setNegativeButton("OTG模式", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("com.androidex.action.OTGDEVICE");
                        sendBroadcast(intent);
                        Toast.makeText(MainActivity.this, "切换OTG模式成功", Toast.LENGTH_SHORT).show();
//                        int main = writeCmd(USB_OTG);
//                        if (main == 0) {
//                            Toast.makeText(MainActivity.this, "USB为OTG模式成功", Toast.LENGTH_SHORT).show();
//                        } else
//                            Toast.makeText(MainActivity.this, "USB为OTG模式失败", Toast.LENGTH_SHORT).show();
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
            case R.id.btn_Ethernet://以太网开关
                AlertDialog.Builder builder4 = new AlertDialog.Builder(MainActivity.this);
                builder4.setTitle("开启和关闭以太网");
                builder4.setPositiveButton("开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hwservice.EthernetStart();
                        Toast.makeText(MainActivity.this, "开启以太网...", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder4.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        hwservice.EthernetStop();
                        Toast.makeText(MainActivity.this, "关闭以太网...", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                builder4.create().show();
                break;
            case R.id.getUUID:
                Log.d(TAG, "uuid=" + MyService.getInstance(this).get_uuid());
                Log.d(TAG, "sdkVersion=" + MyService.getInstance(this).getSdkVersion());
                break;
            case R.id.setEthernetIP://设置以太网静态IP
                Intent intent = new Intent(MainActivity.this, SetEthernetIPActivity.class);
                startActivity(intent);
                break;
            case R.id.setWifiIP://直接查看另外一个demo
                Log.d(TAG, "请联系作者： liyp@androdiex.cn/dev@androidex.cn");
                break;
            case R.id.muteinstall:  //静默安装，需要系统签名
                File f = new File(Environment.getExternalStorageDirectory().getPath() + "/wnys.apk"); //请保证这个路径有这个app文件
                if (f.exists()) {
                    String command = "pm install -r " + f.toString() + "\n";
                    SilentInstall.executeCmd(command);
                }
                break;
            case R.id.uninstall:  //卸载程序
                String unInstallpackage = "com.snda.wifilocating"; //需要卸载程序的包名，我卸载了WIFI万能钥匙
                String command = "pm uninstall " + unInstallpackage + "\n";
                SilentInstall.executeCmd(command);
                break;
            case R.id.finish:
                finish();
                break;
            default:
                break;
        }
    }

    public native int writeCmd(String cmd);
}

