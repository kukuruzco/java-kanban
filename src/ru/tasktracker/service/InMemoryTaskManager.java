package ru.tasktracker.service;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    protected int lastid = 0;
    private final HistoryManager historyManager;


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    protected void setLastId(int id) {
        this.lastid = id;
    }
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task addTask(Task task) {
        int newid = ++lastid;
        Task newTask = new Task(
                newid,
                task.getTaskName(),
                task.getTaskDescription(),
                task.getStatusTask()
        );
        tasks.put(newid, newTask);
        return newTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        int newid = ++lastid;
        Epic newEpic = new Epic(
                newid,
                epic.getTaskName(),
                epic.getTaskDescription(),
                StatusTask.NEW
        );
        epics.put(newid, newEpic);
        return newEpic;
    }

    @Override
    public SubTask addSubTask(SubTask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            System.out.println("Эпик с id " + epicId + " не существует");
            return null;
        }

        int newid = ++lastid;
        SubTask newSubTask = new SubTask(
                newid,
                subtask.getTaskName(),
                subtask.getTaskDescription(),
                subtask.getStatusTask(),
                subtask.getEpicId()
        );

        subtasks.put(newid, newSubTask);

        Epic epic = epics.get(epicId);
        ArrayList<Integer> newsubtaskIds = new ArrayList<>(epic.getsubtaskIds());
        newsubtaskIds.add(newid);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newsubtaskIds
        );
        epics.put(epicId, updatedEpic);

        updateEpicStatus(epicId);
        return newSubTask;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            Epic updatedEpic = new Epic(
                    epic.getId(),
                    epic.getTaskName(),
                    epic.getTaskDescription(),
                    epic.getStatusTask(),
                    new ArrayList<>()
            );
            epics.put(epic.getId(), updatedEpic);
        }

        for (int epicId : epics.keySet()) {
            updateEpicStatus(epicId);
        }
    }


    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addHistory(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addHistory(epic);
        }
        return epic;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addHistory(subtask);
        }
        return subtask;
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int epicId) {
        ArrayList<Integer> subtaskIdsToRemove = new ArrayList<>();
        for (SubTask subTask : subtasks.values()) {
            if (subTask.getEpicId() == epicId) {
                subtaskIdsToRemove.add(subTask.getId());
            }
        }
        for (Integer id : subtaskIdsToRemove) {
            subtasks.remove(id);
        }
        epics.remove(epicId);
    }

    @Override
    public void deleteSubTaskById(int subtaskId) {
        SubTask subTask = subtasks.get(subtaskId);
        if (subTask == null) {
            System.out.println("Подзадача с id " + subtaskId + " не найдена");
            return;
        }

        int epicId = subTask.getEpicId();
        subtasks.remove(subtaskId);
        historyManager.remove(subtaskId);

        Epic epic = epics.get(epicId);
        ArrayList<Integer> newsubtaskIds = new ArrayList<>(epic.getsubtaskIds());
        newsubtaskIds.remove(Integer.valueOf(subtaskId));
        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newsubtaskIds
        );
        epics.put(epicId, updatedEpic);

        updateEpicStatus(epicId);
    }

    public void updateTask(Task updatedTask) {
        tasks.put(updatedTask.getId(), updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int epicId = updatedEpic.getId();
        Epic existingEpic = epics.get(epicId);
        if (existingEpic == null) {
            System.out.println("Эпик не найден");
            return;
        }

        Epic newEpic = new Epic(
                existingEpic.getId(),
                updatedEpic.getTaskName(),
                updatedEpic.getTaskDescription(),
                existingEpic.getStatusTask(),
                existingEpic.getsubtaskIds()
        );

        epics.put(updatedEpic.getId(), newEpic);
        updateEpicStatus(updatedEpic.getId());
    }

    public ArrayList<SubTask> getSubtasksByEpic(int epicId) {
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                result.add(subtask);
            }
        }
        return result;
    }

    public void updateSubTask(SubTask updatedSubTask) {
        if (subtasks.containsKey(updatedSubTask.getId())) {
            subtasks.put(updatedSubTask.getId(), updatedSubTask);
            updateEpicStatus(updatedSubTask.getEpicId());
        } else {
            System.out.println("Ошибка: Подзадача не найдена!");
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<SubTask> epicSubtasks = getSubtasksByEpic(epicId);
        StatusTask newEpicStatus = epic.updateStatus(epicSubtasks);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                newEpicStatus,
                epic.getsubtaskIds()
        );

        epics.put(epicId, updatedEpic);
    }
}

