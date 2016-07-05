package org.ninebox.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.ninebox.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2016/7/1.
 */
public class DownLoadService extends Service {

    public  static final  String  ACTION_START= "ACTION_START";
    public  static final  String  ACTION_STOP ="ACTION_STOP";
    public  static final  String  ACTION_UPDATE ="ACTION_UPDATE";
   public  static final  String  FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloads/";

  //   public final  String  FILE_PATH= getCacheDir().getPath();

    public  String getFilePath() {
        return FILE_PATH;
    }

    public  static final  int  MSG_INIT =1;
    private DownloadTask mTask=null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction()))
        {
            FileInfo  fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("TAG",fileInfo.toString());

            //初始化线程
            new InitThread(fileInfo).start();
        }else  if (ACTION_STOP.equals(intent.getAction()))
        {
            FileInfo  fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.i("TAG",fileInfo.toString());
           if (mTask!=null){
               mTask.isPause=true;
           }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 初始化子线程
     * */
    class InitThread extends  Thread{
        private  FileInfo mFileInfo = null;

        public InitThread( FileInfo mFileInfo) {
            this.mFileInfo = mFileInfo;
        }

        @Override
        public void run() {


            RandomAccessFile mRFile=null;
            HttpURLConnection con=null;
            try {
                //连接网络文件
                //获得文件长度
                URL url  = new URL(mFileInfo.getUrl());
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(3000);
                int length = -1;
                if (con.getResponseCode()== HttpsURLConnection.HTTP_OK)
                {
                    length = con.getContentLength();

                }else if(length<=0){
                    return;
                }
                //在本地创建文件
                //设置网络文件


                File mDir = new File(FILE_PATH);
                if (!mDir.exists()){
                    getCacheDir().mkdir();
                  mDir= new File(getCacheDir().getPath());
                }
                File mFile = new File(mDir,mFileInfo.getFileName());
                mRFile = new RandomAccessFile(mFile,"rwd");
                //设置本地文件长度
                mRFile.setLength(length);
                mFileInfo.setLength(length);
                //线程和service.activity交互是使用handle
                mHander.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();


            } catch (Exception e) {
                e.printStackTrace();
            }finally {

                try {
                    mRFile.close();
                    con.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

     private Handler mHander =new Handler(){

         @Override
         public void handleMessage(Message msg) {
             super.handleMessage(msg);
             switch (msg.what){

                 case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                     Log.i("TAG",fileInfo.toString());
                     //启动下载任务
                     mTask = new DownloadTask(fileInfo,DownLoadService.this);
                     mTask.download();
                 break;
             }
         }
     };
}
