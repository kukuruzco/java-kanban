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

    Task getTaskByid(int id);

    void updateTask(Task updatedTask);

    void deleteTaskByid(int id);

    ArrayList<Epic> getAllEpics();

    Epic addEpic(Epic epic);

    void deleteAllEpics();

    Epic getEpicByid(int id);

    void updateEpic(Epic updatedEpic);

    void deleteEpicByid(int epicid);

    SubTask addSubTask(SubTask subtask);

    ArrayList<SubTask> getAllSubTasks();

    List<SubTask> getSubtasksByEpic(int epicid);

    SubTask getSubTaskByid(int id);

    void deleteSubTaskByid(int subTaskid);

    void deleteAllSubTasks();

    void updateSubTask(SubTask updatedSubTask);

}

