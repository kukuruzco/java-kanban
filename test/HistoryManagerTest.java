import jdk.internal.icu.text.UnicodeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    @DisplayName("Добавление задачи в историю")
    void addTaskToHistory() {
        TaskManager taskManager = Managers.getDefault();
        Task task = taskManager.addTask(new Task("TestTask", "TestTask Description"));
        historyManager.addHistoryList(task);
        final List<Task> history = historyManager.getHistoryList();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }
}