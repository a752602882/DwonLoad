package org.ninebox.dwonload;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ninebox.entities.FileInfo;
import org.ninebox.services.DownLoadService;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private Button  stopButton;
    private Button  downButton;
    private ProgressBar mProgresBar;

        private FileInfo fileInfo = new FileInfo("http://dota2.dl.wanmei.com/dota2/client/wanmeidota3.2.1.apk"
            ,0 ,"wanmeidota3.2.1.apk",0,0);
    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopButton = (Button) findViewById(R.id.stopButton);
        downButton = (Button) findViewById(R.id.downButton);
        mProgresBar = (ProgressBar) findViewById(R.id.progressBar);

        downButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownLoadService.ACTION_UPDATE);
        registerReceiver(mReceiver,filter);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.downButton:
                {
                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
                break;
                }
                case R.id.stopButton:
                {
                Intent intent = new Intent(MainActivity.this, DownLoadService.class);
                intent.setAction(DownLoadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
                break;
                }
            default:
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){
                case DownLoadService.ACTION_UPDATE:
                    int finished= intent.getIntExtra("finished",0);
                Log.i("TAG","----------------------"+finished+"---------------");
                mProgresBar.setProgress(finished);

                break;
            }
        }
    };
}
