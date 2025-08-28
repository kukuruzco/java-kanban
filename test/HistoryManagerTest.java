import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.HistoryManager;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

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
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }

    @Test
    @DisplayName("HistoryManager сохраняет предыдущие версии задач при обновлении")
    void testHistoryManagerPreservesTaskVersions() {
        // Создаем первоначальную версию задачи
        Task originalTask = new Task(1, "Original Task", "Original description", StatusTask.NEW);
        historyManager.addHistoryList(originalTask);

        // Создаем обновленную версию задачи (тот же ID, но другие данные)
        Task updatedTask = new Task(1, "Updated Task", "Updated description", StatusTask.IN_PROGRESS);
        historyManager.addHistoryList(updatedTask);

        // Получаем историю
        List<Task> history = historyManager.getHistory();

        // Проверяем, что в истории две версии задачи
        assertEquals(2, history.size(), "В истории должно быть 2 версии задачи");

        // Проверяем, что последняя версия - обновленная
        Task lastVersion = history.get(history.size() - 1);
        assertEquals("Updated Task", lastVersion.getTaskName());
        assertEquals("Updated description", lastVersion.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, lastVersion.getStatusTask());

        // Проверяем, что первая версия сохранила оригинальные данные
        Task firstVersion = history.get(0);
        assertEquals("Original Task", firstVersion.getTaskName());
        assertEquals("Original description", firstVersion.getTaskDescription());
        assertEquals(StatusTask.NEW, firstVersion.getStatusTask());

        // Проверяем, что ID одинаковые (это одна и та же задача)
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals(originalTask.getId(), firstVersion.getId());
        assertEquals(originalTask.getId(), lastVersion.getId());
    }
}