package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.exceptions.NotFoundException;
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

            try {
                SubTask subtask = taskManager.getSubTaskById(id);
                sendText(exchange, gson.toJson(subtask));
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
            sendText(exchange, "{\"error\": \"Request body is empty\"}", 400);
            return;
        }

        try {
            JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
            boolean hasIdInRequest = jsonObject.has("id") && !jsonObject.get("id").isJsonNull();

            SubTask subtask = gson.fromJson(body, SubTask.class);

            if (subtask.getTaskName() == null || subtask.getTaskName().trim().isEmpty()) {
                sendText(exchange, "{\"error\": \"Subtask name is required\"}", 400);
                return;
            }

            if (hasIdInRequest) {
                try {
                    taskManager.updateSubTask(subtask);
                    sendText(exchange, "{\"message\": \"Subtask updated\"}", 201);
                } catch (NotFoundException e) {
                    sendNotFound(exchange);
                } catch (Exception e) {
                    sendText(exchange, "{\"error\": \"Update failed: " + e.getMessage() + "\"}", 400);
                }
            } else {
                try {
                    SubTask createdSubtask = taskManager.addSubTask(subtask);
                    sendText(exchange, gson.toJson(createdSubtask), 201);
                } catch (NotFoundException e) {
                    sendNotFound(exchange);
                } catch (RuntimeException e) {
                    if (e.getMessage() != null && e.getMessage().toLowerCase().contains("intersection")) {
                        sendText(exchange, "{\"error\": \"Time intersection detected\"}", 406);
                    } else {
                        sendText(exchange, "{\"error\": \"Failed to create subtask: " + e.getMessage() + "\"}", 400);
                    }
                } catch (Exception addException) {
                    sendText(exchange, "{\"error\": \"Failed to create subtask: " + addException.getMessage() + "\"}", 400);
                }
            }

        } catch (JsonSyntaxException e) {
            sendText(exchange, "{\"error\": \"Invalid JSON syntax: " + e.getMessage() + "\"}", 400);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            try {
                taskManager.deleteSubTaskById(id);
                sendText(exchange, "{\"message\": \"Subtask deleted\"}");
            } catch (Exception e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}