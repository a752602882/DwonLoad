package org.ninebox.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/1. 下载任务类
 */
public class DownloadTask {

    private FileInfo mFileInfo=null;
    private Context mContext=null;
    private ThreadDAO mThreadDAO;
    private  int mFinished=0;
    public  boolean isPause=false;
    private  int mThreadCount;

    private List<DownloadThread> mThreadList; //线程的集合



    public DownloadTask(FileInfo mFileInfo, Context mContext,int threadCount) {
        this.mFileInfo = mFileInfo;
        this.mContext = mContext;
        this.mThreadCount =threadCount;
        mThreadDAO =new ThreadDAOImpl(mContext);

     //   new DownloadThread()
    }


    public void download(){

        //从数据库获得下载进度
        List<ThreadInfo> threads = mThreadDAO.getThreads(mFileInfo.getUrl());
        if (threads.size()==0){
            //获得每个线程的下载长度
            int length = mFileInfo.getLength()/mThreadCount;
            for (int i =0;i<mThreadCount;i++){
                //创建线程信息
                ThreadInfo threadInfo = new ThreadInfo(i,mFileInfo.getUrl(),i*length,(i+1)*length-1,0);
                if (i==mThreadCount-1){
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //添加线程到集合里
                threads.add(threadInfo);
                mThreadDAO.insertThread(threadInfo );
            }
        }
        mThreadList = new ArrayList<DownloadThread>();
        //创建子线程进行下载
        for (ThreadInfo threadInfo:threads) {
          DownloadThread thread =    new DownloadThread(threadInfo);
          thread.start();
            //添加线程到集合中
          mThreadList.add(thread);
        }
    }

    class DownloadThread extends  Thread{


        private ThreadInfo mThreadInfo;
        private boolean isFinish=false;//标志线程执行完毕

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
                File  mFile = new File(mContext.getCacheDir().getPath(),mFileInfo.getFileName());
                raf = new RandomAccessFile(mFile,"rwd");
                raf.seek(start);
                Intent  intent = new Intent(DownLoadService.ACTION_UPDATE);
                mFinished +=mThreadInfo.getFinished();
                //开始下载

                if (conn.getResponseCode() ==HttpURLConnection.HTTP_PARTIAL){
                    //读取数据
                    input=conn.getInputStream();
                    byte[] buffer = new byte[1024*4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while((len=input.read(buffer))!= -1){
                        //写入文件
                        raf.write(buffer,0,len);
                        //累加整个文件的完成进度
                        mFinished +=len;
                        //累加每个线程的完成进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished()+len);

                        if (System.currentTimeMillis()-time>1000) {
                            Log.i("TAG","下载中：---------------------------------------"+mFileInfo+"");
                            Log.i("TAG","总大小：---------------------------------------"+ mFileInfo.getLength()+"");
                            Log.i("TAG","堕胎：---------------------------------------"+   mFinished * 100 / mFileInfo.getLength()+"");
                            time =System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //在暂停时 保存下载进度
                        if (isPause){
                            Log.i("TAG","暂停时：----------------------"+mThreadInfo.getFinished()+"---------------");
                            mThreadDAO.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mThreadInfo.getFinished());
                            Log.i("TAG","暂停后：----------------------"+mThreadInfo.getFinished()+"---------------");
                            return;
                        }
                    }

                    //标志线程执行完毕
                    isFinish = true;
                    //检查同一个文件多个线程是否下载完毕
                    checkThreadExecuteFinish();



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
        //synchronized保证同一时段只有一个线程能访问这个方法
        private  synchronized void checkThreadExecuteFinish(){
            boolean allFinished =true;
            for(DownloadThread thread:mThreadList){

                if (thread.isFinish==false){
                    allFinished=false;
                    break;
                }

            }
            if (allFinished){
                //删除线程ID
                mThreadDAO.deleteThread(mThreadInfo.getUrl());
                 //当所有线程执行完毕 发送通知UI下载认为结束
                Intent intent = new Intent(DownLoadService.ACTION_FINISH);
                intent.putExtra("fileInfo",mFileInfo);
                mContext.sendBroadcast(intent);
            }

        }

    }
}
