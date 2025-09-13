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
        historyManager.addHistory(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }

    @Test
    @DisplayName("HistoryManager создает новую версию задачи при обновлении и удаляет старую")
    void testHistoryManagerPreservesTaskVersions() {
        // Создаем первоначальную версию задачи
        Task originalTask = new Task(1, "Original Task", "Original description", StatusTask.NEW);
        historyManager.addHistory(originalTask);

        // Создаем обновленную версию задачи (тот же id, но другие данные)
        Task updatedTask = new Task(1, "Updated Task", "Updated description", StatusTask.IN_PROGRESS);
        historyManager.addHistory(updatedTask);

        // Получаем историю
        List<Task> history = historyManager.getHistory();

        // Проверяем, что в истории одна версия задачи
        assertEquals(1, history.size(), "В истории должна быть 1 версия задачи");

        // Проверяем, что последняя версия - обновленная
        Task lastVersion = history.getLast();
        assertEquals("Updated Task", lastVersion.getTaskName());
        assertEquals("Updated description", lastVersion.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, lastVersion.getStatusTask());

        // Проверяем, что первая версия перезаписана
        Task firstVersion = history.getFirst();
        assertEquals("Updated Task", firstVersion.getTaskName());
        assertEquals("Updated description", firstVersion.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, firstVersion.getStatusTask());

        // Проверяем, что id одинаковые (это одна и та же задача)
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals(originalTask.getId(), firstVersion.getId());
        assertEquals(originalTask.getId(), lastVersion.getId());

        // Создаем новую задачу
        Task newTask = new Task(2, "New Task", "New description", StatusTask.NEW);
        historyManager.addHistory(newTask);

        // Создаем обновленную версию 1-й задачи (тот же id, но другие данные)
        Task updatedFirstTask = new Task(1, "Updated first Task", "Updated first description", StatusTask.DONE);
        historyManager.addHistory(updatedFirstTask);

        // Получаем историю
        List<Task> updatedHistory = historyManager.getHistory();

        // Проверяем, что в истории осталось 2 задачи
        assertEquals(2, updatedHistory.size(), "В истории должно быть 2 задачи");

        // Проверяем, что теперь на первой позиции в списке задача с id 2
        Task firstPos = updatedHistory.getFirst();
        assertEquals(2, firstPos.getId());
        assertEquals("New Task", firstPos.getTaskName());
        assertEquals("New description", firstPos.getTaskDescription());
        assertEquals(StatusTask.NEW, firstPos.getStatusTask());

        // Проверяем, что последнюю позицию в списке задач теперь занимает обновленная версия с id 1
        Task secondPos = updatedHistory.getLast();
        assertEquals(1, secondPos.getId());
        assertEquals("Updated first Task", secondPos.getTaskName());
        assertEquals("Updated first description", secondPos.getTaskDescription());
        assertEquals(StatusTask.DONE, secondPos.getStatusTask());
    }
}