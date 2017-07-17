package com.ms.favtodo.model;

/**
 * Created by MOHAMED SULAIMAN on 05-01-2017.
 */

public class TaskDetails {

    private String title="";
    private String time="";
    private String dateAndTime="";
    private int taskDone=0;
    private int taskId = 0;
    private int taskHour=0;
    private int taskMinute = 0;
    private long dateInMilliSeconds=0;
    private String date="";


    public int getTaskMinute() {
        return taskMinute;
    }

    public void setTaskMinute(int taskMinute) {
        this.taskMinute = taskMinute;
    }

    public int getTaskHour() {
        return taskHour;
    }

    public void setTaskHour(int taskHour) {
        this.taskHour = taskHour;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getDateInMilliSeconds() {
        return dateInMilliSeconds;
    }

    public void setDateInMilliSeconds(long dateInMilliSeconds) {
        this.dateInMilliSeconds = dateInMilliSeconds;
    }


    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getTaskDone() {
        return taskDone;
    }

    public void setTaskDone(int taskDone) {
        this.taskDone = taskDone;
    }

}
