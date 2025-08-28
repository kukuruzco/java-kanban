package ru.tasktracker.model;

import java.util.ArrayList;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String taskName, String taskDescription, int epicId) {
        super(-1, taskName, taskDescription, StatusTask.NEW);
        this.epicId = epicId;
    }

    public SubTask(int id, String taskName, String taskDescription, StatusTask statusTask, int epicId) {
        super(id, taskName, taskDescription, statusTask);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " | EpicID: " + epicId;
    }
}
