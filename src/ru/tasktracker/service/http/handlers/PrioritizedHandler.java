package ru.tasktracker.service.http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                sendText(exchange, gson.toJson(prioritizedTasks));
            } else {
                sendText(exchange, "{\"error\": \"Method Not Allowed\"}", 405);
            }
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
