import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.HistoryManager;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HistoryManagerTest {
    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    @DisplayName("Добавление задачи в историю")
    void addTaskToHistory() {
        TaskManager taskManager = Managers.getDefault();
        Task task = taskManager.addTask(new Task("TestTask", "TestTask Description",
                LocalDateTime.now(), Duration.ofMinutes(30)));
        historyManager.addHistory(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "После добавления задачи, история не должна быть пустой.");
        assertEquals(1, history.size(), "После добавления задачи, история не должна быть пустой.");
    }

    @Test
    @DisplayName("HistoryManager создает новую версию задачи при обновлении и удаляет старую")
    void testHistoryManagerPreservesTaskVersions() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        Duration duration = Duration.ofMinutes(30);

        // Создаем первоначальную версию задачи
        Task originalTask = new Task(1, "Original Task", "Original description", StatusTask.NEW,
                startTime, duration);
        historyManager.addHistory(originalTask);

        // Создаем обновленную версию задачи (тот же id, но другие данные)
        Task updatedTask = new Task(1, "Updated Task", "Updated description", StatusTask.IN_PROGRESS,
                startTime.plusHours(1), Duration.ofMinutes(45));
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
        assertEquals(startTime.plusHours(1), lastVersion.getStartTime());
        assertEquals(Duration.ofMinutes(45), lastVersion.getDuration());

        // Проверяем, что первая версия перезаписана
        Task firstVersion = history.getFirst();
        assertEquals("Updated Task", firstVersion.getTaskName());
        assertEquals("Updated description", firstVersion.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, firstVersion.getStatusTask());
        assertEquals(startTime.plusHours(1), firstVersion.getStartTime());
        assertEquals(Duration.ofMinutes(45), firstVersion.getDuration());

        // Проверяем, что id одинаковые (это одна и та же задача)
        assertEquals(originalTask.getId(), updatedTask.getId());
        assertEquals(originalTask.getId(), firstVersion.getId());
        assertEquals(originalTask.getId(), lastVersion.getId());

        // Создаем новую задачу
        Task newTask = new Task(2, "New Task", "New description", StatusTask.NEW,
                LocalDateTime.of(2024, 1, 15, 12, 0), Duration.ofMinutes(20));
        historyManager.addHistory(newTask);

        // Создаем обновленную версию 1-й задачи (тот же id, но другие данные)
        Task updatedFirstTask = new Task(1, "Updated first Task", "Updated first description", StatusTask.DONE,
                LocalDateTime.of(2024, 1, 15, 14, 0), Duration.ofMinutes(60));
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
        assertEquals(LocalDateTime.of(2024, 1, 15, 12, 0), firstPos.getStartTime());
        assertEquals(Duration.ofMinutes(20), firstPos.getDuration());

        // Проверяем, что последнюю позицию в списке задач теперь занимает обновленная версия с id 1
        Task secondPos = updatedHistory.getLast();
        assertEquals(1, secondPos.getId());
        assertEquals("Updated first Task", secondPos.getTaskName());
        assertEquals("Updated first description", secondPos.getTaskDescription());
        assertEquals(StatusTask.DONE, secondPos.getStatusTask());
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), secondPos.getStartTime());
        assertEquals(Duration.ofMinutes(60), secondPos.getDuration());
    }
}