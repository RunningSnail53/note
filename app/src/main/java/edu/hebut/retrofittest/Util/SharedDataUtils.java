package edu.hebut.retrofittest.Util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.DB.NoteDao;
import edu.hebut.retrofittest.supabase.dao.MindRecordDao;
import edu.hebut.retrofittest.supabase.dao.NoteDaoKt;
import edu.hebut.retrofittest.supabase.entity.MindRecord;
import edu.hebut.retrofittest.supabase.entity.Note;
import edu.hebut.retrofittest.supabase.entity.User;

public class SharedDataUtils {


    public static CompletableFuture<Void> init(User user) {
        loginUser = user;

        // 1. 异步获取用户的心情记录
        CompletableFuture<List<MindRecord>> mindRecordsFuture =
                MindRecordDao.getMindRecordsByUserId(user.getId());

        // 2. 异步获取用户的笔记列表
        CompletableFuture<List<NoteBean>> notesFuture =
                noteDao.queryNotesListByUserId(user.getId());

        // 3. 并行执行并等待两个任务完成
        return CompletableFuture.allOf(mindRecordsFuture, notesFuture)
                .thenAccept(ignored -> {
                    // 4. 设置结果到全局变量
                    mindRecordList = mindRecordsFuture.join();
                    noteBeanList = notesFuture.join();
                })
                .exceptionally(ex -> {
                    System.err.println("初始化失败: " + ex.getMessage());
                    return null;
                });
    }

    private static NoteDao noteDao = new NoteDao();
    private static User loginUser;

    private static List<MindRecord> mindRecordList;

    private static List<NoteBean> noteBeanList;

    public static User getLoginUser() {
        return loginUser;
    }


    public static List<MindRecord> getMindRecordList() {
        return mindRecordList;
    }

    public static List<NoteBean> getNoteBeanList() {
        return noteBeanList;
    }
}
