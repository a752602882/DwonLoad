package org.ninebox.dwonload;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ninebox.entities.FileInfo;
import org.ninebox.services.DownLoadService;

import java.util.List;

/**
 * Created by Administrator on 2016/7/6.
 */
public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> mFileList;

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;
        final FileInfo fileInfo = mFileList.get(position);
        if (view==null) {
            view=  LayoutInflater.from(mContext).inflate(R.layout.list, null);
           //获得布局中的控件
            holder = new ViewHolder();
            holder.tvFile = (TextView) view.findViewById(R.id.tvFile);
            holder.btStop = (Button) view.findViewById(R.id.stopButton);
            holder.btStart = (Button) view.findViewById(R.id.downButton);
            holder.pbFile = (ProgressBar) view.findViewById(R.id.progressBar);
            view.setTag(holder);
            holder.tvFile.setText(fileInfo.getFileName());
            holder.pbFile.setMax(100);
            holder.btStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, DownLoadService.class);
                    intent.setAction(DownLoadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);

                }
            });
            holder.btStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, DownLoadService.class);
                    intent.setAction(DownLoadService.ACTION_STOP);
                    intent.putExtra("fileInfo", fileInfo);
                    mContext.startService(intent);
                }
            });
        }else {
            holder = (ViewHolder) view.getTag();
        }


        holder.pbFile.setProgress(fileInfo.getFinished());

        return view;
    }

//如果不定义为静态的，每次创建Item都会被创建一次，浪费内存空间
    static  class  ViewHolder{
        TextView tvFile;
        Button btStop,btStart;
        ProgressBar pbFile;
    }


    /**
     * 更细列表进度条
     */
    public  void updateProgress(int id,int progress){
        FileInfo fileInfo = mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }
}
