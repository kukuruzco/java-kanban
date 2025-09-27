package ru.tasktracker.service;

import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TypeTask;
import ru.tasktracker.model.StatusTask;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private final Path filePath;

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
        if (task.getType() == TypeTask.SUBTASK) {
            SubTask subTask = (SubTask) task;
            return String.format("%d,%s,%s,%s,%s,%d",
                    subTask.getId(),
                    TypeTask.SUBTASK,
                    subTask.getTaskName(),
                    subTask.getStatusTask(),
                    subTask.getTaskDescription(),
                    subTask.getEpicId());
        } else if (task.getType() == TypeTask.EPIC) {
            Epic epic = (Epic) task;
            return String.format("%d,%s,%s,%s,%s,",
                    epic.getId(),
                    TypeTask.EPIC,
                    epic.getTaskName(),
                    epic.getStatusTask(),
                    epic.getTaskDescription());
        } else {
            return String.format("%d,%s,%s,%s,%s,",
                    task.getId(),
                    TypeTask.TASK,
                    task.getTaskName(),
                    task.getStatusTask(),
                    task.getTaskDescription());
        }
    }

    private void save() {
        try {
            Files.createDirectories(filePath.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                writer.write("id,type,name,status,description,epic\n");

                for (Task task : tasks.values()) {
                    writer.write(toCsvString(task));
                    writer.newLine();
                }

                for (Epic epic : epics.values()) {
                    writer.write(toCsvString(epic));
                    writer.newLine();
                }

                for (SubTask subTask : subtasks.values()) {
                    writer.write(toCsvString(subTask));
                    writer.newLine();
                }
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
        StatusTask status = StatusTask.valueOf(fields[3]);
        String description = fields[4];

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                int epicId = Integer.parseInt(fields[5]);
                return new SubTask(id, name, description, status, epicId);
            default:
                return null;
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                Task task = fromCsv(line);
                if (task != null) {
                    if (task.getId() > maxId) {
                        maxId = task.getId();
                    }

                    if (task.getType() == TypeTask.SUBTASK) {
                        manager.subtasks.put(task.getId(), (SubTask) task);
                    } else if (task.getType() == TypeTask.EPIC) {
                        manager.epics.put(task.getId(), (Epic) task);
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }

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
    }
}
