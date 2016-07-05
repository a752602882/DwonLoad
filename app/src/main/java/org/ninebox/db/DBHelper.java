package org.ninebox.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/7/1. 定义一个数据库的帮助类
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static final int VERSION =1;
    private static final String CREATE_SQL="create table thread_info( id integer primary key autoincrement," +
            "thread_id integer,url text,start integer,end integer,finished integer);";
    private static final String DROP_SQL="drop table if exists thread_info";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
      db.execSQL(DROP_SQL);
      db.execSQL(CREATE_SQL);
    }
}
