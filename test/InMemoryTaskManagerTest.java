import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.InMemoryHistoryManager;
import ru.tasktracker.service.managers.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    @DisplayName("Проверка пересечения задач по времени")
    void testTasksOverlap() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        Task task1 = manager.addTask(new Task("Task 1", "Desc",
                baseTime, Duration.ofHours(1))); // 10:00 - 11:00

        // Пересекающаяся задача
        Task overlappingTask = new Task("Task 2", "Desc",
                baseTime.plusMinutes(30), Duration.ofHours(1)); // 10:30 - 11:30

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(overlappingTask),
                "Должна быть ошибка при добавлении пересекающейся задачи");

        // Непересекающаяся задача
        Task nonOverlappingTask = new Task("Task 3", "Desc",
                baseTime.plusHours(2), Duration.ofHours(1)); // 12:00 - 13:00

        assertDoesNotThrow(() -> manager.addTask(nonOverlappingTask),
                "Не должно быть ошибки при добавлении непересекающейся задачи");
    }

    @Test
    @DisplayName("Проверка пересечения при обновлении задачи")
    void testTasksOverlapOnUpdate() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        Task task1 = manager.addTask(new Task("Task 1", "Desc",
                baseTime, Duration.ofHours(1))); // 10:00 - 11:00
        Task task2 = manager.addTask(new Task("Task 2", "Desc",
                baseTime.plusHours(2), Duration.ofHours(1))); // 12:00 - 13:00

        // Обновляем task2 так, чтобы она пересекалась с task1
        Task updatedTask2 = new Task(task2.getId(), "Task 2 Updated", "Desc",
                task2.getStatusTask(), baseTime.plusMinutes(30), Duration.ofHours(1)); // 10:30 - 11:30

        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(updatedTask2),
                "Должна быть ошибка при обновлении на пересекающееся время");
    }

    @Test
    @DisplayName("Задачи без пересечения добавляются успешно")
    void testNoOverlapTasks() {
        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        Task task1 = manager.addTask(new Task("Task 1", "Desc",
                baseTime, Duration.ofMinutes(30))); // 10:00 - 10:30
        Task task2 = manager.addTask(new Task("Task 2", "Desc",
                baseTime.plusMinutes(45), Duration.ofMinutes(30))); // 10:45 - 11:15

        assertEquals(2, manager.getAllTasks().size(), "Обе задачи должны быть добавлены");
    }

}