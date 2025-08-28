import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void addNewTask() {
        TaskManager manager = Managers.getDefault();
        Task task = manager.addTask(new Task(
                "Test addNewTask",
                "Test addNewTask description"
        ));

        int taskId = task.getId();

        final Task savedTask = manager.getTaskById(taskId);

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
        TaskManager manager = Managers.getDefault();
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

}