package com.example.listenservice.Androidex;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/1/13.
 */

public class ListenService extends Service {
    private static final String ANDROIDEX_LISTEN_SP = "androidex_listen_sp";
    private static final String ANDROIDEX_LISTEN_PACKAGE = "androidex_listen_package";
    private static final String ANDROIDEX_LISTEN_ACTIVITY = "androidex_listen_activity";
    private static final String ANDROIDEX_LISTEN_UPDATE_ACTION = "com.androidex.listen.update.action";
    private static final String ANDROIDEX_LISTEN_DELETE_ACTION = "com.androidex.listen.delete.action";
    private SharedPreferences sharedPreferences;
    private ActivityManager activityManager;
    private String listenAppName;
    private String listenActivity;
    private Context mContext;
    private boolean isPullTime = false;
    private Handler mHandler = new Handler();
    private Timer timer;
    public static boolean isRun = false;
    private Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            if(listenAppName!=null && listenAppName.length()>0 && listenActivity!=null && listenActivity.length()>0){
                Intent i = new Intent();
                i.setClassName(listenAppName,listenActivity);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
            }
        }
    };
    //com.android.calculator2/.Calculator
    //am broadcast -a com.androidex.listen.update.action --es androidex_listen_package "com.android.calculator2" --es androidex_listen_activity "com.android.calculator2.Calculator"
    //am broadcast -a com.androidex.listen.delete.action --es androidex_listen_package "com.android.calculator2"
    private BroadcastReceiver settingBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String activity = intent.getStringExtra(ANDROIDEX_LISTEN_ACTIVITY)==null?"":intent.getStringExtra(ANDROIDEX_LISTEN_ACTIVITY);
            String pack = intent.getStringExtra(ANDROIDEX_LISTEN_PACKAGE)==null?"":intent.getStringExtra(ANDROIDEX_LISTEN_PACKAGE);
            Log.i("xiao_",pack+"/"+activity);
            if(action.equals(ANDROIDEX_LISTEN_UPDATE_ACTION)){
                //替换
                if(timer!=null){
                    timer.cancel();
                    timer = null;
                }
                mHandler.removeCallbacks(startRunnable);
                setAppActivity(activity);
                setAppPackage(pack);
                initListen();
            }else if(action.equals(ANDROIDEX_LISTEN_DELETE_ACTION)){
                //移除
                if(listenAppName!=null && listenAppName.equals(pack)){
                    if(timer!=null){
                        timer.cancel();
                        timer = null;
                    }
                    mHandler.removeCallbacks(startRunnable);
                    setAppActivity("");
                    setAppPackage("");
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("xiao_","开始监听->");
        mContext = this;
        isRun = true;
        sharedPreferences =getSharedPreferences(ANDROIDEX_LISTEN_SP,MODE_PRIVATE);
        activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        initListen();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ANDROIDEX_LISTEN_DELETE_ACTION);
        filter.addAction(ANDROIDEX_LISTEN_UPDATE_ACTION);
        registerReceiver(settingBroadcast,filter);
    }

    private void initListen(){
        listenAppName = getAppPackage();
        listenActivity= getAppActivity();
        if(listenAppName!=null & listenAppName.length()>0){
            if(timer == null){
                timer = new Timer();
            }
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(!isTopApplication()){
                        if(!isPullTime){
                            mHandler.postDelayed(startRunnable,30*1000);
                            isPullTime = true;
                        }
                    }else{
                        mHandler.removeCallbacks(startRunnable);
                        isPullTime = false;
                    }
                }
            },500,3000);
        }
    }

    private boolean isTopApplication(){
        boolean top = false;
        if(activityManager!=null){
            ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
            if(listenAppName!=null){
                top = cn.getPackageName().equals(listenAppName);
            }
        }
        return top;
    }

    private String getAppPackage(){
        if(sharedPreferences!=null){
            return sharedPreferences.getString(ANDROIDEX_LISTEN_PACKAGE,"");
        }
        return null;
    }
    private void setAppPackage(String appPackage){
        if(sharedPreferences!=null){
            sharedPreferences.edit().putString(ANDROIDEX_LISTEN_PACKAGE,appPackage).commit();
            Log.i("xiao_","设置监听Package = "+appPackage);
        }
    }

    private String getAppActivity(){
        if(sharedPreferences!=null){
            return sharedPreferences.getString(ANDROIDEX_LISTEN_ACTIVITY,"");
        }
        return null;
    }
    private void setAppActivity(String appActivity){
        if(sharedPreferences!=null){
            sharedPreferences.edit().putString(ANDROIDEX_LISTEN_ACTIVITY,appActivity).commit();
            Log.i("xiao_","设置启动Activity = "+appActivity);
        }
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(settingBroadcast);
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        mHandler.removeCallbacks(startRunnable);
        isRun = false;
        super.onDestroy();
    }
}
