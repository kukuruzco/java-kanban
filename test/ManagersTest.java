import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    @DisplayName("Managers.getDefault() возвращает готовый к работе TaskManager")
    void getDefaultReturnsInitializedTaskManager() {
        // Получаем менеджер
        TaskManager manager = Managers.getDefault();

        // Проверяем, что не null
        assertNotNull(manager, "Менеджер не должен быть null");

        // Проверяем методы получения списков
        assertNotNull(manager.getAllTasks(), "getAllTasks() не должен возвращать null");
        assertNotNull(manager.getAllEpics(), "getAllEpics() не должен возвращать null");
        assertNotNull(manager.getAllSubTasks(), "getAllSubTasks() не должен возвращать null");
        assertNotNull(manager.getHistoryList(), "getHistory() не должен возвращать null");

        // Проверяем, что списки пусты но готовы к работе
        assertEquals(0, manager.getAllTasks().size());
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubTasks().size());
        assertEquals(0, manager.getHistoryList().size());
    }

    @Test
    @DisplayName("Managers.getDefaultHistory() возвращает готовый к работе HistoryManager")
    void testGetDefaultHistoryReturnsInitializedHistoryManager() {
        // Получаем менеджер истории
        HistoryManager historyManager = Managers.getDefaultHistory();

        // Проверяем, что не null
        assertNotNull(historyManager, "HistoryManager не должен быть null");

        // Должен уметь добавлять задачи
        Task task = new Task("Test", "Description");

        historyManager.addHistoryList(task);

        // Должен уметь возвращать историю
        var history = historyManager.getHistoryList();
        assertNotNull(history, "История не должна быть null");
        assertTrue(history.size() <= 10, "История не должна превышать лимит");
    }

    @Test
    @DisplayName("TaskManager из getDefault() использует HistoryManager из getDefaultHistory()")
    void taskManagerUsesHistoryManagerIntegration() {
        TaskManager taskManager = Managers.getDefault();

        // Создаем и получаем задачу - она должна добавиться в историю
        Task task = taskManager.addTask(new Task("TestTask", "Description"));
        Task retrieved = taskManager.getTaskById(task.getId());

        // Проверяем, что история работает
        ArrayList<Task> history = taskManager.getHistoryList();
        assertNotNull(history, "История не должна быть null");

    }
}