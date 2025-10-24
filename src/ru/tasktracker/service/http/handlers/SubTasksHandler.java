package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class SubTasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubTasksHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/subtasks")) {
            List<SubTask> subtasks = taskManager.getAllSubTasks();
            sendText(exchange, gson.toJson(subtasks));
        } else if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            SubTask subtask = taskManager.getSubTaskById(id);
            if (subtask != null) {
                sendText(exchange, gson.toJson(subtask));
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
            SubTask subtask = gson.fromJson(body, SubTask.class);

            if (subtask == null) {
                sendText(exchange, "{\"error\": \"Failed to parse subtask from JSON\"}", 400);
                return;
            }

            if (subtask.getTaskName() == null || subtask.getTaskName().trim().isEmpty()) {
                sendText(exchange, "{\"error\": \"Subtask name is required\"}", 400);
                return;
            }

            // ПРОВЕРЯЕМ СУЩЕСТВОВАНИЕ ПОДЗАДАЧИ В МЕНЕДЖЕРЕ
            boolean subtaskExists = subtask.getId() != null && taskManager.getSubTaskById(subtask.getId()) != null;

            if (subtaskExists) {
                // ОБНОВЛЕНИЕ существующей подзадачи
                taskManager.updateSubTask(subtask);
                sendText(exchange, "{\"message\": \"Subtask updated\"}", 201);
            } else {
                // СОЗДАНИЕ новой подзадачи
                SubTask createdSubtask = taskManager.addSubTask(subtask);
                sendText(exchange, gson.toJson(createdSubtask), 201);
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
        if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            SubTask subtask = taskManager.getSubTaskById(id);
            if (subtask != null) {
                taskManager.deleteSubTaskById(id);
                sendText(exchange, "{\"message\": \"Subtask deleted\"}");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}