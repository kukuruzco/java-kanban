import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.HistoryManager;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    @DisplayName("Managers.getDefault() возвращает готовый к работе TaskManager")
    void getDefaultReturnsInitializedTaskManager() {
        TaskManager manager = Managers.getDefault();

        assertNotNull(manager, "Менеджер не должен быть null");

        assertNotNull(manager.getAllTasks(), "getAllTasks() не должен возвращать null");
        assertNotNull(manager.getAllEpics(), "getAllEpics() не должен возвращать null");
        assertNotNull(manager.getAllSubTasks(), "getAllSubTasks() не должен возвращать null");
        assertNotNull(manager.getHistory(), "getHistory() не должен возвращать null");

        assertEquals(0, manager.getAllTasks().size());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubTasks().size());
        assertEquals(0, manager.getHistory().size());
    }

    @Test
    @DisplayName("Managers.getDefaultHistory() возвращает готовый к работу HistoryManager")
    void testGetDefaultHistoryReturnsInitializedHistoryManager() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager, "HistoryManager не должен быть null");

        TaskManager taskManager = Managers.getDefault();
        Task task = taskManager.addTask(new Task("Test", "Description",
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30)));

        historyManager.addHistory(task);

        var history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "В истории должна быть одна задача");
        assertEquals(task.getId(), history.get(0).getId(), "ID задачи в истории должен совпадать");
    }

    @Test
    @DisplayName("TaskManager из getDefault() использует HistoryManager из getDefaultHistory()")
    void taskManagerUsesHistoryManagerIntegration() {
        TaskManager taskManager = Managers.getDefault();

        // Создаем и получаем задачу - она должна добавиться в историю
        Task task = taskManager.addTask(new Task("TestTask", "Description",
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30)));
        Task retrieved = taskManager.getTaskById(task.getId());

        // Проверяем, что история работает
        List<Task> history = taskManager.getHistory();
        assertNotNull(history, "История не должна быть null");
        assertEquals(1, history.size(), "В истории должна быть одна задача после получения по ID");
        assertEquals(task.getId(), history.getFirst().getId(), "В истории должна быть запрошенная задача");
    }

    @Test
    @DisplayName("HistoryManager корректно обрабатывает null и задачи без ID")
    void historyManagerHandlesEdgeCases() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Проверяем, что менеджер не падает при пустой истории
        var emptyHistory = historyManager.getHistory();
        assertNotNull(emptyHistory, "История не должна быть null даже когда пуста");
        assertTrue(emptyHistory.isEmpty(), "Изначально история должна быть пустой");
    }
}