package com.example.listenservice.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.listenservice.Androidex.ListenService;

/**
 * Created by Administrator on 2018/1/13.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("xiao_","收到开机广播->");
        if(!ListenService.isRun){
            context.startService(new Intent(context, ListenService.class));
        }
    }
}
