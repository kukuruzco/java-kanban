package ru.tasktracker.service;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
    );
    protected int lastid = 0;
    private final HistoryManager historyManager;


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        if (task.getStartTime() != null && isTaskOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующими задачами: " + task);
        }

        int newid = ++lastid;
        Task newTask = new Task(
                newid,
                task.getTaskName(),
                task.getTaskDescription(),
                task.getStatusTask(),
                task.getStartTime(),
                task.getDuration()
        );
        tasks.put(newid, newTask);

        if (newTask.getStartTime() != null) {
            prioritizedTasks.add(newTask);
        }

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
        if (subtask.getStartTime() != null && isTaskOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующими задачами: " + subtask);
        }

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
                subtask.getEpicId(),
                subtask.getStartTime(),
                subtask.getDuration()
        );

        subtasks.put(newid, newSubTask);

        if (newSubTask.getStartTime() != null) {
            prioritizedTasks.add(newSubTask);
        }

        Epic epic = epics.get(epicId);
        List<Integer> newsubTaskIds = new ArrayList<>(epic.getSubTaskIds());
        newsubTaskIds.add(newid);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newsubTaskIds
        );
        epics.put(epicId, updatedEpic);

        updateEpicCalculatedFields(epicId);
        return newSubTask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values()
                .forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().forEach(prioritizedTasks::remove);

        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.clear();

        epics.values().forEach(epic -> {
            Epic updatedEpic = new Epic(
                    epic.getId(),
                    epic.getTaskName(),
                    epic.getTaskDescription(),
                    epic.getStatusTask(),
                    new ArrayList<>()
            );
            epics.put(epic.getId(), updatedEpic);
        });

        epics.keySet().forEach(this::updateEpicCalculatedFields);
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
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(int epicId) {
        List<SubTask> subTasksToRemove = subtasks.values().stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .toList();

        subTasksToRemove.forEach(subTask -> {
            subtasks.remove(subTask.getId());
            prioritizedTasks.remove(subTask);
            historyManager.remove(subTask.getId());
        });

        Epic epic = epics.remove(epicId);
        if (epic != null) {
            prioritizedTasks.remove(epic);
            historyManager.remove(epicId);
        }
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
        prioritizedTasks.remove(subTask);
        historyManager.remove(subtaskId);

        Epic epic = epics.get(epicId);
        List<Integer> newsubTaskIds = new ArrayList<>(epic.getSubTaskIds());
        newsubTaskIds.remove(Integer.valueOf(subtaskId));
        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newsubTaskIds
        );
        epics.put(epicId, updatedEpic);

        updateEpicCalculatedFields(epicId);
    }

    public void updateTask(Task updatedTask) {
        if (updatedTask.getStartTime() != null && isTaskOverlap(updatedTask)) {
            throw new IllegalArgumentException("Обновленная задача пересекается по времени с другими задачами: " + updatedTask);
        }

        Task oldTask = tasks.get(updatedTask.getId());
        if (oldTask != null) {
            prioritizedTasks.remove(oldTask);
        }

        tasks.put(updatedTask.getId(), updatedTask);

        if (updatedTask.getStartTime() != null) {
            prioritizedTasks.add(updatedTask);
        }
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
                existingEpic.getSubTaskIds()
        );

        epics.put(updatedEpic.getId(), newEpic);
        updateEpicCalculatedFields(updatedEpic.getId());
    }

    public List<SubTask> getSubtasksByEpic(int epicId) {
        return subtasks.values().stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .toList();
    }

    public void updateSubTask(SubTask updatedSubTask) {
        if (updatedSubTask.getStartTime() != null && isTaskOverlap(updatedSubTask)) {
            throw new IllegalArgumentException("Обновленная подзадача пересекается по времени с другими задачами: " + updatedSubTask);
        }

        if (subtasks.containsKey(updatedSubTask.getId())) {
            SubTask oldSubTask = subtasks.get(updatedSubTask.getId());

            prioritizedTasks.remove(oldSubTask);

            subtasks.put(updatedSubTask.getId(), updatedSubTask);

            if (updatedSubTask.getStartTime() != null) {
                prioritizedTasks.add(updatedSubTask);
            }

            updateEpicCalculatedFields(updatedSubTask.getEpicId());
        } else {
            System.out.println("Ошибка: Подзадача не найдена!");
        }
    }

    public void updateEpicCalculatedFields(int epicId) {
        Epic epic = epics.get(epicId);
        List<SubTask> epicSubtasks = getSubtasksByEpic(epicId);

        LocalDateTime startTime = calculateEpicStartTime(epicSubtasks);
        LocalDateTime endTime = calculateEpicEndTime(epicSubtasks);
        Duration duration = calculateEpicDuration(epicSubtasks);
        StatusTask status = calculateEpicStatus(epicSubtasks);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                status,
                epic.getSubTaskIds(),
                startTime,
                duration,
                endTime
        );

        epics.put(epicId, updatedEpic);
    }

    private LocalDateTime calculateEpicStartTime(List<SubTask> epicSubtasks) {
        return epicSubtasks.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime calculateEpicEndTime(List<SubTask> epicSubtasks) {
        return epicSubtasks.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime calculateSubTaskEndTime(SubTask subTask) {
        return subTask.getEndTime();
    }

    private Duration calculateEpicDuration(List<SubTask> epicSubtasks) {
        return epicSubtasks.stream()
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus);
    }

    private StatusTask calculateEpicStatus(List<SubTask> epicSubtasks) {
        if (epicSubtasks.isEmpty()) {
            return StatusTask.NEW;
        }

        boolean allNew = epicSubtasks.stream().allMatch(st -> st.getStatusTask() == StatusTask.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(st -> st.getStatusTask() == StatusTask.DONE);
        boolean hasInProgress = epicSubtasks.stream().anyMatch(st -> st.getStatusTask() == StatusTask.IN_PROGRESS);

        if (hasInProgress) return StatusTask.IN_PROGRESS;
        if (allDone) return StatusTask.DONE;
        if (allNew) return StatusTask.NEW;
        return StatusTask.IN_PROGRESS;
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
                task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }

        return task1.getEndTime().isAfter(task2.getStartTime()) &&
                task2.getEndTime().isAfter(task1.getStartTime());
    }

    @Override
    public boolean isTaskOverlap(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }

        return prioritizedTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .filter(existingTask -> existingTask.getStartTime() != null)
                .anyMatch(existingTask -> isTasksOverlap(task, existingTask));
    }

}

