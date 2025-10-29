import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.exceptions.NotFoundException;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
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

    @Test
    @DisplayName("Удаление задачи по ID")
    void deleteTaskById() {
        Task task = new Task("Задача", "Описание задачи",
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task createdTask = manager.addTask(task);

        // Проверяем, что задача создалась
        assertNotNull(manager.getTaskById(createdTask.getId()));

        // Удаляем задачу
        manager.deleteTaskById(createdTask.getId());

        // Проверяем, что задача удалилась - должно выбрасываться исключение
        assertThrows(NotFoundException.class, () -> manager.getTaskById(createdTask.getId()),
                "После удаления задачи getTaskById должен бросать NotFoundException");
    }

    @Test
    @DisplayName("Удаление эпика с подзадачами")
    void deleteEpicWithSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача", "Описание подзадачи", createdEpic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30));
        SubTask createdSubTask = manager.addSubTask(subTask);

        // Проверяем, что эпик и подзадача создались
        assertNotNull(manager.getEpicById(createdEpic.getId()));
        assertNotNull(manager.getSubTaskById(createdSubTask.getId()));

        // Удаляем эпик
        manager.deleteEpicById(createdEpic.getId());

        // Проверяем, что эпик удалился - должно выбрасываться исключение
        assertThrows(NotFoundException.class, () -> manager.getEpicById(createdEpic.getId()),
                "После удаления эпика getEpicById должен бросать NotFoundException");

        // Проверяем, что подзадачи тоже удалились
        assertThrows(NotFoundException.class, () -> manager.getSubTaskById(createdSubTask.getId()),
                "После удаления эпика все его подзадачи должны быть удалены");
    }

    @Test
    @DisplayName("Добавление подзадачи с несуществующим эпиком")
    void addSubTaskWithInvalidEpic() {
        SubTask subTask = new SubTask("Подзадача", "Описание подзадачи", 999,
                LocalDateTime.now(), Duration.ofMinutes(30));

        assertThrows(NotFoundException.class, () -> manager.addSubTask(subTask),
                "Добавление подзадачи к несуществующему эпику должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Удаление подзадачи")
    void deleteSubTask() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача", "Описание подзадачи", createdEpic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30));
        SubTask createdSubTask = manager.addSubTask(subTask);

        // Проверяем, что подзадача создалась
        assertNotNull(manager.getSubTaskById(createdSubTask.getId()));

        // Удаляем подзадачу
        manager.deleteSubTaskById(createdSubTask.getId());

        // Проверяем, что подзадача удалилась - должно выбрасываться исключение
        assertThrows(NotFoundException.class, () -> manager.getSubTaskById(createdSubTask.getId()),
                "После удаления подзадачи getSubTaskById должен бросать NotFoundException");

        // Проверяем, что подзадача удалилась из эпика
        Epic epicAfterDeletion = manager.getEpicById(createdEpic.getId());
        assertFalse(epicAfterDeletion.getSubTaskIds().contains(createdSubTask.getId()),
                "Подзадача должна быть удалена из списка подзадач эпика");
    }

    @Test
    @DisplayName("Получение задачи по несуществующему ID")
    void getTaskByInvalidId() {
        assertThrows(NotFoundException.class, () -> manager.getTaskById(999),
                "Получение несуществующей задачи должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Получение эпика по несуществующему ID")
    void getEpicByInvalidId() {
        assertThrows(NotFoundException.class, () -> manager.getEpicById(999),
                "Получение несуществующего эпика должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Получение подзадачи по несуществующему ID")
    void getSubTaskByInvalidId() {
        assertThrows(NotFoundException.class, () -> manager.getSubTaskById(999),
                "Получение несуществующей подзадачи должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Удаление несуществующей задачи")
    void deleteNonExistentTask() {
        assertThrows(NotFoundException.class, () -> manager.deleteTaskById(999),
                "Удаление несуществующей задачи должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Удаление несуществующего эпика")
    void deleteNonExistentEpic() {
        assertThrows(NotFoundException.class, () -> manager.deleteEpicById(999),
                "Удаление несуществующего эпика должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Удаление несуществующей подзадачи")
    void deleteNonExistentSubTask() {
        assertThrows(NotFoundException.class, () -> manager.deleteSubTaskById(999),
                "Удаление несуществующей подзадачи должно бросать NotFoundException");
    }

    @Test
    @DisplayName("Создание задачи с null временем")
    void createTaskWithNullTime() {
        Task task = new Task("Задача с null временем", "Описание", null, null);
        Task createdTask = manager.addTask(task);

        assertNotNull(createdTask, "Задача с null временем должна создаваться");
        assertNull(createdTask.getStartTime(), "У задачи startTime должен быть null");
        assertNull(createdTask.getDuration(), "У задачи duration должен быть null");
    }

    @Test
    @DisplayName("Создание подзадачи с null временем")
    void createSubTaskWithNullTime() {
        Epic epic = new Epic("Эпик", "Описание");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subTask = new SubTask("Подзадача с null временем", "Описание", createdEpic.getId(), null, null);
        SubTask createdSubTask = manager.addSubTask(subTask);

        assertNotNull(createdSubTask, "Подзадача с null временем должна создаваться");
        assertNull(createdSubTask.getStartTime(), "У подзадачи startTime должен быть null");
        assertNull(createdSubTask.getDuration(), "У подзадачи duration должен быть null");
    }

    @Test
    @DisplayName("Задачи с null временем не должны пересекаться")
    void tasksWithNullTimeShouldNotOverlap() {
        // Создаем первую задачу с null временем
        Task task1 = new Task("Task 1", "Desc", null, null);
        Task createdTask1 = manager.addTask(task1);

        // Создаем вторую задачу с null временем - не должно быть конфликта
        Task task2 = new Task("Task 2", "Desc", null, null);
        Task createdTask2 = manager.addTask(task2);

        assertEquals(2, manager.getAllTasks().size(),
                "Обе задачи с null временем должны быть добавлены без конфликтов");
    }
}