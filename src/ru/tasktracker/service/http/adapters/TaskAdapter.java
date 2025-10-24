package ru.tasktracker.service.http.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import ru.tasktracker.model.Task;

import java.lang.reflect.Type;

public class TaskAdapter implements JsonSerializer<Task> {
    @Override
    public JsonElement serialize(Task task, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        if (task.getId() != null) {
            jsonObject.addProperty("id", task.getId());
        }
        jsonObject.addProperty("taskName", task.getTaskName());
        jsonObject.addProperty("taskDescription", task.getTaskDescription());
        jsonObject.addProperty("statusTask", task.getStatusTask().toString());
        jsonObject.addProperty("type", task.getType().toString());

        if (task.getDuration() != null) {
            jsonObject.addProperty("duration", task.getDuration().toMinutes());
        }

        if (task.getStartTime() != null) {
            jsonObject.addProperty("startTime", task.getStartTime().toString());
        }

        if (task.getEndTime() != null) {
            jsonObject.addProperty("endTime", task.getEndTime().toString());
        }

        return jsonObject;
    }
}