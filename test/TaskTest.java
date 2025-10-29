import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void addNewTask() {
        Task task = manager.addTask(new Task(
                "Test addNewTask",
                "Test addNewTask description",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));

        int taskid = task.getId();

        final Task savedTask = manager.getTaskById(taskid);

        System.out.println(savedTask);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), savedTask.getStartTime());
        assertEquals(Duration.ofMinutes(30), savedTask.getDuration());

        final List<Task> tasks = manager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void deleteAllTasks() {
        Task task1 = manager.addTask(new Task(
                "Test1 addNewTask",
                "Test addNewTask description",
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));
        Task task2 = manager.addTask(new Task(
                "Test2 addNewTask",
                "Test addNewTask description",
                LocalDateTime.of(2024, 1, 15, 11, 0),
                Duration.ofMinutes(45)
        ));

        assertEquals(2, manager.getAllTasks().size(), "Неверное количество задач.");

        manager.deleteAllTasks();

        final List<Task> tasksAfterDel = manager.getAllTasks();

        assertEquals(0, tasksAfterDel.size(), "Неверное количество задач.");
    }

    @Test
    @DisplayName("Изменение задачи через сеттер не должно влиять на задачу в менеджере")
    void testTaskModificationThroughSetterShouldNotAffectManager() {
        // Создаем и добавляем задачу
        Task originalTask = new Task("Original", "Description",
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30));
        Task addedTask = manager.addTask(originalTask);
        int taskid = addedTask.getId();

        // Получаем задачу из менеджера
        Task taskFromManager = manager.getTaskById(taskid);
    }
}