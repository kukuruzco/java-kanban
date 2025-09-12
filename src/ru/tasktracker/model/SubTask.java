package ru.tasktracker.model;

import java.util.ArrayList;

public class SubTask extends Task {
    private final int epicid;

    public SubTask(String taskName, String taskDescription, int epicid) {
        super(-1, taskName, taskDescription, StatusTask.NEW);
        this.epicid = epicid;
    }

    public SubTask(int id, String taskName, String taskDescription, StatusTask statusTask, int epicid) {
        super(id, taskName, taskDescription, statusTask);
        this.epicid = epicid;
    }

    public int getEpicid() {
        return epicid;
    }

    public SubTask withid(int newid) {
        return new SubTask(newid, getTaskName(), getTaskDescription(), getStatusTask(), epicid);
    }

    public SubTask withName(String newName) {
        return new SubTask(getid(), newName, getTaskDescription(), getStatusTask(), epicid);
    }

    public SubTask withDescription(String newDescription) {
        return new SubTask(getid(), getTaskName(), newDescription, getStatusTask(), epicid);
    }

    public SubTask withStatus(StatusTask newStatus) {
        return new SubTask(getid(), getTaskName(), getTaskDescription(), newStatus, epicid);
    }

    public SubTask withEpicid(int newEpicid) {
        return new SubTask(getid(), getTaskName(), getTaskDescription(), getStatusTask(), newEpicid);
    }

    @Override
    public String toString() {
        return super.toString() + " | Epicid: " + epicid;
    }
}
