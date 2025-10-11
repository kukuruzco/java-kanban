package ru.tasktracker.service;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;

import java.util.List;

public interface TaskManager {
    List<Task> getPrioritizedTasks();

    List<Task> getHistory();

    Task addTask(Task task);

    List<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int id);

    void deleteSubTaskById(int subtaskId);

    void updateTask(Task updatedTask);

    SubTask getSubTaskById(int id);

    List<Epic> getAllEpics();

    Epic addEpic(Epic epic);

    void deleteAllEpics();

    void updateEpic(Epic updatedEpic);

    void deleteTaskById(int id);

    SubTask addSubTask(SubTask subtask);

    List<SubTask> getAllSubTasks();

    List<SubTask> getSubtasksByEpic(int epicId);

    Epic getEpicById(int id);

    void deleteEpicById(int epicId);

    void deleteAllSubTasks();

    void updateSubTask(SubTask updatedSubTask);

    boolean isTaskOverlap(Task task);
}

