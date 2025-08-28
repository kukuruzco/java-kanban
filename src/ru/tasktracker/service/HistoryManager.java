package ru.tasktracker.service;

import ru.tasktracker.model.Task;

import java.util.ArrayList;

public interface HistoryManager {
    public void addHistoryList(Task task);
    public ArrayList<Task> getHistory();
}

