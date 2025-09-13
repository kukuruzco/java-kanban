import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    @DisplayName("Добавление и поиск задач всех типов по id")
    void addAndFindAllTaskTypesByid() {
        // 1. Добавляем задачу типа Task
        Task task = manager.addTask(new Task(
                "TestTask",
                "TaskDesc"
        ));
        int taskid = task.getId();
        assertTrue(taskid > 0, "Задача должна получить id");

        // 2. Добавляем Epic
        Epic epic = manager.addEpic(new Epic(
                "TestEpic",
                "EpicDesc"
        ));
        int epicId = epic.getId();
        assertTrue(epicId > 0, "Эпик должен получить id");
        assertNotEquals(taskid, epicId, "id должны быть разными");

        // 3. Добавляем SubTask для эпика
        SubTask subTask = manager.addSubTask(new SubTask(
                "TestSubtask",
                "SubtaskDesc",
                epicId
        ));
        int subtaskId = subTask.getId();
        assertTrue(subtaskId > 0, "Подзадача должна получить id");
        assertNotEquals(taskid, subtaskId, "id должны быть разными");
        assertNotEquals(epicId, subtaskId, "id должны быть разными");

        // 4. Проверяем, что все задачи добавились
        assertEquals(1, manager.getAllTasks().size(), "1 обычная задача");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(1, manager.getAllSubTasks().size(), "1 подзадача");

        // 5. Проверяем поиск по id
        Task foundTask = manager.getTaskByid(taskid);
        assertNotNull(foundTask, "Должна найтись обычная задача");
        assertEquals(task, foundTask, "Задачи должны совпадать");
        assertEquals("TestTask", foundTask.getTaskName());

        Epic foundEpic = manager.getEpicByid(epicId);
        assertNotNull(foundEpic, "Должен найтись эпик");
        assertEquals(epic, foundEpic, "Эпики должны совпадать");
        assertEquals("TestEpic", foundEpic.getTaskName());

        SubTask foundSubTask = manager.getSubTaskByid(subtaskId);
        assertNotNull(foundSubTask, "Должна найтись подзадача");
        assertEquals(subTask, foundSubTask, "Подзадачи должны совпадать");
        assertEquals("TestSubtask", foundSubTask.getTaskName());
        assertEquals(epicId, foundSubTask.getepicId(), "Подзадача должна ссылаться на эпик");

        // 6. Проверяем, что эпик знает о своей подзадаче
        assertTrue(foundEpic.getsubtaskIds().contains(subtaskId),
                "Эпик должен содержать id своей подзадачи");
    }

    @Test
    @DisplayName("Поиск несуществующих задач должен возвращать null или бросать исключение")
    void findNonExistentTasks() {
        // Пытаемся найти задачи с несуществующими id
        assertNull(manager.getTaskByid(999), "Несуществующая задача должна возвращать null");
        assertNull(manager.getEpicByid(999), "Несуществующий эпик должен возвращать null");
        assertNull(manager.getSubTaskByid(999), "Несуществующая подзадача должна возвращать null");

    }

    @Test
    @DisplayName("Все задачи должны получать уникальные id")
    void uniqueidsAcrossAllTaskTypes() {
        // Создаем задачи разных типов
        Task task = manager.addTask(new Task("Task", "Desc"));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("SubTask", "Desc", epic.getId()));

        // Проверяем, что все id уникальны
        assertNotEquals(task.getId(), epic.getId(), "Task и Epic должны иметь разные id");
        assertNotEquals(task.getId(), subTask.getId(), "Task и SubTask должны иметь разные id");
        assertNotEquals(epic.getId(), subTask.getId(), "Epic и SubTask должны иметь разные id");

    }

    @Test
    @DisplayName("Удаление задач всех типов по id")
    void deleteAllTaskTypesByid() {
        // Создаем задачи разных типов
        Task task = manager.addTask(new Task("Task", "Desc"));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask1 = manager.addSubTask(new SubTask("SubTask1", "Desc1", epic.getId()));
        SubTask subTask2 = manager.addSubTask(new SubTask("SubTask2", "Desc2", epic.getId()));
        int taskid = task.getId();
        int epicId = epic.getId();
        int subTask1id = subTask1.getId();
        int subTask2id = subTask2.getId();

        // Проверяем, что все задачи добавились
        assertEquals(1, manager.getAllTasks().size(), "1 обычная задача");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(2, manager.getAllSubTasks().size(), "2 подзадачи");

        // Удаляем задачу и одну подзадачу
        manager.deleteTaskByid(taskid);
        manager.deleteSubTaskByid(subTask1id);
        assertEquals(0, manager.getAllTasks().size(), "обычных задач нет");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(1, manager.getAllSubTasks().size(), "1 подзадача");

        // Удаляем эпик. Автоматически должна удалитьс подзадача
        manager.deleteAllEpics();
        assertEquals(0, manager.getAllEpics().size(), "0 эпиков");
        assertEquals(0, manager.getAllSubTasks().size(), "0 подзадач");
    }
}