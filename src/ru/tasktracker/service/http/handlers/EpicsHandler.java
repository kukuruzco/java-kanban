package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
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
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                sendText(exchange, gson.toJson(epic));
            } else {
                sendNotFound(exchange);
            }
        } else if (path.matches("/epics/\\d+/subtasks")) {
            String[] pathParts = path.split("/");
            int epicId = Integer.parseInt(pathParts[2]);
            try {
                List<SubTask> epicSubtasks = taskManager.getSubtasksByEpic(epicId);
                sendText(exchange, gson.toJson(epicSubtasks));
            } catch (RuntimeException e) {
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
                Epic epic = gson.fromJson(body, Epic.class);
                Epic existingEpic = taskManager.getEpicById(epic.getId());
                if (existingEpic != null) {
                    taskManager.updateEpic(epic);
                    sendText(exchange, "{\"message\": \"Epic updated\"}", 201);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                Epic epic = gson.fromJson(body, Epic.class);
                Epic createdEpic = taskManager.addEpic(epic);
                sendText(exchange, gson.toJson(createdEpic), 201);
            }
        } catch (RuntimeException e) {
            sendInternalError(exchange);
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            String[] pathParts = path.split("/");
            int id = Integer.parseInt(pathParts[2]);
            Epic epic = taskManager.getEpicById(id);
            if (epic != null) {
                taskManager.deleteEpicById(id);
                sendText(exchange, "{\"message\": \"Epic deleted\"}");
            } else {
                sendNotFound(exchange);
            }
        } else {
            sendNotFound(exchange);
        }
    }
}
