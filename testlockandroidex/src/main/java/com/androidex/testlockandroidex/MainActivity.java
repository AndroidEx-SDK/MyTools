package com.androidex.testlockandroidex;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.androidex.IDoorLockInterface;
import com.androidex.plugins.OnBackCall;
import com.androidex.plugins.kkfile;

public class MainActivity extends AppCompatActivity implements OnBackCall {
    private DoorLockServiceBinder mDoorLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDoorLock = new DoorLockServiceBinder();

    }

    public void open(View v){
        //int result = mDoorLock.openDoor(0xF0, 0); //常开
         int result = mDoorLock.openDoor(0xF0, 0x40); //延时自动关门
        Log.i("xiao_",result+"    开门");
    }

    public void close(View v){
        int result = mDoorLock.closeDoor(0); //主门
        //mDoorLock.closeDoor(1); //副门
        Log.i("xiao_",result+"   关门");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDoorLock = null;
    }

    @Override
    public void onBackCallEvent(int i, String s) {
        Log.v("onBackCallEvent", s);
    }

    public class DoorLockServiceBinder extends IDoorLockInterface.Stub {
        String rkeyDev = "/dev/rkey";
        //String rkeyDev = "/dev/ttySV0";
        int ident = 0;

        /**
         * 开门指令
         *
         * @param index 门的序号,主门=0,副门=1
         * @param delay 延迟关门的时间,0表示不启用延迟关门,大于0表示延迟时间,延迟时间为delay*150ms
         * @return 大于0表示成功, 实际上等于9表示真正的成功, 因为返回值表示写入的数据, 开门指令长度为9.
         */
        public int openDoor(int index, int delay) {
            kkfile rkey = new kkfile();
            if (index < 0 || index > 0xFE) index = 0;
            if (ident < 0 || ident > 0xFE) ident = 0;
            if (delay < 0 || delay > 0xFE) delay = 0;
            // int result = mDoorLock.openDoor(0xF0, 0); //常开
            // int result = mDoorLock.openDoor(0xF0, 0x40); //延时自动关门40*150ms
            String cmd = String.format("FB%02X5003%02X01%02X00FE", ident, index, delay);
            //echo "FB005003F0010000FE" > dev/ttySV0
            //echo "FB005003F0010000FE" > dev/rkey
            //FB+Index+CMD+LEN+DATA+CRC+FE
            //开门  FB 00 50 03 F0 01 00 00FE
            //关门  FB 00 25 03 F0 01 00 00FE
            //常开：        FB005003 F0 01 00 00FE
            //延时自动关门：FB005003 F0 01 40 00FE
            //全开：  FB005003 F0 01 40 00FE
            //开主门：FB005003 00 01 40 00FE
            //开附门：FB005003 01 01 40 00FE
            int r = rkey.native_file_writeHex(rkeyDev, cmd);
            Log.i("xiao_", "cmd = " + cmd);
            if (r > 0) {
                SoundPoolUtil.getSoundPoolUtil().loadVoice(getBaseContext(), 011111);
                Log.i("xiao_", " 开门播报");
            } else {
                Log.i("xiao_", " r <=0");
            }
            return r > 0 ? 1 : 0;
        }

        public int closeDoor(int index) {
            kkfile rkey = new kkfile();

            if (index < 0 || index > 0xFE) index = 0;
            if (ident < 0 || ident > 0xFE) ident = 0;
            String cmd = String.format("FB%02X2503%02X000000FE", ident, index);
            int r = rkey.native_file_writeHex(rkeyDev, cmd);
            return r > 0 ? 1 : 0;
        }
    }
}
