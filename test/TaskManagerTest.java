import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createTaskManager();

    @BeforeEach
    void setUp() {
        manager = createTaskManager();
    }

    // ТЕСТЫ ДЛЯ TASK

    @Test
    @DisplayName("Добавление и получение задачи")
    void addAndGetTask() {
        Task task = new Task("Test Task", "Test Description",
                LocalDateTime.now(), Duration.ofMinutes(30));
        Task addedTask = manager.addTask(task);

        assertNotNull(addedTask.getId(), "Задача должна получить ID");
        assertEquals("Test Task", addedTask.getTaskName());
        assertEquals("Test Description", addedTask.getTaskDescription());
        assertEquals(StatusTask.NEW, addedTask.getStatusTask());

        Task retrievedTask = manager.getTaskById(addedTask.getId());
        assertEquals(addedTask, retrievedTask, "Задачи должны совпадать");
    }

    @Test
    @DisplayName("Получение всех задач")
    void getAllTasks() {
        assertEquals(0, manager.getAllTasks().size(), "Список задач должен быть пустым");

        manager.addTask(new Task("Task 1", "Description",
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addTask(new Task("Task 2", "Description",
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        assertEquals(2, manager.getAllTasks().size(), "Должно быть 2 задачи");
    }

    @Test
    @DisplayName("Обновление задачи")
    void updateTask() {
        Task task = manager.addTask(new Task("Original", "Desc",
                LocalDateTime.now(), Duration.ofMinutes(30)));

        Task updatedTask = new Task(task.getId(), "Updated", "New Desc",
                StatusTask.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(45));
        manager.updateTask(updatedTask);

        Task retrievedTask = manager.getTaskById(task.getId());
        assertEquals("Updated", retrievedTask.getTaskName());
        assertEquals("New Desc", retrievedTask.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, retrievedTask.getStatusTask());
    }

    @Test
    @DisplayName("Удаление задачи по ID")
    void deleteTaskById() {
        Task task = manager.addTask(new Task("Test", "Desc",
                LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.deleteTaskById(task.getId());

        assertNull(manager.getTaskById(task.getId()), "Задача должна быть удалена");
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    @DisplayName("Удаление всех задач")
    void deleteAllTasks() {
        manager.addTask(new Task("Task 1", "Desc", LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addTask(new Task("Task 2", "Desc", LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        manager.deleteAllTasks();

        assertEquals(0, manager.getAllTasks().size(), "Все задачи должны быть удалены");
    }

    // ТЕСТЫ ДЛЯ EPIC

    @Test
    @DisplayName("Добавление и получение эпика")
    void addAndGetEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");
        Epic addedEpic = manager.addEpic(epic);

        assertNotNull(addedEpic.getId(), "Эпик должен получить ID");
        assertEquals("Test Epic", addedEpic.getTaskName());
        assertEquals("Test Description", addedEpic.getTaskDescription());
        assertEquals(StatusTask.NEW, addedEpic.getStatusTask());
        assertTrue(addedEpic.getSubTaskIds().isEmpty(), "У нового эпика не должно быть подзадач");

        Epic retrievedEpic = manager.getEpicById(addedEpic.getId());
        assertEquals(addedEpic, retrievedEpic, "Эпики должны совпадать");
    }

    @Test
    @DisplayName("Статус эпика без подзадач")
    void epicStatusNoSubtasks() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        assertEquals(StatusTask.NEW, epic.getStatusTask(),
                "Эпик без подзадач должен иметь статус NEW");
    }

    // ГРАНИЧНЫЕ УСЛОВИЯ ДЛЯ СТАТУСА EPIC

    @Test
    @DisplayName("a. Все подзадачи со статусом NEW")
    void epicStatusAllSubtasksNew() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        assertEquals(StatusTask.NEW, manager.getEpicById(epic.getId()).getStatusTask(),
                "Эпик со всеми подзадачами NEW должен иметь статус NEW");
    }

    @Test
    @DisplayName("b. Все подзадачи со статусом DONE")
    void epicStatusAllSubtasksDone() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask sub2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        // Обновляем подзадачи на DONE
        manager.updateSubTask(new SubTask(sub1.getId(), "Sub 1", "Desc",
                StatusTask.DONE, epic.getId(), sub1.getStartTime(), sub1.getDuration()));
        manager.updateSubTask(new SubTask(sub2.getId(), "Sub 2", "Desc",
                StatusTask.DONE, epic.getId(), sub2.getStartTime(), sub2.getDuration()));

        assertEquals(StatusTask.DONE, manager.getEpicById(epic.getId()).getStatusTask(),
                "Эпик со всеми подзадачами DONE должен иметь статус DONE");
    }

    @Test
    @DisplayName("c. Подзадачи со статусами NEW и DONE")
    void epicStatusSubtasksNewAndDone() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask sub2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        // Обновляем одну подзадачу на DONE
        manager.updateSubTask(new SubTask(sub1.getId(), "Sub 1", "Desc",
                StatusTask.DONE, epic.getId(), sub1.getStartTime(), sub1.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatusTask(),
                "Эпик с подзадачами NEW и DONE должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("d. Подзадачи со статусом IN_PROGRESS")
    void epicStatusSubtasksInProgress() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask sub2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        // Обновляем подзадачи на IN_PROGRESS
        manager.updateSubTask(new SubTask(sub1.getId(), "Sub 1", "Desc",
                StatusTask.IN_PROGRESS, epic.getId(), sub1.getStartTime(), sub1.getDuration()));
        manager.updateSubTask(new SubTask(sub2.getId(), "Sub 2", "Desc",
                StatusTask.IN_PROGRESS, epic.getId(), sub2.getStartTime(), sub2.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatusTask(),
                "Эпик с подзадачами IN_PROGRESS должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("Смешанные статусы подзадач")
    void epicStatusMixedSubtasks() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask sub2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));
        SubTask sub3 = manager.addSubTask(new SubTask("Sub 3", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(60)));

        // Устанавливаем разные статусы
        manager.updateSubTask(new SubTask(sub1.getId(), "Sub 1", "Desc",
                StatusTask.NEW, epic.getId(), sub1.getStartTime(), sub1.getDuration()));
        manager.updateSubTask(new SubTask(sub2.getId(), "Sub 2", "Desc",
                StatusTask.IN_PROGRESS, epic.getId(), sub2.getStartTime(), sub2.getDuration()));
        manager.updateSubTask(new SubTask(sub3.getId(), "Sub 3", "Desc",
                StatusTask.DONE, epic.getId(), sub3.getStartTime(), sub3.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatusTask(),
                "Эпик со смешанными статусами подзадач должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("Расчет времени эпика на основе подзадач")
    void testEpicTimeCalculation() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 15, 11, 0);

        manager.addSubTask(new SubTask("Subtask 1", "Description", epic.getId(),
                startTime1, Duration.ofMinutes(30)));
        manager.addSubTask(new SubTask("Subtask 2", "Description", epic.getId(),
                startTime2, Duration.ofMinutes(45)));

        Epic currentEpic = manager.getEpicById(epic.getId());

        assertEquals(startTime1, currentEpic.getStartTime(), "StartTime должен быть самым ранним");
        assertEquals(startTime2.plusMinutes(45), currentEpic.getEndTime(), "EndTime должен быть самым поздним");
        assertEquals(Duration.ofMinutes(75), currentEpic.getDuration(), "Duration должен быть суммой");
    }

    @Test
    @DisplayName("Удаление эпика с подзадачами")
    void deleteEpicWithSubtasks() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask sub2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        assertEquals(1, manager.getAllEpics().size());
        assertEquals(2, manager.getAllSubTasks().size());

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getEpicById(epic.getId()), "Эпик должен быть удален");
        assertNull(manager.getSubTaskById(sub1.getId()), "Подзадачи должны быть удалены");
        assertNull(manager.getSubTaskById(sub2.getId()), "Подзадачи должны быть удалены");
        assertEquals(0, manager.getAllEpics().size());
        assertEquals(0, manager.getAllSubTasks().size());
    }

    // ТЕСТЫ ДЛЯ SUBTASK

    @Test
    @DisplayName("Добавление подзадачи с существующим эпиком")
    void addSubTaskWithValidEpic() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("Sub", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));

        assertNotNull(subTask.getId(), "Подзадача должна получить ID");
        assertEquals(epic.getId(), subTask.getEpicId(), "Подзадача должна ссылаться на эпик");
        assertTrue(manager.getEpicById(epic.getId()).getSubTaskIds().contains(subTask.getId()),
                "Эпик должен содержать ID подзадачи");
    }

    @Test
    @DisplayName("Добавление подзадачи с несуществующим эпиком")
    void addSubTaskWithInvalidEpic() {
        SubTask subTask = manager.addSubTask(new SubTask("Sub", "Desc", 999,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        assertNull(subTask, "Подзадача с несуществующим эпиком не должна добавляться");
    }

    @Test
    @DisplayName("Получение подзадач по эпику")
    void getSubtasksByEpic() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        List<SubTask> epicSubtasks = manager.getSubtasksByEpic(epic.getId());
        assertEquals(2, epicSubtasks.size(), "Должно быть 2 подзадачи у эпика");
    }

    @Test
    @DisplayName("Обновление подзадачи")
    void updateSubTask() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("Original", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));

        SubTask updatedSubTask = new SubTask(subTask.getId(), "Updated", "New Desc",
                StatusTask.IN_PROGRESS, epic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));
        manager.updateSubTask(updatedSubTask);

        SubTask retrievedSubTask = manager.getSubTaskById(subTask.getId());
        assertEquals("Updated", retrievedSubTask.getTaskName());
        assertEquals("New Desc", retrievedSubTask.getTaskDescription());
        assertEquals(StatusTask.IN_PROGRESS, retrievedSubTask.getStatusTask());
    }

    @Test
    @DisplayName("Удаление подзадачи")
    void deleteSubTask() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("Sub", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));

        assertEquals(1, manager.getAllSubTasks().size());
        assertEquals(1, manager.getEpicById(epic.getId()).getSubTaskIds().size());

        manager.deleteSubTaskById(subTask.getId());

        assertNull(manager.getSubTaskById(subTask.getId()), "Подзадача должна быть удалена");
        assertEquals(0, manager.getAllSubTasks().size());
        assertEquals(0, manager.getEpicById(epic.getId()).getSubTaskIds().size());
    }

    // ТЕСТЫ ДЛЯ ИСТОРИИ

    @Test
    @DisplayName("Добавление в историю просмотров")
    void addToHistory() {
        Task task = manager.addTask(new Task("Task", "Desc",
                LocalDateTime.now(), Duration.ofMinutes(30)));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertTrue(history.contains(task), "История должна содержать задачу");
        assertTrue(history.contains(epic), "История должна содержать эпик");
    }

    @Test
    @DisplayName("Удаление из истории")
    void removeFromHistory() {
        Task task = manager.addTask(new Task("Task", "Desc",
                LocalDateTime.now(), Duration.ofMinutes(30)));

        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size(), "Задача должна быть в истории");

        manager.deleteTaskById(task.getId());
        assertEquals(0, manager.getHistory().size(), "История должна быть пустой после удаления");
    }

    // ТЕСТЫ ДЛЯ ПРИОРИТЕТНЫХ ЗАДАЧ

    @Test
    @DisplayName("Получение приоритетных задач")
    void getPrioritizedTasks() {
        LocalDateTime now = LocalDateTime.now();
        Task task1 = manager.addTask(new Task("Task 1", "Desc", now.plusHours(2), Duration.ofMinutes(30)));
        Task task2 = manager.addTask(new Task("Task 2", "Desc", now.plusHours(1), Duration.ofMinutes(45)));
        Task task3 = manager.addTask(new Task("Task 3", "Desc", now.plusHours(3), Duration.ofMinutes(60)));

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(3, prioritized.size(), "Должно быть 3 приоритетных задачи");
        assertEquals(task2, prioritized.get(0), "Задачи должны быть отсортированы по времени начала");
        assertEquals(task1, prioritized.get(1), "Задачи должны быть отсортированы по времени начала");
        assertEquals(task3, prioritized.get(2), "Задачи должны быть отсортированы по времени начала");
    }
}
