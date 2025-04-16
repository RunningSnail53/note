package edu.hebut.retrofittest.Bean;

import java.io.Serializable;
import java.util.List;


public class NoteBean implements Serializable {

    private int id;
    private String title;
    private String content;
    private String type;
    private int mark;
    private String createTime;
    private String updateTime;



    private Long ownerId;
    private String owner;
    private String year;
    private String month;
    private String day;
    private String recordPath;
    private String link;
    private int musicId;
    private int weather;
    private List<String> picPaths;

    public List<String> getOriginPicPaths() {
        return originPicPaths;
    }

    private List<String> originPicPaths;

    public List<String> getPicPaths() {
        return picPaths;
    }

    public void setPicPaths(List<String> picPaths) {
        if(originPicPaths == null){
            originPicPaths = picPaths;
        }
        this.picPaths = picPaths;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRecordPath() {
        return recordPath;
    }

    public void setRecordPath(String recordPath) {
        this.recordPath = recordPath;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMark(){return mark;}

    public void setMark(int mark){this.mark = mark;};


    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }



    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }
}
