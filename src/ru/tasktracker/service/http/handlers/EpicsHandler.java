package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.exceptions.NotFoundException;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager, Gson gson) {
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
        if (path.equals("/epics")) {
            List<Epic> epics = taskManager.getAllEpics();
            sendText(exchange, gson.toJson(epics));
        } else if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            try {
                Epic epic = taskManager.getEpicById(id);
                sendText(exchange, gson.toJson(epic));
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String[] pathParts = path.split("/");
            int epicId = Integer.parseInt(pathParts[2]);

            try {
                taskManager.getEpicById(epicId);
                List<SubTask> subtasks = taskManager.getSubtasksByEpic(epicId);
                sendText(exchange, gson.toJson(subtasks));
            } catch (NotFoundException e) {
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

            if (hasIdInRequest) {
                sendText(exchange, "{\"error\": \"ID should not be provided when creating epic\"}", 400);
                return;
            }

            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getTaskName() == null || epic.getTaskName().trim().isEmpty()) {
                sendText(exchange, "{\"error\": \"Epic name is required\"}", 400);
                return;
            }

            Epic createdEpic = taskManager.addEpic(epic);
            sendText(exchange, gson.toJson(createdEpic), 201);

        } catch (JsonSyntaxException e) {
            sendText(exchange, "{\"error\": \"Invalid JSON syntax: " + e.getMessage() + "\"}", 400);
        } catch (Exception e) {
            sendText(exchange, "{\"error\": \"Failed to create epic: " + e.getMessage() + "\"}", 400);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);

            try {
                taskManager.deleteEpicById(id);
                sendText(exchange, "{\"message\": \"Epic deleted\"}");
            } catch (NotFoundException e) {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}