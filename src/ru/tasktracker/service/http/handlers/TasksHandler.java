package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendText(exchange, "{\"error\": \"Method Not Allowed\"}", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/tasks")) {
            List<Task> tasks = taskManager.getAllTasks();
            sendText(exchange, gson.toJson(tasks));
        } else if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTaskById(id);
            if (task != null) {
                sendText(exchange, gson.toJson(task));
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);

        if (body == null || body.trim().isEmpty()) {
            sendText(exchange, "{\"error\": \"Request body is empty\"}", 400);
            return;
        }

        try {
            Task task = gson.fromJson(body, Task.class);

            if (task == null) {
                sendText(exchange, "{\"error\": \"Failed to parse task from JSON\"}", 400);
                return;
            }

            if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
                sendText(exchange, "{\"error\": \"Task name is required\"}", 400);
                return;
            }

            // Проверяем существует ли задача с таким ID
            boolean taskExists = task.getId() != null && taskManager.getTaskById(task.getId()) != null;

            if (taskExists) {
                // Обновление существующей задачи
                taskManager.updateTask(task);
                sendText(exchange, "{\"message\": \"Task updated\"}", 200);
            } else {
                // Создание новой задачи
                Task createdTask = taskManager.addTask(task);
                sendText(exchange, gson.toJson(createdTask), 201);
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "{\"error\": \"Invalid JSON syntax: " + e.getMessage() + "\"}", 400);
        } catch (RuntimeException e) {
            if (e.getMessage() != null &&
                    (e.getMessage().toLowerCase().contains("intersection") ||
                            e.getMessage().toLowerCase().contains("interaction"))) {
                sendText(exchange, "{\"error\": \"Time intersection detected\"}", 409);
            } else {
                sendInternalError(exchange);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            Task task = taskManager.getTaskById(id);
            if (task != null) {
                taskManager.deleteTaskById(id);
                sendText(exchange, "{\"message\": \"Task deleted\"}");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}