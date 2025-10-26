package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
                    sendText(exchange, "{\"error\": \"Метод не поддерживается\"}", 405);
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

            try {
                Task task = taskManager.getTaskById(id);
                sendText(exchange, gson.toJson(task));
            } catch (Exception e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readText(exchange);

        if (body == null || body.trim().isEmpty()) {
            sendText(exchange, "{\"error\": \"Тело запроса пустое\"}", 400);
            return;
        }

        try {
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            boolean hasIdInRequest = jsonObject.has("id") && !jsonObject.get("id").isJsonNull();

            Task task = gson.fromJson(body, Task.class);

            if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
                sendText(exchange, "{\"error\": \"Имя задачи обязательно\"}", 400);
                return;
            }

            if (hasIdInRequest) {
                try {
                    taskManager.updateTask(task);
                    sendText(exchange, "{\"message\": \"Задача обновлена\"}", 201);
                } catch (Exception e) {
                    sendText(exchange, "{\"error\": \"Ошибка обновления: " + e.getMessage() + "\"}", 400);
                }
            } else {
                try {
                    Task createdTask = taskManager.addTask(task);
                    sendText(exchange, gson.toJson(createdTask), 201);
                } catch (RuntimeException e) {
                    // Проверяем как английское "intersection", так и русское "пересекается"
                    if (e.getMessage() != null &&
                            (e.getMessage().toLowerCase().contains("intersection") ||
                                    e.getMessage().toLowerCase().contains("пересекается"))) {
                        sendText(exchange, "{\"error\": \"Обнаружено пересечение по времени\"}", 406);
                    } else {
                        sendText(exchange, "{\"error\": \"Ошибка создания задачи: " + e.getMessage() + "\"}", 400);
                    }
                } catch (Exception addException) {
                    sendText(exchange, "{\"error\": \"Ошибка создания задачи: " + addException.getMessage() + "\"}", 400);
                }
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "{\"error\": \"Неверный формат JSON: " + e.getMessage() + "\"}", 400);
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
                sendText(exchange, "{\"message\": \"Задача удалена\"}");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}