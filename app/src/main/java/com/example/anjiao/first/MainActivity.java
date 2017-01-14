package com.example.anjiao.first;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN:";
    int nLowPowerNumber = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startMonitorService();

        ((Button) findViewById(R.id.buttonShutdown)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shutdown();
            }
        });
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
            e.printStackTrace();
        }
    }

    public void startMonitorService() {
        Intent intent = new Intent(this, MonitorService.class);
        startService(intent);
    }
    public void stopMonitorService() {
        Intent intent = new Intent(this, MonitorService.class);
        stopService(intent);
    }
}
