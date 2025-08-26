import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
    }

    @Test
    void addNewEpic() {
        Epic epic = manager.addEpic(new Epic(
                "Test addNewEpic",
                "Test addNewEpic description"
        ));

        int epicId = epic.getId();

        final Task savedEpic = manager.getEpicById(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = manager.getAllEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void deleteEpicById() {
        Epic epic1 = manager.addEpic(new Epic(
                "Test1 addNewEpic",
                "Test addNewTask description"
        ));
        Epic epic2 = manager.addEpic(new Epic(
                "Test2 addNewEpic",
                "Test addNewTask description"
        ));

        assertEquals(2, manager.getAllEpics().size(), "Неверное количество эпиков.");

        int epic1Id = epic1.getId();
        manager.deleteEpicById(epic1Id);
        assertNull(manager.getEpicById(epic1Id), "Такой эпик существует");
        assertEquals(1, manager.getAllEpics().size(), "Неверное количество эпиков.");
    }

    @Test
    void deleteAllEpics() {
        Epic epic1 = manager.addEpic(new Epic(
                "Test1 addNewEpic",
                "Test addNewTask description"
        ));
        Epic epic2 = manager.addEpic(new Epic(
                "Test2 addNewEpic",
                "Test addNewTask description"
        ));

        assertEquals(2, manager.getAllEpics().size(), "Неверное количество эпиков.");

        manager.deleteAllEpics();

        final List<Epic> epicsAfterDel = manager.getAllEpics();

        assertEquals(0, epicsAfterDel.size(), "Неверное количество эпиков.");
    }
    @Test
    public void testEpicCannotAddItselfAsSubtask() {
        Epic epic = manager.addEpic(new Epic(
                "Test Epic",
                "Test Description"
        ));
        int epicId = epic.getId();
        SubTask subtask = manager.addSubTask(new SubTask(
                "Normal subtask",
                "Desc",
                epicId
        ));

        // Сохраняем оригинальный хэш эпика ДО попытки обновления
        int originalEpicHash = epic.hashCode();
        int originalSubtaskListHash = epic.getSubtaskIds().hashCode();

        // Так как вручную при создании подзадачм невозможно передать её Id,
        // вызываем метод обновления подзадачи с указание Id эпика в параметрах
        SubTask badSubTask = new SubTask(
                epicId,
                "Bad subtask",
                "Bad desc",
                StatusTask.NEW,
                epicId
        );
        manager.updateSubTask(badSubTask);

        // ПРОВЕРЯЕМ ХЭШИ ПОСЛЕ ПОПЫТКИ ОБНОВЛЕНИЯ
        Epic epicAfterAttempt = manager.getEpicById(epicId);

        // 1. Проверяем хэш всего эпика
        assertEquals(originalEpicHash, epicAfterAttempt.hashCode(),
                "Хэш эпика не должен измениться");

        // 2. Проверяем хэш списка подзадач
        assertEquals(originalSubtaskListHash, epicAfterAttempt.getSubtaskIds().hashCode(),
                "Хэш списка подзадач не должен измениться");

    }

    @Test
    @DisplayName("Проверка изменения статуса эпика после изменения подзадач")
    void  changeStatusAfterChangeSubTasks() {
        Epic epic = manager.addEpic(new Epic(
                "Test addNewEpic",
                "Test addNewEpic description"
        ));

        int epicId = epic.getId();

        List<Integer> subTasksIdsWoTasks = epic.getSubtaskIds();
        assertEquals(0, subTasksIdsWoTasks.size());

        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Test1 addNewSubtask",
                "Test addNewSubtask description",
                epicId
        ));

        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Test2 addNewSubtask",
                "Test addNewSubtask description",
                epicId
        ));

        List<Integer> subTasksIdsWith2Tasks = epic.getSubtaskIds();
        assertEquals(2, subTasksIdsWoTasks.size());

        // Проверяем статус эпика после добавления двух задач
        assertEquals(epic.getStatusTask(), StatusTask.NEW);

        // Изменяем статус одой из задач на DONE -> Ожидаем изменения статуса эпика на IN_PROGRESS
        int subtask2Id = subtask2.getId();
        manager.updateSubTask(new SubTask(
                subtask2Id,
                "Test2 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicId
        ));

        // Проверяем статус эпика
        assertEquals(epic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Изменяем статус второй задачи на DONE -> Ожидаем изменения статуса эпика на DONE
        int subtask1Id = subtask1.getId();
        manager.updateSubTask(new SubTask(
                subtask1Id,
                "Test1 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicId
        ));

        // Проверяем статус эпика -> DONE
        assertEquals(epic.getStatusTask(), StatusTask.DONE, "Статус эпика не DONE");

        //Проверка изменения статуса эпика в IN_PROGRESS при добавлении новой задачи
        SubTask subtask3 = manager.addSubTask(new SubTask(
                "Test3 addNewSubtask",
                "Test addNewSubtask description",
                epicId
        ));

        // Проверяем, что количество подзадач в эпике теперь равно 3
        assertEquals(3, epic.getSubtaskIds().size(), "Неверное количество подзадач.");
        // Проверяем статус эпика -> IN_PROGRESS
        assertEquals(epic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Проверка изменения статуса эпика после удаления подзадач -> NEW
        manager.deleteAllSubTasks();
        assertEquals(0, epic.getSubtaskIds().size(), "Неверное количество подзадач.");
        assertEquals(epic.getStatusTask(), StatusTask.NEW, "Статус эпика не NEW");
    }
}