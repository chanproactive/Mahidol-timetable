package org.mahidol;

import javafx.beans.property.SimpleStringProperty;

public class SubjectData {
    private SimpleStringProperty tableName;
    private SimpleStringProperty code;
    private SimpleStringProperty course;
    private int credits;
    private int startTime;
    private int finishTime;
    private SimpleStringProperty room;
    private int day;
    private SimpleStringProperty details;
    private SimpleStringProperty stringTime;

    public SubjectData(String code, String course, int credits, int startTime,
                       int finishTime, String room, int day, String details) {
        this.code = new SimpleStringProperty(code);
        this.course = new SimpleStringProperty(course);
        this.credits = credits;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.room = new SimpleStringProperty(room);
        this.day = day;
        this.details = new SimpleStringProperty(details);
        this.stringTime = new SimpleStringProperty(mapStringTime());
    }

    public String getTableName() {
        return tableName.get();
    }

    public SimpleStringProperty tableNameProperty() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName.set(tableName);
    }

    public String getCode() {
        return code.get();
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code.set(code);
    }

    public String getCourse() {
        return course.get();
    }

    public SimpleStringProperty courseProperty() {
        return course;
    }

    public void setCourse(String course) {
        this.course.set(course);
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public String getRoom() {
        return room.get();
    }

    public SimpleStringProperty roomProperty() {
        return room;
    }

    public void setRoom(String room) {
        this.room.set(room);
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getDetails() {
        return details.get();
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public String getStringTime() {
        return stringTime.get();
    }

    public SimpleStringProperty stringTimeProperty() {
        return stringTime;
    }

    public void setStringTime(String stringTime) {
        this.stringTime.set(stringTime);
    }

    private String mapStringTime(){
        String start = (startTime/60)+":"+(startTime%60==0? "00": startTime%60);
        String finish = (finishTime/60)+":"+(finishTime%60==0? "00": finishTime%60);
        return start+"-"+finish;
    }
}