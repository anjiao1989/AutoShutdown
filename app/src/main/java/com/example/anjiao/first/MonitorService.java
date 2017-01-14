package com.example.anjiao.first;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class MonitorService extends Service {
    private static String TAG = "SERVICE: ";
    BatteryReceiver batteryReceiver = null;

    int nLowPowerNumber = 20;
    boolean bFirst = true;

    public MonitorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBatteryChangedReceiver();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void beginBackgroundThread() {
        Thread thread = new Thread(null, doBackgroundWork, "backgroundThread");
        thread.start();
    }
    private Runnable doBackgroundWork = new Runnable() {
        public void run() {

        };
    };

    private void registerBatteryChangedReceiver() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);
    }
    private void unregisterBatteryChangedReceiver() {
        unregisterReceiver(batteryReceiver);
        batteryReceiver = null;
    }

    class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //判断它是否是为电量变化的Broadcast Action
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);

                Log.i(TAG, "电池电量为" + ((level * 100) / scale) + "%");
                if(bFirst && level > nLowPowerNumber) {
                    bFirst = false;
                }
                if(!bFirst && level < nLowPowerNumber) {
                    shutdown();
                }
            }
        }
    }

    static Process createSuProcess() throws IOException {
        return Runtime.getRuntime().exec("su");
    }

    static Process createSuProcess(String cmd) throws IOException {
        DataOutputStream os = null;
        Process process = createSuProcess();

        try {
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit $?\n");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "exception", e);
                }
            }
        }

        return process;
    }

    // 需要 root 权限
    public void shutdown() {
        Log.i(TAG, "do shutdown");
        try {
            createSuProcess("reboot -p").waitFor(); //关机命令
            //createSuProcess("reboot").waitFor(); //这个部分代码是用来重启的
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }
}
