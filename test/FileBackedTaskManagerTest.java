import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File testFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            testFile = File.createTempFile("test_tasks", ".csv");
            testFile.deleteOnExit();
            return new FileBackedTaskManager(testFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @Test
    @DisplayName("Сохранение и загрузка пустого файла")
    void saveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubTasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    @DisplayName("Сохранение и загрузка задач разных типов")
    void saveAndLoadDifferentTaskTypes() {
        // Создаем задачи разных типов
        Task task = manager.addTask(new Task("Task", "Desc",
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30)));
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask = manager.addSubTask(new SubTask("SubTask", "Desc", epic.getId(),
                LocalDateTime.of(2024, 1, 15, 11, 0), Duration.ofMinutes(45)));

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверяем, что все задачи загрузились
        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubTasks().size());

        // Проверяем корректность данных
        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertEquals("Task", loadedTask.getTaskName());
        assertEquals("Desc", loadedTask.getTaskDescription());

        SubTask loadedSubTask = loadedManager.getSubTaskById(subTask.getId());
        assertEquals("SubTask", loadedSubTask.getTaskName());
        assertEquals(epic.getId(), loadedSubTask.getEpicId());
    }

    @Test
    @DisplayName("Восстановление связей эпиков и подзадач")
    void restoreEpicSubTaskRelationships() {
        Epic epic = manager.addEpic(new Epic("Epic", "Desc"));
        SubTask subTask1 = manager.addSubTask(new SubTask("Sub 1", "Desc", epic.getId(),
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subTask2 = manager.addSubTask(new SubTask("Sub 2", "Desc", epic.getId(),
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertEquals(2, loadedEpic.getSubTaskIds().size(), "У эпика должно быть 2 подзадачи");
        assertTrue(loadedEpic.getSubTaskIds().contains(subTask1.getId()));
        assertTrue(loadedEpic.getSubTaskIds().contains(subTask2.getId()));

        // Проверяем, что подзадачи ссылаются на правильный эпик
        SubTask loadedSubTask1 = loadedManager.getSubTaskById(subTask1.getId());
        assertEquals(epic.getId(), loadedSubTask1.getEpicId());
    }

    @Test
    @DisplayName("Корректный перехват исключений при работе с файлами")
    void testFileExceptionHandling() {
        // Попытка загрузить из несуществующего файла
        File nonExistentFile = new File("non_existent_file.csv");

        assertThrows(FileBackedTaskManager.ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        }, "Должно быть исключение при загрузке из несуществующего файла");

        // Создание менеджера с некорректным путем
        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = new FileBackedTaskManager("/invalid/path/tasks.csv");
        }, "Создание менеджера с некорректным путем не должно вызывать исключение");
    }
}