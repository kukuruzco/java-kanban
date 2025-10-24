package ru.tasktracker.service.http.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter out, Duration value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.toMinutes());
        }
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        try {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            long minutes = in.nextLong();
            return Duration.ofMinutes(minutes);
        } catch (NumberFormatException | IllegalStateException e) {
            in.nextNull();
            return null;
        }
    }
}
