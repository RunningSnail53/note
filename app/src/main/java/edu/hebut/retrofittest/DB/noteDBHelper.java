package edu.hebut.retrofittest.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class noteDBHelper extends SQLiteOpenHelper {
    public noteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String sql = "create table if not exists note_data(" +
                "note_id integer primary key autoincrement," +
                "note_tittle varchar," +
                "note_content varchar," +
                "note_type varchar," +
                "note_mark varchar," +
                "createTime varchar," +
                "year varchar," +
                "month varchar," +
                "day varchar," +
                "updateTime varchar," +
                "weather integer," +
                "note_owner varchar," +
                "picPaths varchar," +
                "recordPath varchar," +
                "link varchar," +
                "musicId integer," +
                "mind integer)";
        sqLiteDatabase.execSQL(sql);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
