import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    @DisplayName("Добавление и поиск задач всех типов по ID")
    void addAndFindAllTaskTypesById() {
        // 1. Добавляем задачу типа Task
        Task task = manager.addTask(new Task(
                "TestTask",
                "TaskDesc"
        ));
        int taskId = task.getId();
        assertTrue(taskId > 0, "Задача должна получить ID");

        // 2. Добавляем Epic
        Epic epic = manager.addEpic(new Epic(
                "TestEpic",
                "EpicDesc"
        ));
        int epicId = epic.getId();
        assertTrue(epicId > 0, "Эпик должен получить ID");
        assertNotEquals(taskId, epicId, "ID должны быть разными");

        // 3. Добавляем SubTask для эпика
        SubTask subTask = manager.addSubTask(new SubTask(
                "TestSubtask",
                "SubtaskDesc",
                epicId
        ));
        int subTaskId = subTask.getId();
        assertTrue(subTaskId > 0, "Подзадача должна получить ID");
        assertNotEquals(taskId, subTaskId, "ID должны быть разными");
        assertNotEquals(epicId, subTaskId, "ID должны быть разными");

        // 4. Проверяем, что все задачи добавились
        assertEquals(1, manager.getAllTasks().size(), "1 обычная задача");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(1, manager.getAllSubTasks().size(), "1 подзадача");

        // 5. Проверяем поиск по ID
        Task foundTask = manager.getTaskById(taskId);
        assertNotNull(foundTask, "Должна найтись обычная задача");
        assertEquals(task, foundTask, "Задачи должны совпадать");
        assertEquals("TestTask", foundTask.getTaskName());

        Epic foundEpic = manager.getEpicById(epicId);
        assertNotNull(foundEpic, "Должен найтись эпик");
        assertEquals(epic, foundEpic, "Эпики должны совпадать");
        assertEquals("TestEpic", foundEpic.getTaskName());

        SubTask foundSubTask = manager.getSubTaskById(subTaskId);
        assertNotNull(foundSubTask, "Должна найтись подзадача");
        assertEquals(subTask, foundSubTask, "Подзадачи должны совпадать");
        assertEquals("TestSubtask", foundSubTask.getTaskName());
        assertEquals(epicId, foundSubTask.getEpicId(), "Подзадача должна ссылаться на эпик");

        // 6. Проверяем, что эпик знает о своей подзадаче
        assertTrue(foundEpic.getSubtaskIds().contains(subTaskId),
                "Эпик должен содержать ID своей подзадачи");
    }

    @Test
    @DisplayName("Поиск несуществующих задач должен возвращать null или бросать исключение")
    void findNonExistentTasks() {
        // Пытаемся найти задачи с несуществующими ID
        assertNull(manager.getTaskById(999), "Несуществующая задача должна возвращать null");
        assertNull(manager.getEpicById(999), "Несуществующий эпик должен возвращать null");
        assertNull(manager.getSubTaskById(999), "Несуществующая подзадача должна возвращать null");

    }

    @Test
    @DisplayName("Все задачи должны получать уникальные ID")
    void uniqueIdsAcrossAllTaskTypes() {
        // Создаем задачи разных типов
        Task task = manager.addTask(new Task("Task", "Desc"));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("SubTask", "Desc", epic.getId()));

        // Проверяем, что все ID уникальны
        assertNotEquals(task.getId(), epic.getId(), "Task и Epic должны иметь разные ID");
        assertNotEquals(task.getId(), subTask.getId(), "Task и SubTask должны иметь разные ID");
        assertNotEquals(epic.getId(), subTask.getId(), "Epic и SubTask должны иметь разные ID");

    }

    @Test
    @DisplayName("Удаление задач всех типов по ID")
    void deleteAllTaskTypesById() {
        // Создаем задачи разных типов
        Task task = manager.addTask(new Task("Task", "Desc"));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask1 = manager.addSubTask(new SubTask("SubTask1", "Desc1", epic.getId()));
        SubTask subTask2 = manager.addSubTask(new SubTask("SubTask2", "Desc2", epic.getId()));
        int taskId = task.getId();
        int epicId = epic.getId();
        int subTask1Id = subTask1.getId();
        int subTask2Id = subTask2.getId();

        // Проверяем, что все задачи добавились
        assertEquals(1, manager.getAllTasks().size(), "1 обычная задача");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(2, manager.getAllSubTasks().size(), "2 подзадачи");

        // Удаляем задачу и одну подзадачу
        manager.deleteTaskById(taskId);
        manager.deleteSubTaskById(subTask1Id);
        assertEquals(0, manager.getAllTasks().size(), "обычных задач нет");
        assertEquals(1, manager.getAllEpics().size(), "1 эпик");
        assertEquals(1, manager.getAllSubTasks().size(), "1 подзадача");

        // Удаляем эпик. Автоматически должна удалитьс подзадача
        manager.deleteAllEpics();
        assertEquals(0, manager.getAllEpics().size(), "0 эпиков");
        assertEquals(0, manager.getAllSubTasks().size(), "0 подзадач");
    }
}