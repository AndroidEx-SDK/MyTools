package com.androidex.mytools;


import android.content.Context;

import com.androidex.aexapplibs.appLibsService;

/**
 * @author liyp
 * @editTime 2017/12/11
 */

public class MyService extends appLibsService {

    private static MyService myService;

    public MyService(Context context) {
        super(context);
    }

    public static MyService getInstance(Context context){
        if (myService==null){
            myService = new MyService(context);
        }
        return myService;
    }


}
