package org.ninebox.services;

import android.content.Context;
import android.content.Intent;

import org.ninebox.db.ThreadDAO;
import org.ninebox.db.ThreadDAOImpl;
import org.ninebox.entities.FileInfo;
import org.ninebox.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Administrator on 2016/7/1. 下载任务类
 */
public class DownloadTask {

    private FileInfo nFileInfo=null;
    private Context mContext=null;
    private ThreadDAO mThreadDAO;
    private  int mFinished=0;
    public  boolean isPause;


    public DownloadTask(FileInfo nFileInfo, Context mContext) {
        this.nFileInfo = nFileInfo;
        this.mContext = mContext;
        mThreadDAO =new ThreadDAOImpl(mContext);

     //   new DownloadThread()
    }


    public void download(){

        //读取数据库线程信息
        List<ThreadInfo> threadInfos =mThreadDAO.getThreads(nFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() ==0){
            //初始化线程信息
            threadInfo  = new ThreadInfo(0,nFileInfo.getUrl(),0,nFileInfo.getLength(),0);
        }else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程进行下载
        new DownloadThread(threadInfo).start();
    }



    class DownloadThread extends  Thread{


       private ThreadInfo mThreadInfo;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }

        @Override
        public void run() {

            HttpURLConnection conn = null;

            RandomAccessFile raf=null;
            InputStream input=null;
            //向数据库里插入线程信息
        if (!mThreadDAO.isRxists(mThreadInfo.getUrl(),mThreadInfo.getId())){
                mThreadDAO.insertThread(mThreadInfo);
            }
             //设置下载位置
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int start = mThreadInfo.getStart()+mThreadInfo.getFinished();
                //Range 范围的意思，可以设置开始的字节数到结束的字节数
                conn.setRequestProperty("Range","bytes="+start+"-"+mThreadInfo.getEnd());

                //设置文件写入位置
                File  mFile = new File(mContext.getCacheDir().getPath(),nFileInfo.getFileName());
                 raf = new RandomAccessFile(mFile,"rwd");
                raf.seek(start);
                Intent  intent = new Intent(DownLoadService.ACTION_UPDATE);
                mFinished +=mThreadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() ==HttpURLConnection.HTTP_OK){
                    //读取数据
                    input=conn.getInputStream();
                    byte[] buffer = new byte[1024*4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while((len=input.read(buffer))!= -1){
                        //写入文件
                        raf.write(buffer,0,len);
                        //把下载的进度广播给Activity
                        mFinished +=len;
                        if (System.currentTimeMillis()-time>500) {
                            time =System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / nFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //在暂停时 保存下载进度
                        if (isPause){
                            mThreadDAO.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mFinished);
                            return;
                        }
                    }
                    //删除线程ID
                    mThreadDAO.deleteThread(mThreadInfo.getUrl(),mThreadInfo.getId());


                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {

                try {
                    input.close();
                    raf.close();
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }











        }
    }
}
