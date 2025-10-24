package ru.tasktracker.service.http.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import ru.tasktracker.model.SubTask;

import java.lang.reflect.Type;

public class SubTaskAdapter implements JsonSerializer<SubTask> {
    @Override
    public JsonElement serialize(SubTask subTask, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        if (subTask.getId() != null) {
            jsonObject.addProperty("id", subTask.getId());
        }
        jsonObject.addProperty("taskName", subTask.getTaskName());
        jsonObject.addProperty("taskDescription", subTask.getTaskDescription());
        jsonObject.addProperty("statusTask", subTask.getStatusTask().toString());
        jsonObject.addProperty("type", subTask.getType().toString());

        if (subTask.getDuration() != null) {
            jsonObject.addProperty("duration", subTask.getDuration().toMinutes());
        }

        if (subTask.getStartTime() != null) {
            jsonObject.addProperty("startTime", subTask.getStartTime().toString());
        }

        if (subTask.getEndTime() != null) {
            jsonObject.addProperty("endTime", subTask.getEndTime().toString());
        }

        if (subTask.getEpicId() != null) {
            jsonObject.addProperty("epicId", subTask.getEpicId());
        }

        return jsonObject;
    }
}
