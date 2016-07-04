package org.ninebox.db;

import org.ninebox.entities.ThreadInfo;

import java.util.List;

/**
 * Created by Administrator on 2016/7/1. 定义数据库线程访问接口
 */
public interface ThreadDAO {

    //插入线程信息
    public  void insertThread(ThreadInfo threadInfo);
    /**
     * 删除线程的方法
     * */
    public  void deleteThread(String url,int thread_id);

    /**
     *
     * @param url
     * @param thread_id  更新线程进度
     * @param finished
     */
    public  void updateThread(String url,int thread_id,int finished);

    /**
     *
     * @param url       查询线程信息
     * @return
     */
    public List<ThreadInfo> getThreads(String url);


    /**
     *
     * @param url
     * @param thread_id 查询线程是否存在
     * @return
     */
    public boolean isRxists(String url,int thread_id);
}
