package ru.tasktracker.service.managers;

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

    Task getTaskById(Integer id);

    void deleteSubTaskById(Integer subtaskId);

    void updateTask(Task updatedTask);

    SubTask getSubTaskById(Integer id);

    List<Epic> getAllEpics();

    Epic addEpic(Epic epic);

    void deleteAllEpics();

    void updateEpic(Epic updatedEpic);

    void deleteTaskById(Integer id);

    SubTask addSubTask(SubTask subtask);

    List<SubTask> getAllSubTasks();

    List<SubTask> getSubtasksByEpic(Integer epicId);

    Epic getEpicById(Integer id);

    void deleteEpicById(Integer epicId);

    void deleteAllSubTasks();

    void updateSubTask(SubTask updatedSubTask);

    boolean isTaskOverlap(Task task);
}

