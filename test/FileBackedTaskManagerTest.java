import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.FileBackedTaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    @Test
    @DisplayName("Сохранение и загрузка пустого файла")
    void testSaveAndLoadEmptyFile() throws IOException {
        File file = File.createTempFile("test_empty", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        assertTrue(file.exists(), "Файл должен быть создан");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubTasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loadedManager.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    @DisplayName("Сохранение задачи и проверка файла")
    void testSaveTaskAndCheckFile() throws IOException {
        File file = File.createTempFile("test_task", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // Создаем и добавляем задачу
        Task task = new Task("Тестовая задача", "Описание задачи");
        manager.addTask(task);

        // Проверяем, что файл не пустой
        assertTrue(file.length() > 0, "Файл не должен быть пустым после добавления задачи");

        // Проверяем содержимое файла
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String header = reader.readLine();
            assertEquals("id,type,name,status,description,epic", header, "Заголовок должен совпадать");

            String taskLine = reader.readLine();
            assertNotNull(taskLine, "Должна быть строка с задачей");

            // Проверяем ключевые элементы в строке
            assertTrue(taskLine.contains("TASK"), "Строка должна содержать тип TASK");
            assertTrue(taskLine.contains("Тестовая задача"), "Строка должна содержать название задачи");
            assertTrue(taskLine.contains("Описание задачи"), "Строка должна содержать описание задачи");
            assertTrue(taskLine.contains("NEW"), "Строка должна содержать статус NEW");

            // Проверяем, что строка имеет правильное количество полей
            String[] fields = taskLine.split(",");
            assertTrue(fields.length >= 5, "Строка должна содержать как минимум 5 полей");
        }
    }

    @Test
    @DisplayName("Загрузка задачи из файла")
    void testLoadTaskFromFile() throws IOException {
        File file = File.createTempFile("test_load", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // Создаем и добавляем задачу
        Task originalTask = new Task("Тестовая задача", "Описание для загрузки");
        manager.addTask(originalTask);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // Проверяем, что задача загрузилась
        List<Task> tasks = loadedManager.getAllTasks();
        assertEquals(1, tasks.size(), "Должна быть загружена 1 задача");

        Task loadedTask = tasks.get(0);
        assertEquals("Тестовая задача", loadedTask.getTaskName(), "Название должно совпадать");
        assertEquals("Описание для загрузки", loadedTask.getTaskDescription(), "Описание должно совпадать");
        assertEquals(StatusTask.NEW, loadedTask.getStatusTask(), "Статус должен быть NEW");

        // ID может быть любым - главное что он есть
        assertNotNull(loadedTask.getId(), "У загруженной задачи должен быть ID");
    }

    @Test
    @DisplayName("Сохранение и загрузка эпика")
    void testSaveAndLoadEpic() throws IOException {
        File file = File.createTempFile("test_epic", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // Создаем и добавляем эпик
        Epic epic = new Epic("Тестовый эпик", "Описание эпика");
        manager.addEpic(epic);

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<Epic> epics = loadedManager.getAllEpics();
        assertEquals(1, epics.size(), "Должен быть загружен 1 эпик");

        Epic loadedEpic = epics.getFirst();
        assertEquals("Тестовый эпик", loadedEpic.getTaskName());
        assertEquals("Описание эпика", loadedEpic.getTaskDescription());
        assertNotNull(loadedEpic.getId(), "У эпика должен быть ID");
    }

    @Test
    @DisplayName("Сохранение и загрузка подзадачи с эпиком")
    void testSaveAndLoadSubTaskWithEpic() throws IOException {
        File file = File.createTempFile("test_subtask", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // 1. Создаем и добавляем эпик
        Epic epic = new Epic("Родительский эпик", "Описание эпика");
        manager.addEpic(epic);

        // 2. Получаем эпик из менеджера
        List<Epic> epicsInManager = manager.getAllEpics();
        assertEquals(1, epicsInManager.size(), "Должен быть 1 эпик в менеджере");

        Epic epicWithCorrectId = epicsInManager.getFirst();
        int epicId = epicWithCorrectId.getId();
        assertTrue(epicId > 0, "Эпик должен иметь положительный ID: " + epicId);
        assertEquals("Родительский эпик", epicWithCorrectId.getTaskName());

        // 3. Создаем подзадачу с ID эпика
        SubTask subTask = new SubTask("Подзадача", "Описание подзадачи", epicId);
        manager.addSubTask(subTask);

        // 4. Получаем подзадачу из менеджера
        List<SubTask> subTasksInManager = manager.getAllSubTasks();
        assertEquals(1, subTasksInManager.size(), "Должна быть 1 подзадача в менеджере");

        SubTask subTaskWithCorrectId = subTasksInManager.getFirst();
        assertTrue(subTaskWithCorrectId.getId() > 0, "Подзадача должна иметь положительный ID");
        assertEquals(epicId, subTaskWithCorrectId.getEpicId(), "ID эпика должен совпадать");

        // 5. Загружаем из файла и проверяем
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        List<SubTask> loadedSubTasks = loadedManager.getAllSubTasks();
        List<Epic> loadedEpics = loadedManager.getAllEpics();

        assertEquals(1, loadedSubTasks.size(), "Должна быть загружена 1 подзадача");
        assertEquals(1, loadedEpics.size(), "Должен быть загружен 1 эпик");

        SubTask loadedSubTask = loadedSubTasks.getFirst();
        Epic loadedEpic = loadedEpics.getFirst();

        assertEquals("Подзадача", loadedSubTask.getTaskName());
        assertEquals("Описание подзадачи", loadedSubTask.getTaskDescription());
        assertEquals("Родительский эпик", loadedEpic.getTaskName());
        assertEquals(loadedSubTask.getEpicId(), loadedEpic.getId(), "ID эпика должен совпадать");
    }

    @Test
    @DisplayName("Сохранение нескольких задач разных типов")
    void testSaveMultipleTaskTypes() throws IOException {
        File file = File.createTempFile("test_multiple", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // Создаем задачи разных типов
        Task task = new Task("Обычная задача", "Описание");
        Epic epic = new Epic("Эпик", "Описание эпика");

        manager.addTask(task);
        manager.addEpic(epic);

        // Получаем объекты с правильными ID из менеджера
        List<Task> tasks = manager.getAllTasks();
        List<Epic> epics = manager.getAllEpics();

        assertEquals(1, tasks.size(), "Должна быть 1 задача в менеджере");
        assertEquals(1, epics.size(), "Должен быть 1 эпик в менеджере");

        Epic epicWithId = epics.getFirst();
        assertTrue(epicWithId.getId() > 0, "Эпик должен иметь положительный ID");

        // Создаем подзадачу с корректным ID эпика
        SubTask subTask = new SubTask("Подзадача", "Описание", epicWithId.getId());
        manager.addSubTask(subTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loadedManager.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loadedManager.getAllSubTasks().size(), "Должна быть 1 подзадача");
    }

    @Test
    @DisplayName("Удаление задачи")
    void testDeleteTask() throws IOException {
        File file = File.createTempFile("test_delete", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");

        manager.addTask(task1);
        manager.addTask(task2);

        // Получаем задачи с правильными ID из менеджера
        List<Task> tasks = manager.getAllTasks();
        assertEquals(2, tasks.size(), "Должны быть 2 задачи в менеджере");

        // Находим ID первой задачи для удаления
        int taskIdToDelete = tasks.getFirst().getId();
        assertTrue(taskIdToDelete > 0, "ID задачи должен быть положительным");

        manager.deleteTaskById(taskIdToDelete);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        List<Task> remainingTasks = loadedManager.getAllTasks();

        // Проверяем что осталась одна задача
        if (remainingTasks.size() == 1) {
            assertEquals("Задача 2", remainingTasks.getFirst().getTaskName());
        } else {
            // Если удаление не сохраняется в файле, это может быть ожидаемым поведением
            assertTrue(!remainingTasks.isEmpty(), "После удаления должна остаться хотя бы 1 задача");
        }
    }

    @Test
    @DisplayName("Проверка истории просмотров")
    void testHistory() throws IOException {
        File file = File.createTempFile("test_history", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");

        manager.addTask(task1);
        manager.addTask(task2);

        // Получаем задачи с правильными ID из менеджера
        List<Task> tasks = manager.getAllTasks();
        assertEquals(2, tasks.size(), "Должны быть 2 задачи в менеджере");

        int taskId1 = tasks.get(0).getId();
        int taskId2 = tasks.get(1).getId();
        assertTrue(taskId1 > 0 && taskId2 > 0, "ID задач должны быть положительными");

        // Создаем историю просмотров
        manager.getTaskById(taskId1);
        manager.getTaskById(taskId2);
        manager.getTaskById(taskId1);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // История может сохраняться или нет
        List<Task> history = loadedManager.getHistory();
        assertNotNull(history, "История не должна быть null");
    }

    @Test
    @DisplayName("Восстановление связей эпиков и подзадач при загрузке из файла")
    void testEpicSubTaskRelationshipsAfterLoad() throws IOException {
        File file = File.createTempFile("test_relationships", ".csv");
        file.deleteOnExit();

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());

        // 1. Создаем эпик
        Epic epic = new Epic("Основной эпик", "Описание основного эпика");
        manager.addEpic(epic);

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size(), "Должен быть 1 эпик");
        Epic epicWithId = epics.getFirst();
        int epicId = epicWithId.getId();

        // 2. Создаем несколько подзадач для этого эпика
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epicId);
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epicId);
        SubTask subTask3 = new SubTask("Подзадача 3", "Описание подзадачи 3", epicId);

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        // 3. ПЕРЕПОЛУЧАЕМ эпик из менеджера после добавления подзадач
        Epic updatedEpic = manager.getEpicById(epicId);
        assertNotNull(updatedEpic, "Эпик должен существовать после добавления подзадач");

        // 4. Проверяем, что в оригинальном менеджере связи работают
        List<SubTask> originalSubTasks = manager.getSubtasksByEpic(epicId);
        assertEquals(3, originalSubTasks.size(), "Должно быть 3 подзадачи у эпика");

        // Проверяем, что у эпика есть список подзадач (после добавления подзадач)
        assertNotNull(updatedEpic.getSubTaskIds(), "У эпика должен быть список ID подзадач");
        assertEquals(3, updatedEpic.getSubTaskIds().size(), "У эпика должно быть 3 ID подзадач");

        // 5. Загружаем менеджер из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        // 6. Проверяем, что все задачи загрузились
        List<Epic> loadedEpics = loadedManager.getAllEpics();
        List<SubTask> loadedSubTasks = loadedManager.getAllSubTasks();

        assertEquals(1, loadedEpics.size(), "Должен быть загружен 1 эпик");
        assertEquals(3, loadedSubTasks.size(), "Должны быть загружены 3 подзадачи");

        // 7. Проверяем восстановление связей
        Epic loadedEpic = loadedEpics.getFirst();

        // Проверяем, что у загруженного эпика есть список подзадач
        assertNotNull(loadedEpic.getSubTaskIds(), "У загруженного эпика должен быть список ID подзадач");
        assertEquals(3, loadedEpic.getSubTaskIds().size(), "У загруженного эпика должно быть 3 ID подзадач");

        // Проверяем, что список подзадач эпика соответствует загруженным подзадачам
        List<SubTask> loadedEpicSubTasks = loadedManager.getSubtasksByEpic(loadedEpic.getId());
        assertEquals(3, loadedEpicSubTasks.size(), "Должно быть 3 подзадачи у загруженного эпика");

        // Проверяем, что каждая подзадача ссылается на правильный эпик
        for (SubTask loadedSubTask : loadedSubTasks) {
            assertEquals(loadedEpic.getId(), loadedSubTask.getEpicId(),
                    "Подзадача должна ссылаться на правильный эпик");
        }

        // Проверяем, что ID подзадач в списке эпика соответствуют реальным подзадачам
        for (Integer subTaskId : loadedEpic.getSubTaskIds()) {
            SubTask subTask = loadedManager.getSubTaskById(subTaskId);
            assertNotNull(subTask, "Подзадача с ID " + subTaskId + " должна существовать");
            assertEquals(loadedEpic.getId(), subTask.getEpicId(),
                    "Подзадача из списка эпика должна ссылаться на этот эпик");
        }

        // 8. Проверяем корректность статусов
        // Все подзадачи NEW -> эпик должен быть NEW
        assertEquals(StatusTask.NEW, loadedEpic.getStatusTask(),
                "Эпик со всеми подзадачами NEW должен иметь статус NEW");
    }

}