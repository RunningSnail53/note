package edu.hebut.retrofittest.DB;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import edu.hebut.retrofittest.Bean.NoteBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class NoteDao {
    Context context;
    noteDBHelper dbHelper;

    public NoteDao(Context context) {
        this.context = context;
        dbHelper = new noteDBHelper(context, "note.db", null, 1);
    }

    public void insertNote(NoteBean bean) {

        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("note_tittle", bean.getTitle());
        cv.put("note_content", bean.getContent());
        cv.put("note_type", bean.getType());
        cv.put("note_mark", bean.getMark());
        cv.put("createTime", bean.getCreateTime());
        cv.put("updateTime", bean.getUpdateTime());
        cv.put("note_owner", bean.getOwner());
        cv.put("year", bean.getYear());
        cv.put("month", bean.getMonth());
        cv.put("day", bean.getDay());
        cv.put("recordPath",bean.getRecordPath());
        cv.put("musicId",bean.getMusicId());
        cv.put("link",bean.getLink());
        cv.put("weather", bean.getWeather());
        cv.put("picPaths",new Gson().toJson(bean.getPicPaths()));
        sqLiteDatabase.insert("note_data", null, cv);
    }

    public int DeleteNote(int id) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        int ret = 0;
        ret = sqLiteDatabase.delete("note_data", "note_id=?", new String[]{id + ""});
        return ret;
    }

    public Cursor getAllData(String note_owner) {
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        String sql = "select * from note_data where note_owner=?";
        return sqLiteDatabase.rawQuery(sql, new String[]{note_owner});
    }

    public void updateNote(NoteBean note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("note_tittle", note.getTitle());
        cv.put("note_content", note.getContent());
        cv.put("note_type", note.getType());
        cv.put("note_mark", note.getMark());
        cv.put("updateTime", note.getUpdateTime());
        cv.put("year", note.getYear());
        cv.put("month", note.getMonth());
        cv.put("day", note.getDay());
        cv.put("recordPath",note.getRecordPath());
        cv.put("weather", note.getWeather());
        cv.put("link",note.getLink());
        cv.put("musicId",note.getMusicId());
        cv.put("picPaths",new Gson().toJson(note.getPicPaths()));
        db.update("note_data", cv, "note_id=?", new String[]{note.getId() + ""});
        db.close();
    }

    @SuppressLint("Range")
    public List<NoteBean> queryNotesByDate(String login_user, String updateTime) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<NoteBean> noteList = new ArrayList<>();
        NoteBean note;
        String sql;
        Cursor cursor = null;
        sql = "select * from note_data where note_owner=? and updateTime like '%"+updateTime+"%' order by note_id desc";
        cursor = db.rawQuery(sql, new String[]{login_user});
        while (cursor.moveToNext()) {
            note = new NoteBean();
            note.setId(cursor.getInt(cursor.getColumnIndex("note_id")));
            note.setTitle(cursor.getString(cursor.getColumnIndex("note_tittle")));
            note.setContent(cursor.getString(cursor.getColumnIndex("note_content")));
            note.setType(cursor.getString(cursor.getColumnIndex("note_type")));
            note.setMark(cursor.getInt(cursor.getColumnIndex("note_mark")));
            note.setYear(cursor.getString(cursor.getColumnIndex("year")));
            note.setMonth(cursor.getString(cursor.getColumnIndex("month")));
            note.setDay(cursor.getString(cursor.getColumnIndex("day")));
            note.setRecordPath(cursor.getString(cursor.getColumnIndex("recordPath")));
            note.setMusicId(cursor.getInt(cursor.getColumnIndex("musicId")));
            note.setLink(cursor.getString(cursor.getColumnIndex("link")));
            note.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
            note.setWeather(cursor.getInt(cursor.getColumnIndex("weather")));
            note.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
            List<String> list = new Gson().fromJson(cursor.getString(cursor.getColumnIndex("picPaths")), new TypeToken<List<String>>() {
            }.getType());
            note.setPicPaths(list);
            noteList.add(note);
        }

        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }

        return noteList;
    }


    @SuppressLint("Range")
    public List<NoteBean> queryNotesAll(String login_user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<NoteBean> noteList = new ArrayList<>();
        NoteBean note;
        String sql;
        Cursor cursor = null;
        sql = "select * from note_data where note_owner=? order by note_id desc";

        cursor = db.rawQuery(sql, new String[]{login_user});
        while (cursor.moveToNext()) {
            note = new NoteBean();
            note.setId(cursor.getInt(cursor.getColumnIndex("note_id")));
            note.setTitle(cursor.getString(cursor.getColumnIndex("note_tittle")));
            note.setContent(cursor.getString(cursor.getColumnIndex("note_content")));
            note.setType(cursor.getString(cursor.getColumnIndex("note_type")));
            note.setMark(cursor.getInt(cursor.getColumnIndex("note_mark")));
            note.setYear(cursor.getString(cursor.getColumnIndex("year")));
            note.setMonth(cursor.getString(cursor.getColumnIndex("month")));
            note.setDay(cursor.getString(cursor.getColumnIndex("day")));
            note.setRecordPath(cursor.getString(cursor.getColumnIndex("recordPath")));
            note.setMusicId(cursor.getInt(cursor.getColumnIndex("musicId")));
            note.setLink(cursor.getString(cursor.getColumnIndex("link")));
            note.setWeather(cursor.getInt(cursor.getColumnIndex("weather")));
            note.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
            note.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
            List<String> list = new Gson().fromJson(cursor.getString(cursor.getColumnIndex("picPaths")), new TypeToken<List<String>>() {
            }.getType());
            note.setPicPaths(list);
            noteList.add(note);
        }

        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }

        return noteList;
    }

    @SuppressLint("Range")
    public List<NoteBean> queryNotesAllByDate(String login_user, int mark, String year, String month, String day) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        List<NoteBean> noteList = new ArrayList<>();
        NoteBean note;
        String sql;
        Cursor cursor = null;
        sql = "select * from note_data where note_owner=? and  year='"+year+"' and  month='"+month+"' and  day='"+day+"' order by note_id desc";

        cursor = db.rawQuery(sql, new String[]{login_user});
        while (cursor.moveToNext()) {
            note = new NoteBean();
            note.setId(cursor.getInt(cursor.getColumnIndex("note_id")));
            note.setTitle(cursor.getString(cursor.getColumnIndex("note_tittle")));
            note.setContent(cursor.getString(cursor.getColumnIndex("note_content")));
            note.setType(cursor.getString(cursor.getColumnIndex("note_type")));
            note.setMark(cursor.getInt(cursor.getColumnIndex("note_mark")));
            note.setCreateTime(cursor.getString(cursor.getColumnIndex("createTime")));
            note.setUpdateTime(cursor.getString(cursor.getColumnIndex("updateTime")));
            note.setYear(cursor.getString(cursor.getColumnIndex("year")));
            note.setMonth(cursor.getString(cursor.getColumnIndex("month")));
            note.setDay(cursor.getString(cursor.getColumnIndex("day")));
            note.setWeather(cursor.getInt(cursor.getColumnIndex("weather")));

            List<String> list = new Gson().fromJson(cursor.getString(cursor.getColumnIndex("picPaths")), new TypeToken<List<String>>() {
            }.getType());
            note.setPicPaths(list);
            noteList.add(note);
        }

        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }

        return noteList;
    }

    public int countType(String login_user, int mark) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select count(*) from note_data where note_owner=? and note_mark=?";
        Cursor cursor = db.rawQuery(sql, new String[]{login_user, mark + ""});
        int i = 0;
        while (cursor.moveToNext()) {
            i = cursor.getInt(0);
        }
        return i;
    }


}
