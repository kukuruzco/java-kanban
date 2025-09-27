import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
                "Test addNewTask description"
        ));

        int taskid = task.getId();

        final Task savedTask = manager.getTaskById(taskid);

        System.out.println(savedTask);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = manager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void deleteAllTasks() {
        Task task1 = manager.addTask(new Task(
                "Test1 addNewTask",
                "Test addNewTask description"
        ));
        Task task2 = manager.addTask(new Task(
                "Test2 addNewTask",
                "Test addNewTask description"
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
        Task originalTask = new Task("Original", "Description");
        Task addedTask = manager.addTask(originalTask);
        int taskid = addedTask.getId();

        // Получаем задачу из менеджера
        Task taskFromManager = manager.getTaskById(taskid);

        // Меняем задачу через сеттер (ОПАСНО!)
//        taskFromManager.setTaskName("Modified Title");
//        taskFromManager.setTaskDescription("Modified Description");
//        taskFromManager.setStatusTask(StatusTask.DONE);
//
//        // Проверяем, что задача в менеджере НЕ изменилась
//        Task taskAfterModification = manager.getTaskByid(taskid);
//        assertEquals("Original", taskAfterModification.getTaskName());
//        assertEquals("Description", taskAfterModification.getTaskDescription());
//        assertEquals(StatusTask.NEW, taskAfterModification.getStatusTask());
    }

}