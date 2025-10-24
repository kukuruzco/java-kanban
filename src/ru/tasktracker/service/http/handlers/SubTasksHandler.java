package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.model.SubTask;
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

        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        boolean hasId = jsonObject.has("id") && !jsonObject.get("id").isJsonNull();

        try {
            if (hasId) {
                SubTask subtask = gson.fromJson(body, SubTask.class);
                SubTask existingSubtask = taskManager.getSubTaskById(subtask.getId());
                if (existingSubtask != null) {
                    taskManager.updateSubTask(subtask);
                    sendText(exchange, "{\"message\": \"SubTask updated\"}", 201);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                SubTask subtask = gson.fromJson(body, SubTask.class);
                SubTask createdSubtask = taskManager.addSubTask(subtask);
                sendText(exchange, gson.toJson(createdSubtask), 201);
            }
        } catch (RuntimeException e) {
            if (e.getMessage().contains("intersection") || e.getMessage().contains("interaction")) {
                sendHasInteractions(exchange);
            } else {
                sendInternalError(exchange);
            }
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
