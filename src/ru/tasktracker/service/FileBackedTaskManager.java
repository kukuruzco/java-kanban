package ru.tasktracker.service;

import ru.tasktracker.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final Path filePath;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileBackedTaskManager(String filePath) {
        super(new InMemoryHistoryManager());
        this.filePath = Paths.get(filePath);
    }

    @Override
    public SubTask addSubTask(SubTask subtask) {
        SubTask result = super.addSubTask(subtask);
        save();
        return result;
    }

    @Override
    public Epic addEpic(Epic epic) {
        Epic result = super.addEpic(epic);
        save();
        return result;
    }

    @Override
    public Task addTask(Task task) {
        Task result = super.addTask(task);
        save();
        return result;
    }

    @Override
    public void updateSubTask(SubTask updatedSubTask) {
        super.updateSubTask(updatedSubTask);
        save();
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpic(Epic updatedEpic) {
        super.updateEpic(updatedEpic);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int epicId) {
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void deleteSubTaskById(int subtaskId) {
        super.deleteSubTaskById(subtaskId);
        save();
    }

    private String toCsvString(Task task) {
        String durationStr = "";
        if (task.getDuration() != null) {
            durationStr = String.valueOf(task.getDuration().toMinutes());
        }

        String startTimeStr = "";
        if (task.getStartTime() != null) {
            startTimeStr = task.getStartTime().format(DATE_TIME_FORMATTER);
        }

        String endTimeStr = "";

        switch (task.getType()) {
            case SUBTASK:
                SubTask subTask = (SubTask) task;
                return String.format("%d,%s,%s,%s,%s,%d,%s,%s,%s",
                        subTask.getId(),
                        TypeTask.SUBTASK,
                        subTask.getTaskName(),
                        subTask.getTaskDescription(),
                        subTask.getStatusTask(),
                        subTask.getEpicId(),
                        startTimeStr,
                        durationStr,
                        "");
            case EPIC:
                Epic epic = (Epic) task;
                if (epic.getEndTime() != null) {
                    endTimeStr = epic.getEndTime().format(DATE_TIME_FORMATTER);
                }
                return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s",
                        epic.getId(),
                        TypeTask.EPIC,
                        epic.getTaskName(),
                        epic.getTaskDescription(),
                        epic.getStatusTask(),
                        "",
                        startTimeStr,
                        durationStr,
                        endTimeStr);
            case TASK:
                return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s",
                        task.getId(),
                        TypeTask.TASK,
                        task.getTaskName(),
                        task.getTaskDescription(),
                        task.getStatusTask(),
                        "",
                        startTimeStr,
                        durationStr,
                        "");
            default:
                return null;
        }
    }

    private void save() {
        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write("id,type,name,description,status,epic,startTime,duration,endTime\n");

                Stream.of(tasks.values(), epics.values(), subtasks.values())
                        .flatMap(Collection::stream)
                        .map(this::toCsvString)
                        .forEach(line -> {
                            try {
                                writer.write(line);
                                writer.newLine();
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        });

            } catch (IOException e) {
                throw new ManagerSaveException("Ошибка сохранения в файл: " + filePath, e);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл: " + filePath, e);
        }
    }

    public static Task fromCsv(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TypeTask type = TypeTask.valueOf(fields[1]);
        String name = fields[2];
        String description = fields[3];
        StatusTask status = StatusTask.valueOf(fields[4]);
        LocalDateTime startTime = null;
        Duration duration = Duration.ZERO;
        LocalDateTime endTime = null;

        if (type != TypeTask.EPIC) {
            if (fields.length > 6 && !fields[6].isEmpty()) {
                startTime = LocalDateTime.parse(fields[6], DATE_TIME_FORMATTER);
            }

            if (fields.length > 7 && !fields[7].isEmpty()) {
                long minutes = Long.parseLong(fields[7]);
                duration = Duration.ofMinutes(minutes);
            }
        }

        switch (type) {
            case TASK:
                return new Task(id, name, description, status, startTime, duration);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new SubTask(id, name, description, status, epicId, startTime, duration);
            default:
                return null;
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Task> tasks = reader.lines()
                    .skip(1)
                    .map(FileBackedTaskManager::fromCsv)
                    .filter(Objects::nonNull)
                    .toList();

            maxId = tasks.stream()
                    .mapToInt(Task::getId)
                    .max()
                    .orElse(0);

            tasks.forEach(task -> {
                if (task.getType() == TypeTask.SUBTASK) {
                    manager.subtasks.put(task.getId(), (SubTask) task);
                    manager.prioritizedTasks.add(task);
                } else if (task.getType() == TypeTask.EPIC) {
                    manager.epics.put(task.getId(), (Epic) task);
                } else {
                    manager.tasks.put(task.getId(), task);
                    manager.prioritizedTasks.add(task);
                }
            });

            boolean hasOverlaps = manager.prioritizedTasks.stream()
                    .filter(task -> task.getStartTime() != null)
                    .anyMatch(manager::isTaskOverlap);

            if (hasOverlaps) {
                throw new ManagerSaveException("Обнаружены пересечения по времени в загружаемых задачах");
            }

            Map<Integer, List<Integer>> epicSubTasks = manager.subtasks.values().stream()
                    .collect(Collectors.groupingBy(
                            SubTask::getEpicId,
                            Collectors.mapping(SubTask::getId, Collectors.toList())
                    ));

            epicSubTasks.forEach((epicId, subTaskIds) -> {
                Epic epic = manager.epics.get(epicId);
                if (epic != null) {
                    Epic updatedEpic = new Epic(epic.getId(), epic.getTaskName(),
                            epic.getTaskDescription(), epic.getStatusTask());
                    updatedEpic.getSubTaskIds().addAll(subTaskIds);
                    manager.epics.put(updatedEpic.getId(), updatedEpic);
                }
            });

            manager.epics.keySet().forEach(manager::updateEpicCalculatedFields);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки", e);
        }

        manager.lastid = maxId;
        return manager;
    }


    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }

        public ManagerSaveException(String message) {
            super(message);
        }
    }
}
