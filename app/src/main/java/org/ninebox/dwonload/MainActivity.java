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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ninebox.entities.FileInfo;
import org.ninebox.services.DownLoadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

//    private Button  stopButton;
//    private Button  downButton;
//    private ProgressBar mProgresBar;

    private ListView mLvFile = null;
    private FileListAdapter mAdapter=null;
    private List<FileInfo> mFileList=null;



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

        //创建文件集合
        mFileList  = new ArrayList<>();
        FileInfo fileInfo = new FileInfo("http://dota2.dl.wanmei.com/dota2/client/wanmeidota3.2.1.apk"
                ,0 ,"wanmeidota3.2.1.apk",0,0);
         FileInfo fileInfo1 = new FileInfo("http://sw.bos.baidu.com/sw-search-sp/software/30f44738c65/QQ_8.4.18357.0_setup.exe"
                ,1 ,"QQ_8.4.18357.0_setup.exe",0,0);
        FileInfo fileInfo2 = new FileInfo("http://dlsw.baidu.com/sw-search-sp/soft/ca/13442/Thunder_dl_7.9.43.5054.1456898740.exe"
                ,2 ,"Thunder_dl_7.9.43.5054.1456898740.exe",0,0);
         FileInfo fileInfo3 = new FileInfo("http://staticlive.douyutv.com/upload/client/douyu_client_1_0v2_2_7.apk"
                ,3 ,"douyu_client_1_0v2_2_7.apk",0,0);
        mFileList.add(fileInfo);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);

        mLvFile = (ListView) findViewById(R.id.listView);

        //设置adpter
        mAdapter = new FileListAdapter(this,mFileList);
        mLvFile.setAdapter(mAdapter);

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownLoadService.ACTION_UPDATE);
        filter.addAction(DownLoadService.ACTION_FINISH);
        registerReceiver(mReceiver,filter);

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
                    int id= intent.getIntExtra("id",0);
                    mAdapter.updateProgress(id,finished);
                    Log.i("TAG","----------------------"+finished+"---------------");
                    break;
                case DownLoadService.ACTION_FINISH:
                    FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                    mAdapter.updateProgress(fileInfo.getId(),0);
                    Toast.makeText(MainActivity.this,mFileList.get(fileInfo.getId()).getFileName()+"下载完毕",Toast.LENGTH_SHORT).show();

            }
        }
    };
}
