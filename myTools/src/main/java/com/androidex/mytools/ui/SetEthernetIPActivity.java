package com.androidex.mytools.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.mytools.R;
import com.androidex.mytools.utils.NetUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Iterator;

import static com.androidex.mytools.utils.NetUtils.getIPv4Address;

public class SetEthernetIPActivity extends Activity {
    StaticIpConfiguration mStaticIpConfiguration;
    IpConfiguration mIpConfiguration;
    EthernetManager mEthManager;
    private RadioButton radioDHCP,radioManual;
    private EditText textIP,textDns1,textDns2,textGateway;
    private Button viewSet,viewExit;

    private  static String mEthIpAddress = "192.168.1.102";  //IP
    private  static String mEthNetmask = "255.255.255.0";  //  子网掩码
    private  static String mEthGateway = "192.168.1.1";   //网关
    private  static String mEthdns1 = "8.8.8.8";   // DNS1
    private  static String mEthdns2 = "8.8.4.4";   // DNS2
    private boolean isset = false;
    private SharedPreferences sharedPreferences;
    private IpConfiguration.IpAssignment mIpAssignment = IpConfiguration.IpAssignment.DHCP;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case 1:
                    isset = false;
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setethernet_ip);
        mEthManager = (EthernetManager) getSystemService("ethernet");
        sharedPreferences = sharedPreferences =getSharedPreferences("androidex_ip_info_sp",MODE_PRIVATE);
        initView();
    }

    private void setBykey(String key,String value){
        sharedPreferences.edit().putString(key,value).commit();
    }
    private String getByKey(String key){
        return sharedPreferences.getString(key,"");
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mEthManager.isAvailable()){
            initIpMode();
        }else{
            Log.i("xiao_","以太网不可用");
        }
    }
    //textIP,textDns1,textDns2,textGateway;
    private void initView(){
        radioDHCP = (RadioButton) findViewById(R.id.dhcp_radio);
        radioManual = (RadioButton) findViewById(R.id.manual_radio);
        textIP = (EditText) findViewById(R.id.ip_);
        textDns1 = (EditText) findViewById(R.id.dns1_);
        textDns2 = (EditText) findViewById(R.id.dns2_);
        textGateway = (EditText) findViewById(R.id.wanguan_);
        findViewById(R.id.set_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEthManager.isAvailable()){
                    if(radioDHCP.isChecked()){
                        mEthManager.setConfiguration(new IpConfiguration(mIpAssignment, IpConfiguration.ProxySettings.NONE,
                                null, null));
                    }else{
                        if(isIpAddress(textIP.getText().toString())
                                && isIpAddress(textGateway.getText().toString())) {
                            if((isIpAddress(textDns1.getText().toString()) || textDns1.getText().toString().trim() == null)
                                    && (isIpAddress(textDns2.getText().toString()) || textDns2.getText().toString().trim() == null)){

                                mEthIpAddress = textIP.getText().toString().trim();
                                mEthGateway = textGateway.getText().toString().trim();
                                mEthdns1 = textDns1.getText().toString().trim();
                                mEthdns2 = textDns2.getText().toString().trim();
                                if(!isset){
                                    isset = true;
                                    mHandler.sendEmptyMessageDelayed(1,2000);
                                    setBykey("mEthIpAddress",mEthIpAddress);
                                    setBykey("mEthGateway",mEthGateway);
                                    setBykey("mEthdns1",mEthdns1);
                                    setBykey("mEthdns2",mEthdns2);
                                    setStaticIP();
                                    if(mEthManager.isAvailable()){
                                        getEthIP(mEthManager.getConfiguration());
                                    }
                                }
                            }else{
                                showToast("请正确填写DNS");
                            }
                        }else{
                            showToast("请正确填写IP或网关");
                        }
                    }
                }else{
                    Log.i("xiao_","以太网不可用");
                }
                Log.i("xiao_","ip设置完成");
            }
        });
       findViewById(R.id.exit_).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetEthernetIPActivity.this.finish();
            }
        });;
        radioDHCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEthMode(IpConfiguration.IpAssignment.DHCP,null);
            }
        });
        radioManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEthMode(IpConfiguration.IpAssignment.STATIC,null);
            }
        });
    }
    private void showToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void setEthMode(IpConfiguration.IpAssignment ethMode, IpConfiguration ipinfo){
        if(ipinfo!=null){
            getEthIP(ipinfo);
        }
        if(ethMode == IpConfiguration.IpAssignment.DHCP){
            radioDHCP.setChecked(true);
            textIP.setEnabled(false);
            textDns1.setEnabled(false);
            textDns2.setEnabled(false);
            textGateway.setEnabled(false);
            mIpAssignment = IpConfiguration.IpAssignment.DHCP;
        }else{
            radioManual.setChecked(true);
            textIP.setEnabled(true);
            textDns1.setEnabled(true);
            textDns2.setEnabled(true);
            textGateway.setEnabled(true);
            mIpAssignment = IpConfiguration.IpAssignment.STATIC;
        }
    }

    private void initIpMode(){
        IpConfiguration ipinfo = mEthManager.getConfiguration();
        if(ipinfo!=null){
            if(ipinfo.ipAssignment == IpConfiguration.IpAssignment.DHCP) {
                Log.i("xiao_","动态IP");
                setEthMode(IpConfiguration.IpAssignment.DHCP,ipinfo);
            }else{
                Log.i("xiao_","静态IP");
                setEthMode(IpConfiguration.IpAssignment.STATIC,ipinfo);
            }
        }else{
            Log.i("xiao_","没有连接？获取IpConfiguration失败");
        }
    }

    private void getEthIP(IpConfiguration ipin){
        if(ipin!=null){
            StaticIpConfiguration staticConfig = ipin.getStaticIpConfiguration();
            if(staticConfig!=null){
                if (staticConfig.ipAddress != null) { //ip
                    textIP.setText(staticConfig.ipAddress.getAddress().getHostAddress());
                    Log.i("xiao_","ip = "+staticConfig.ipAddress.getAddress().getHostAddress());
                }
                if (staticConfig.gateway != null) { //网关
                    textGateway.setText(staticConfig.gateway.getHostAddress());
                    Log.i("xiao_","gateway = "+staticConfig.gateway.getHostAddress());
                }
                Iterator<InetAddress> dnsIterator = staticConfig.dnsServers.iterator(); //DNS
                int i=0;
                while (dnsIterator.hasNext()){
                    if(i == 0){
                        String dns1 = dnsIterator.next().getHostAddress();
                        textDns1.setText(dns1);
                        Log.i("xiao_","dns1 = "+dns1);
                        i++;
                    }else{
                        String dns2 = dnsIterator.next().getHostAddress();
                        textDns2.setText(dns2);
                        Log.i("xiao_","dns2 = "+dns2);
                    }
                }
            }else{
                setDefip();
                Log.i("xiao_","StaticIpConfiguration = null 获取ip信息失败");
            }
        }else{
            Log.i("xiao_","IpConfiguration = null 获取ip信息失败");
            setDefip();
        }
    }

    private void setDefip(){
        textIP.setText(getByKey("mEthIpAddress"));
        textGateway.setText(getByKey("mEthGateway"));
        textDns1.setText(getByKey("mEthdns1"));
        textDns2.setText(getByKey("mEthdns2"));
    }

    private String getViewText(int id){
        return ((TextView)findViewById(id)).getText().toString().trim();
    }

    private void setStaticIP() {
        mStaticIpConfiguration = new StaticIpConfiguration();
        Inet4Address inetAddr = getIPv4Address(mEthIpAddress);
        int prefixLength = NetUtils.maskStr2InetMask(mEthNetmask);
        InetAddress gatewayAddr = getIPv4Address(mEthGateway);
        InetAddress dnsAddr = getIPv4Address(mEthdns1);

        if (inetAddr.getAddress().toString().isEmpty() || prefixLength ==0 || gatewayAddr.toString().isEmpty()
                || dnsAddr.toString().isEmpty()) {
           return;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName("android.net.LinkAddress");
        } catch (Exception e) {
            // TODO: handle exception
        }

        Class[] cl = new Class[]{InetAddress.class, int.class};
        Constructor cons = null;

        //取得所有构造函数
        try {
             cons = clazz.getConstructor(cl);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        //给传入参数赋初值
        Object[] x = {inetAddr, prefixLength};

        String dnsStr2 = mEthdns2;
        //mStaticIpConfiguration.ipAddress = new LinkAddress(inetAddr, prefixLength);
        try {
            mStaticIpConfiguration.ipAddress = (LinkAddress) cons.newInstance(x);
            Log.d("232323", "chanson 1111111");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        mStaticIpConfiguration.gateway=gatewayAddr;
        mStaticIpConfiguration.dnsServers.add(dnsAddr);

        if (!dnsStr2.isEmpty()) {
            mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
        }

        Log.d("2312321", "chanson mStaticIpConfiguration  ====" + mStaticIpConfiguration);

        mIpConfiguration = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, mStaticIpConfiguration, null);

        mEthManager.setConfiguration(mIpConfiguration);
    }

    private boolean isIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {
            if (end == -1) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }
}
