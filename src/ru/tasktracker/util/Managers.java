package ru.tasktracker.util;

import ru.tasktracker.service.HistoryManager;
import ru.tasktracker.service.InMemoryHistoryManager;
import ru.tasktracker.service.InMemoryTaskManager;
import ru.tasktracker.service.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

