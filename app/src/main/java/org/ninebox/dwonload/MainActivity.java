package org.ninebox.dwonload;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.ninebox.entities.FileInfo;
import org.ninebox.services.DownLoadService;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    private Button  stopButton;
    private Button  downButton;
    private TextView tvFileName;

        private FileInfo fileInfo = new FileInfo("http://dlsw.baidu.com/sw-search-sp/soft/97/17517/Steam_2.10.91.91_Setup.1459840891.exe"
            ,0 ,"Steam_2.10.91.91_Setup.1459840891.exe",0,0);
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
        tvFileName = (TextView) findViewById(R.id.tvFileNmae);

        downButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);

        //





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
                intent.setAction(DownLoadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
                break;
                }
            default:
                break;
        }

    }
}
