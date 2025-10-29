import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.HistoryManager;
import ru.tasktracker.service.managers.InMemoryHistoryManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    @DisplayName("a. Пустая история задач")
    void emptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    @DisplayName("b. Дублирование")
    void historyNoDuplicates() {
        Task task = new Task(1, "Task", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));

        historyManager.addHistory(task);
        historyManager.addHistory(task);
        historyManager.addHistory(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }

    @Test
    @DisplayName("c. Удаление из начала истории")
    void removeFromBeginningOfHistory() {
        Task task1 = new Task(1, "Task 1", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task(2, "Task 2", "Desc", null, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        Task task3 = new Task(3, "Task 3", "Desc", null, LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));

        historyManager.addHistory(task1);
        historyManager.addHistory(task2);
        historyManager.addHistory(task3);

        historyManager.remove(1); // Удаляем из начала

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    @DisplayName("c. Удаление из середины истории")
    void removeFromMiddleOfHistory() {
        Task task1 = new Task(1, "Task 1", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task(2, "Task 2", "Desc", null, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        Task task3 = new Task(3, "Task 3", "Desc", null, LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));

        historyManager.addHistory(task1);
        historyManager.addHistory(task2);
        historyManager.addHistory(task3);

        historyManager.remove(2); // Удаляем из середины

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
    }

    @Test
    @DisplayName("c. Удаление из конца истории")
    void removeFromEndOfHistory() {
        Task task1 = new Task(1, "Task 1", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task(2, "Task 2", "Desc", null, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        Task task3 = new Task(3, "Task 3", "Desc", null, LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));

        historyManager.addHistory(task1);
        historyManager.addHistory(task2);
        historyManager.addHistory(task3);

        historyManager.remove(3); // Удаляем из конца

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    @DisplayName("История сохраняет порядок добавления")
    void historyPreservesOrder() {
        Task task1 = new Task(1, "Task 1", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task(2, "Task 2", "Desc", null, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        Task task3 = new Task(3, "Task 3", "Desc", null, LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));

        historyManager.addHistory(task1);
        historyManager.addHistory(task2);
        historyManager.addHistory(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    @DisplayName("Добавление задачи в историю")
    void addToHistory() {
        Task task = new Task(1, "Task", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        historyManager.addHistory(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test
    @DisplayName("Удаление несуществующей задачи из истории")
    void removeNonExistentTask() {
        Task task = new Task(1, "Task", "Desc", null, LocalDateTime.now(), Duration.ofMinutes(30));
        historyManager.addHistory(task);

        // Удаляем несуществующую задачу
        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться при удалении несуществующей задачи");
    }
}