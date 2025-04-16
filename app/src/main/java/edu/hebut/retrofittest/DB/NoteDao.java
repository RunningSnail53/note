package edu.hebut.retrofittest.DB;

import android.annotation.SuppressLint;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.hebut.retrofittest.Bean.NoteBean;
import edu.hebut.retrofittest.Util.DateUtils;
import edu.hebut.retrofittest.supabase.dao.MindRecordDao;
import edu.hebut.retrofittest.supabase.dao.MusicDao;
import edu.hebut.retrofittest.supabase.dao.NoteDaoKt;
import edu.hebut.retrofittest.supabase.dao.PhotoDao;
import edu.hebut.retrofittest.supabase.dao.UserDao;
import edu.hebut.retrofittest.supabase.entity.MindRecord;
import edu.hebut.retrofittest.supabase.entity.Music;
import edu.hebut.retrofittest.supabase.entity.Note;
import edu.hebut.retrofittest.supabase.entity.Photo;
import edu.hebut.retrofittest.supabase.entity.User;
import edu.hebut.retrofittest.supabase.entity.Weather;
import kotlin.Unit;


public class NoteDao {

    public CompletableFuture<Void> insertNote(NoteBean noteBean) {
        // 创建Note对象
        Note note = new Note();
        note.setId(noteBean.getId());
        note.setTitle(noteBean.getTitle());
        note.setContent(noteBean.getContent());
        note.setLink(noteBean.getLink());
        note.setCreateTime(DateUtils.valueOf(noteBean.getCreateTime()));
        note.setUpdateTime(DateUtils.valueOf(noteBean.getUpdateTime()));
        note.setUser_id(noteBean.getOwnerId());
        note.setDay(Integer.valueOf(noteBean.getDay()));
        note.setMonth(Integer.valueOf(noteBean.getMonth()));
        note.setYear(Integer.valueOf(noteBean.getYear()));
        note.setWeather(Weather.values()[noteBean.getWeather()]);
        note.setRecordPath(noteBean.getRecordPath());

        // 异步插入Note
        return NoteDaoKt.insertNote(note)
                .thenCompose(insertedNote -> {
                    // 插入成功后，处理图片
                    List<CompletableFuture<Unit>> photoFutures = noteBean.getPicPaths().stream()
                            .map(path -> {
                                Photo photo = new Photo();
                                photo.setNote_id(noteBean.getId());
                                photo.decodeUrl(path);
                                return PhotoDao.insertPhoto(photo);
                            })
                            .collect(Collectors.toList());

                    // 等待所有图片插入完成
                    return CompletableFuture.allOf(photoFutures.toArray(new CompletableFuture[0]));
                })
                .exceptionally(ex -> {
                    // 统一错误处理
                    System.err.println("Failed to insert note or photos: " + ex.getMessage());
                    throw new CompletionException(ex);
                });
    }

    public CompletableFuture<Integer> deleteNote(Long noteId) {
        // 1. 先删除关联数据（Photo, Music, MindRecord）
        CompletableFuture<Unit> deletePhotos = PhotoDao.deletePhotoByNoteId(noteId);
        CompletableFuture<Unit> deleteMusic = MusicDao.deleteMusicByNoteId(noteId);
        CompletableFuture<Unit> deleteMindRecords = MindRecordDao.deleteMindRecordByNoteId(noteId);

        // 2. 等待所有关联数据删除完成，再删除 Note
        return CompletableFuture.allOf(deletePhotos, deleteMusic, deleteMindRecords)
                .thenCompose(ignored -> NoteDaoKt.deleteNoteById(noteId))
                .thenApply(ret -> 1) // 成功返回 1
                .exceptionally(ex -> {
                    System.err.println("Failed to delete note: " + ex.getMessage());
                    return -1; // 失败返回 -1
                });
    }
    public CompletableFuture<Void> updateNote(NoteBean noteBean) {
        // 1. 构造 Note 对象
        Note note = new Note();
        note.setId(noteBean.getId());
        note.setTitle(noteBean.getTitle());
        note.setContent(noteBean.getContent());
        note.setLink(noteBean.getLink());
        note.setCreateTime(DateUtils.valueOf(noteBean.getCreateTime()));
        note.setUpdateTime(DateUtils.valueOf(noteBean.getUpdateTime()));
        note.setUser_id(noteBean.getOwnerId());
        note.setDay(Integer.valueOf(noteBean.getDay()));
        note.setMonth(Integer.valueOf(noteBean.getMonth()));
        note.setYear(Integer.valueOf(noteBean.getYear()));
        note.setWeather(Weather.values()[noteBean.getWeather()]);
        note.setRecordPath(noteBean.getRecordPath());

        // 2. 异步更新 Note
        return NoteDaoKt.updateNote(note)
                .thenCompose(ignored -> {
                    // 3. 异步插入或更新图片（并行执行）
                    List<CompletableFuture<Unit>> photoFutures = noteBean.getPicPaths().stream()
                            .map(path -> {
                                Photo photo = new Photo();
                                photo.setNote_id(noteBean.getId());
                                photo.decodeUrl(path);
                                return PhotoDao.insertPhoto(photo);
                            })
                            .collect(Collectors.toList());

                    return CompletableFuture.allOf(photoFutures.toArray(new CompletableFuture[0]));
                })
                .exceptionally(ex -> {
                    System.err.println("Failed to update note or photos: " + ex.getMessage());
                    throw new CompletionException(ex); // 或者返回 null 并记录日志
                });
    }

    @SuppressLint("Range")
    public CompletableFuture<List<NoteBean>> queryNotesByUserIdAndDate(Long userId, String updateTime) {
        // 1. 异步获取用户信息
        CompletableFuture<User> userFuture = UserDao.getUserById(userId);

        // 2. 异步获取符合条件的笔记列表
        CompletableFuture<List<Note>> notesFuture = NoteDaoKt.getNoteListByUserIdAndDate(userId, updateTime);

        // 3. 组合结果并处理
        return userFuture.thenCombine(notesFuture, (user, notes) -> {
                    // 4. 对每个笔记，异步获取关联数据（MindRecord, Photos, Music）
                    List<CompletableFuture<NoteBean>> noteBeanFutures = notes.stream()
                            .map(note -> {
                                CompletableFuture<MindRecord> mindRecordFuture =
                                        MindRecordDao.getMindRecordByNoteId(note.getId());
                                CompletableFuture<List<Photo>> photosFuture =
                                        PhotoDao.getPhotosByNoteId(note.getId());
                                CompletableFuture<List<Music>> musicsFuture =
                                        MusicDao.getMusicListByNoteId(note.getId());

                                // 5. 组合所有关联数据并构建 NoteBean
                                return CompletableFuture.allOf(mindRecordFuture, photosFuture, musicsFuture)
                                        .thenApply(ignored -> {
                                            MindRecord mindRecord = mindRecordFuture.join();
                                            List<Photo> photos = photosFuture.join();
                                            List<Music> musics = musicsFuture.join();

                                            NoteBean noteBean = new NoteBean();
                                            noteBean.setOwner(user.getName());
                                            noteBean.setOwnerId(user.getId());
                                            noteBean.setId((int) note.getId());
                                            noteBean.setTitle(note.getTitle());
                                            noteBean.setContent(note.getContent());
                                            noteBean.setCreateTime(note.getCreateTime().toString());
                                            noteBean.setUpdateTime(note.getUpdateTime().toString());
                                            noteBean.setLink(note.getLink());
                                            noteBean.setRecordPath(note.getRecordPath());
                                            noteBean.setWeather(note.getWeather().ordinal());
                                            noteBean.setMark(mindRecord.getName().ordinal());
                                            noteBean.setYear(note.getYear().toString());
                                            noteBean.setMonth(note.getMonth().toString());
                                            noteBean.setDay(note.getDay().toString());
                                            noteBean.setPicPaths(photos.stream()
                                                    .map(Photo::getPublicUrl)
                                                    .collect(Collectors.toList()));
                                            noteBean.setMusicId((int) musics.get(0).getId());

                                            return noteBean;
                                        });
                            })
                            .collect(Collectors.toList());

                    // 6. 等待所有 NoteBean 构建完成
                    return CompletableFuture.allOf(noteBeanFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> noteBeanFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                })
                .thenCompose(Function.identity()) // 展平嵌套的 CompletableFuture
                .exceptionally(ex -> {
                    System.err.println("Failed to query notes: " + ex.getMessage());
                    return Collections.emptyList(); // 返回空列表或抛出异常
                });
    }

    @SuppressLint("Range")
    public CompletableFuture<List<NoteBean>> queryNotesListByUserId(Long userId) {
        // 1. 异步获取用户信息
        CompletableFuture<User> userFuture = UserDao.getUserById(userId);

        // 2. 异步获取用户的所有笔记
        CompletableFuture<List<Note>> notesFuture = NoteDaoKt.getNoteListByUserId(userId);

        // 3. 组合用户信息和笔记列表
        return userFuture.thenCombine(notesFuture, (user, notes) -> {
                    // 4. 对每个笔记，异步获取关联数据（MindRecord, Photos, Music）
                    List<CompletableFuture<NoteBean>> noteBeanFutures = notes.stream()
                            .map(note -> {
                                CompletableFuture<MindRecord> mindRecordFuture =
                                        MindRecordDao.getMindRecordByNoteId(note.getId());
                                CompletableFuture<List<Photo>> photosFuture =
                                        PhotoDao.getPhotosByNoteId(note.getId());
                                CompletableFuture<List<Music>> musicsFuture =
                                        MusicDao.getMusicListByNoteId(note.getId());

                                // 5. 组合所有关联数据并构建 NoteBean
                                return CompletableFuture.allOf(mindRecordFuture, photosFuture, musicsFuture)
                                        .thenApply(ignored -> {
                                            MindRecord mindRecord = mindRecordFuture.join();
                                            List<Photo> photos = photosFuture.join();
                                            List<Music> musics = musicsFuture.join();

                                            NoteBean noteBean = new NoteBean();
                                            noteBean.setOwner(user.getName());
                                            noteBean.setOwnerId(user.getId());
                                            noteBean.setId((int) note.getId());
                                            noteBean.setTitle(note.getTitle());
                                            noteBean.setContent(note.getContent());
                                            noteBean.setCreateTime(note.getCreateTime().toString());
                                            noteBean.setUpdateTime(note.getUpdateTime().toString());
                                            noteBean.setLink(note.getLink());
                                            noteBean.setRecordPath(note.getRecordPath());
                                            noteBean.setWeather(note.getWeather().ordinal());
                                            noteBean.setMark(mindRecord.getName().ordinal());
                                            noteBean.setYear(note.getYear().toString());
                                            noteBean.setMonth(note.getMonth().toString());
                                            noteBean.setDay(note.getDay().toString());
                                            noteBean.setPicPaths(photos.stream()
                                                    .map(Photo::getPublicUrl)
                                                    .collect(Collectors.toList()));

                                            // 安全获取音乐ID（避免空指针）
                                            noteBean.setMusicId(musics.isEmpty() ? 0 : (int) musics.get(0).getId());

                                            return noteBean;
                                        });
                            })
                            .collect(Collectors.toList());

                    // 6. 等待所有 NoteBean 构建完成
                    return CompletableFuture.allOf(noteBeanFutures.toArray(new CompletableFuture[0]))
                            .thenApply(ignored -> noteBeanFutures.stream()
                                    .map(CompletableFuture::join)
                                    .collect(Collectors.toList()));
                })
                .thenCompose(Function.identity()) // 展平嵌套的 CompletableFuture
                .exceptionally(ex -> {
                    System.err.println("Failed to query notes: " + ex.getMessage());
                    return Collections.emptyList(); // 返回空列表或抛出异常
                });
    }
}
