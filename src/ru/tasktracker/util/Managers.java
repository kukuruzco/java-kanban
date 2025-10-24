package ru.tasktracker.util;

import ru.tasktracker.service.managers.HistoryManager;
import ru.tasktracker.service.managers.InMemoryHistoryManager;
import ru.tasktracker.service.managers.InMemoryTaskManager;
import ru.tasktracker.service.managers.TaskManager;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(getDefaultHistory());
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}

