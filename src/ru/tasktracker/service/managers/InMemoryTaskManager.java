package ru.tasktracker.service.managers;

import ru.tasktracker.exceptions.NotFoundException;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(LocalDateTime::compareTo))
                    .thenComparing(Task::getId)
    );
    protected Integer lastId = 0;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void setLastId(Integer id) {
        this.lastId = id;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Task addTask(Task task) {
        // Проверяем, что задача не имеет ID (новая задача)
        if (task.getId() != null) {
            throw new IllegalArgumentException("Новая задача не должна иметь ID");
        }

        if (task.getStartTime() != null && isTaskOverlap(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующими задачами: " + task);
        }

        // Генерируем новый ID
        Integer newId = ++lastId;

        // Создаем новую задачу с сгенерированным ID
        Task newTask = new Task(
                newId,
                task.getTaskName(),
                task.getTaskDescription(),
                task.getStatusTask(),
                task.getStartTime(),
                task.getDuration()
        );
        tasks.put(newId, newTask);

        if (newTask.getStartTime() != null) {
            prioritizedTasks.add(newTask);
        }

        return newTask;
    }

    @Override
    public Epic addEpic(Epic epic) {
        // Проверяем, что эпик не имеет ID (новый эпик)
        if (epic.getId() != null) {
            throw new IllegalArgumentException("Новый эпик не должен иметь ID");
        }

        Integer newId = ++lastId;
        Epic newEpic = new Epic(
                newId,
                epic.getTaskName(),
                epic.getTaskDescription(),
                StatusTask.NEW
        );
        epics.put(newId, newEpic);
        return newEpic;
    }

    @Override
    public SubTask addSubTask(SubTask subtask) {
        // Проверяем, что подзадача не имеет ID (новая подзадача)
        if (subtask.getId() != null) {
            throw new IllegalArgumentException("Новая подзадача не должна иметь ID");
        }

        if (subtask.getStartTime() != null && isTaskOverlap(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующими задачами: " + subtask);
        }

        Integer epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            throw new NotFoundException("Эпик с id " + epicId + " не найден");
        }

        Integer newId = ++lastId;
        SubTask newSubTask = new SubTask(
                newId,
                subtask.getTaskName(),
                subtask.getTaskDescription(),
                subtask.getStatusTask(),
                subtask.getEpicId(),
                subtask.getStartTime(),
                subtask.getDuration()
        );

        subtasks.put(newId, newSubTask);

        if (newSubTask.getStartTime() != null) {
            prioritizedTasks.add(newSubTask);
        }

        Epic epic = epics.get(epicId);
        List<Integer> newSubTaskIds = new ArrayList<>(epic.getSubTaskIds());
        newSubTaskIds.add(newId);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newSubTaskIds
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
        tasks.values().stream()
                .filter(task -> task.getStartTime() != null)
                .forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.values().stream()
                .filter(subTask -> subTask.getStartTime() != null)
                .forEach(prioritizedTasks::remove);

        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.values().stream()
                .filter(subTask -> subTask.getStartTime() != null)
                .forEach(prioritizedTasks::remove);
        subtasks.clear();

        epics.values().forEach(epic -> {
            Epic updatedEpic = new Epic(
                    epic.getId(),
                    epic.getTaskName(),
                    epic.getTaskDescription(),
                    StatusTask.NEW,
                    new ArrayList<>()
            );
            epics.put(epic.getId(), updatedEpic);
        });

        epics.keySet().forEach(this::updateEpicCalculatedFields);
    }

    @Override
    public Task getTaskById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        if (!tasks.containsKey(id)) {
            throw new NotFoundException("Задача с id " + id + " не найдена");
        }
        Task task = tasks.get(id);
        historyManager.addHistory(task);
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        if (!epics.containsKey(id)) {
            throw new NotFoundException("Эпик с id " + id + " не найден");
        }
        Epic epic = epics.get(id);
        historyManager.addHistory(epic);
        return epic;
    }

    @Override
    public SubTask getSubTaskById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        if (!subtasks.containsKey(id)) {
            throw new NotFoundException("Подзадача с id " + id + " не найдена");
        }
        SubTask subtask = subtasks.get(id);
        historyManager.addHistory(subtask);
        return subtask;
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (!tasks.containsKey(id)) {
            throw new NotFoundException("Задача с id " + id + " не найдена");
        }

        Task task = tasks.get(id);
        tasks.remove(id);

        if (task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }

        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(Integer epicId) {
        if (!epics.containsKey(epicId)) {
            throw new NotFoundException("Эпик с id " + epicId + " не найден");
        }

        Epic epic = epics.get(epicId);

        List<SubTask> subTasksToRemove = subtasks.values().stream()
                .filter(subTask -> Objects.equals(subTask.getEpicId(), epicId))
                .toList();

        subTasksToRemove.forEach(subTask -> {
            subtasks.remove(subTask.getId());
            if (subTask.getStartTime() != null) {
                prioritizedTasks.remove(subTask);
            }
            historyManager.remove(subTask.getId());
        });

        epics.remove(epicId);

        if (epic.getStartTime() != null) {
            prioritizedTasks.remove(epic);
        }

        historyManager.remove(epicId);
    }

    @Override
    public void deleteSubTaskById(Integer subtaskId) {
        if (!subtasks.containsKey(subtaskId)) {
            throw new NotFoundException("Подзадача с id " + subtaskId + " не найдена");
        }

        SubTask subTask = subtasks.get(subtaskId);
        Integer epicId = subTask.getEpicId();

        subtasks.remove(subtaskId);

        if (subTask.getStartTime() != null) {
            prioritizedTasks.remove(subTask);
        }

        historyManager.remove(subtaskId);

        Epic epic = epics.get(epicId);
        List<Integer> newSubTaskIds = new ArrayList<>(epic.getSubTaskIds());
        newSubTaskIds.remove(Integer.valueOf(subtaskId));

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                epic.getStatusTask(),
                newSubTaskIds
        );
        epics.put(epicId, updatedEpic);

        updateEpicCalculatedFields(epicId);
    }

    @Override
    public void updateTask(Task updatedTask) {
        Integer taskId = updatedTask.getId();
        if (taskId == null) {
            throw new IllegalArgumentException("Обновляемая задача должна иметь ID");
        }

        if (!tasks.containsKey(taskId)) {
            throw new NotFoundException("Задача с id " + taskId + " не найдена");
        }

        // Для проверки пересечений временно создаем задачу с тем же ID
        if (updatedTask.getStartTime() != null && isTaskOverlap(updatedTask)) {
            throw new IllegalArgumentException("Обновленная задача пересекается по времени с другими задачами: " + updatedTask);
        }

        Task oldTask = tasks.get(taskId);

        if (oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }

        // Сохраняем обновленную задачу (не создаем новую!)
        tasks.put(taskId, updatedTask);

        if (updatedTask.getStartTime() != null) {
            prioritizedTasks.add(updatedTask);
        }
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        Integer epicId = updatedEpic.getId();
        if (epicId == null) {
            throw new IllegalArgumentException("Обновляемый эпик должен иметь ID");
        }

        if (!epics.containsKey(epicId)) {
            throw new NotFoundException("Эпик с id " + epicId + " не найден");
        }

        Epic existingEpic = epics.get(epicId);

        Epic newEpic = new Epic(
                existingEpic.getId(),
                updatedEpic.getTaskName(),
                updatedEpic.getTaskDescription(),
                existingEpic.getStatusTask(),
                existingEpic.getSubTaskIds(),
                existingEpic.getStartTime(),
                existingEpic.getDuration(),
                existingEpic.getEndTime()
        );

        epics.put(epicId, newEpic);
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        Integer subtaskId = updatedSubTask.getId();
        if (subtaskId == null) {
            throw new IllegalArgumentException("Обновляемая подзадача должна иметь ID");
        }

        if (!subtasks.containsKey(subtaskId)) {
            throw new NotFoundException("Подзадача с id " + subtaskId + " не найдена");
        }

        // Для проверки пересечений временно создаем подзадачу с тем же ID
        if (updatedSubTask.getStartTime() != null && isTaskOverlap(updatedSubTask)) {
            throw new IllegalArgumentException("Обновленная подзадача пересекается по времени с другими задачами: " + updatedSubTask);
        }

        SubTask oldSubTask = subtasks.get(subtaskId);

        if (oldSubTask.getStartTime() != null) {
            prioritizedTasks.remove(oldSubTask);
        }

        // Сохраняем обновленную подзадачу (не создаем новую!)
        subtasks.put(subtaskId, updatedSubTask);

        if (updatedSubTask.getStartTime() != null) {
            prioritizedTasks.add(updatedSubTask);
        }

        updateEpicCalculatedFields(updatedSubTask.getEpicId());
    }

    @Override
    public List<SubTask> getSubtasksByEpic(Integer epicId) {
        if (epicId == null || !epics.containsKey(epicId)) {
            return new ArrayList<>();
        }
        return subtasks.values().stream()
                .filter(subTask -> epicId.equals(subTask.getEpicId()))
                .toList();
    }

    public void updateEpicCalculatedFields(Integer epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }

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

        if (allNew) {
            return StatusTask.NEW;
        }
        if (allDone) {
            return StatusTask.DONE;
        }
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
                .filter(existingTask -> !Objects.equals(existingTask.getId(), task.getId()))
                .filter(existingTask -> existingTask.getStartTime() != null)
                .anyMatch(existingTask -> isTasksOverlap(task, existingTask));
    }
}