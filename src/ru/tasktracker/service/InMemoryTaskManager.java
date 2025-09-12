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
        int epicid = subtask.getEpicid();
        if (!epics.containsKey(epicid)) {
            System.out.println("Эпик с id " + epicid + " не существует");
            return null;
        }

        int newid = ++lastid;
        SubTask newSubTask = new SubTask(
                newid,
                subtask.getTaskName(),
                subtask.getTaskDescription(),
                subtask.getStatusTask(),
                subtask.getEpicid()
        );

        subtasks.put(newid, newSubTask);

        Epic epic = epics.get(epicid);
        ArrayList<Integer> newSubtaskids = new ArrayList<>(epic.getSubtaskids());
        newSubtaskids.add(newid);

        Epic updatedEpic = new Epic(
                epic.getid(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newSubtaskids
        );
        epics.put(epicid, updatedEpic);

        updateEpicStatus(epicid);
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
                    epic.getid(),
                    epic.getTaskName(),
                    epic.getTaskDescription(),
                    epic.getStatusTask(),
                    new ArrayList<>()
            );
            epics.put(epic.getid(), updatedEpic);
        }

        for (int epicid : epics.keySet()) {
            updateEpicStatus(epicid);
        }
    }


    @Override
    public Task getTaskByid(int id) {
        historyManager.addHistory(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicByid(int id) {
        historyManager.addHistory(epics.get(id));
        return epics.get(id);
    }

    @Override
    public SubTask getSubTaskByid(int id) {
        historyManager.addHistory(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public void deleteTaskByid(int id) {
        tasks.remove(id);
    }

    @Override
    public void deleteEpicByid(int epicid) {
        ArrayList<Integer> subtaskidsToRemove = new ArrayList<>();
        for (SubTask subTask : subtasks.values()) {
            if (subTask.getEpicid() == epicid) {
                subtaskidsToRemove.add(subTask.getid());
            }
        }
        for (Integer id : subtaskidsToRemove) {
            subtasks.remove(id);
        }
        epics.remove(epicid);
    }

    @Override
    public void deleteSubTaskByid(int subTaskid) {
        SubTask subTask = subtasks.get(subTaskid);
        if (subTask == null) {
            System.out.println("Подзадача с id " + subTaskid + " не найдена");
            return;
        }

        int epicid = subTask.getEpicid();
        subtasks.remove(subTaskid);
        historyManager.remove(subTaskid);

        Epic epic = epics.get(epicid);
        ArrayList<Integer> newSubtaskids = new ArrayList<>(epic.getSubtaskids());
        newSubtaskids.remove(Integer.valueOf(subTaskid));
        Epic updatedEpic = new Epic(
                epic.getid(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newSubtaskids
        );
        epics.put(epicid, updatedEpic);

        updateEpicStatus(epicid);
    }

    public void updateTask(Task updatedTask) {
        tasks.put(updatedTask.getid(), updatedTask);
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        int epicid = updatedEpic.getid();
        Epic existingEpic = epics.get(epicid);
        if (existingEpic == null) {
            System.out.println("Эпик не найден");
            return;
        }

        Epic newEpic = new Epic(
                existingEpic.getid(),
                updatedEpic.getTaskName(),
                updatedEpic.getTaskDescription(),
                existingEpic.getStatusTask(),
                existingEpic.getSubtaskids()
        );

        epics.put(updatedEpic.getid(), newEpic);
        updateEpicStatus(updatedEpic.getid());
    }

    public ArrayList<SubTask> getSubtasksByEpic(int epicid) {
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask subtask : subtasks.values()) {
            if (subtask.getEpicid() == epicid) {
                result.add(subtask);
            }
        }
        return result;
    }

    public void updateSubTask(SubTask updatedSubTask) {
        if (subtasks.containsKey(updatedSubTask.getid())) {
            subtasks.put(updatedSubTask.getid(), updatedSubTask);
            updateEpicStatus(updatedSubTask.getEpicid());
        } else {
            System.out.println("Ошибка: Подзадача не найдена!");
        }
    }

    private void updateEpicStatus(int epicid) {
        Epic epic = epics.get(epicid);
        ArrayList<SubTask> epicSubtasks = getSubtasksByEpic(epicid);
        StatusTask newEpicStatus = epic.updateStatus(epicSubtasks);

        Epic updatedEpic = new Epic(
                epic.getid(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                newEpicStatus,
                epic.getSubtaskids()
        );

        epics.put(epicid, updatedEpic);
    }
}

