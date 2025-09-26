package ru.tasktracker.service;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    List<Task> getHistory();

    Task addTask(Task task);

    ArrayList<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    void deleteSubTaskById(int subtaskId);

    void updateTask(Task updatedTask);

    SubTask getSubTaskById(int id);

    ArrayList<Epic> getAllEpics();

    Epic addEpic(Epic epic);

    void deleteAllEpics();

    void updateEpic(Epic updatedEpic);

    void deleteTaskById(int id);

    SubTask addSubTask(SubTask subtask);

    ArrayList<SubTask> getAllSubTasks();

    List<SubTask> getSubtasksByEpic(int epicId);

    Epic getEpicById(int id);

    void deleteEpicById(int epicId);

    void deleteAllSubTasks();

    void updateSubTask(SubTask updatedSubTask);

}

