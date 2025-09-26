import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import java.util.ArrayList;
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
    void deleteEpicByid() {
        Epic epic1 = manager.addEpic(new Epic(
                "Test1 addNewEpic",
                "Test addNewTask description"
        ));
        Epic epic2 = manager.addEpic(new Epic(
                "Test2 addNewEpic",
                "Test addNewTask description"
        ));

        assertEquals(2, manager.getAllEpics().size(), "Неверное количество эпиков.");

        int epic1id = epic1.getId();
        manager.deleteEpicById(epic1id);
        assertNull(manager.getEpicById(epic1id), "Такой эпик существует");
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
    @DisplayName("Проверка невозможности добавить эпик в качестве подзадачи")
    public void testEpicCannotAddItselfAsSubtask() {
        Epic epic = manager.addEpic(new Epic(
                "Test Epic",
                "Test Description"
        ));
        int epicId = epic.getId();

        // Получаем актуальную версию эпика
        Epic currentEpic = manager.getEpicById(epicId);
        SubTask subtask = manager.addSubTask(new SubTask(
                "Normal subtask",
                "Desc",
                epicId
        ));

        // Получаем актуальную версию после добавления подзадачи
        currentEpic = manager.getEpicById(epicId);

        // Сохраняем содержимое списка
        List<Integer> originalSubtaskList = new ArrayList<>(currentEpic.getsubtaskIds());
        int originalSubtaskCount = originalSubtaskList.size();

        // Пытаемся обновить подзадачу с некорректными данными
        SubTask badSubTask = new SubTask(
                epicId, // id подзадачи равен id эпика - это ошибка
                "Bad subtask",
                "Bad desc",
                StatusTask.NEW,
                epicId
        );
        manager.updateSubTask(badSubTask);

        // Получаем актуальную версию эпика после попытки обновления
        Epic epicAfterAttempt = manager.getEpicById(epicId);
        List<Integer> subtaskIdsAfter = epicAfterAttempt.getsubtaskIds();

        // Проверяем, что список подзадач не изменился
        assertEquals(originalSubtaskCount, subtaskIdsAfter.size(),
                "Количество подзадач не должно измениться");
        assertEquals(originalSubtaskList, subtaskIdsAfter,
                "Содержимое списка подзадач не должно измениться");
    }

    @Test
    @DisplayName("Проверка изменения статуса эпика после изменения подзадач")
    void changeStatusAfterChangeSubTasks() {
        Epic epic = manager.addEpic(new Epic(
                "Test addNewEpic",
                "Test addNewEpic description"
        ));

        int epicId = epic.getId();

        Epic currentEpic = manager.getEpicById(epicId);
        List<Integer> subTasksidsWoTasks = currentEpic.getsubtaskIds();
        assertEquals(0, subTasksidsWoTasks.size());

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

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicById(epicId);
        List<Integer> subTasksidsWith2Tasks = currentEpic.getsubtaskIds();
        assertEquals(2, subTasksidsWith2Tasks.size());

        // Проверяем статус эпика после добавления двух задач
        assertEquals(currentEpic.getStatusTask(), StatusTask.NEW);

        // Изменяем статус одной из задач на DONE
        int subtask2id = subtask2.getId();
        manager.updateSubTask(new SubTask(
                subtask2id,
                "Test2 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicId
        ));

        // Получаем актуальную версию эпика и проверяем статус
        currentEpic = manager.getEpicById(epicId);
        assertEquals(currentEpic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Изменяем статус второй задачи на DONE
        int subtask1id = subtask1.getId();
        manager.updateSubTask(new SubTask(
                subtask1id,
                "Test1 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicId
        ));

        // Получаем актуальную версию эпика и проверяем статус
        currentEpic = manager.getEpicById(epicId);
        assertEquals(currentEpic.getStatusTask(), StatusTask.DONE, "Статус эпика не DONE");

        // Добавляем новую задачу
        SubTask subtask3 = manager.addSubTask(new SubTask(
                "Test3 addNewSubtask",
                "Test addNewSubtask description",
                epicId
        ));

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicById(epicId);
        assertEquals(3, currentEpic.getsubtaskIds().size(), "Неверное количество подзадач.");
        assertEquals(currentEpic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Удаляем все подзадачи
        manager.deleteAllSubTasks();

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicById(epicId);
        assertEquals(0, currentEpic.getsubtaskIds().size(), "Неверное количество подзадач.");
        assertEquals(currentEpic.getStatusTask(), StatusTask.NEW, "Статус эпика не NEW");
    }

    @Test
    @DisplayName("Внутри эпиков не должно оставаться неактуальных id подзадач после удаления")
    void testEpicShouldNotContainStalesubtaskIdsAfterDeletion() {
        Epic epic = manager.addEpic(new Epic(
                "Test Epic for Stale ids",
                "Test Description"
        ));
        int epicId = epic.getId();

        // Создаем несколько подзадач
        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Subtask 1",
                "Description 1",
                epicId
        ));
        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Subtask 2",
                "Description 2",
                epicId
        ));
        SubTask subtask3 = manager.addSubTask(new SubTask(
                "Subtask 3",
                "Description 3",
                epicId
        ));

        int subtask1id = subtask1.getId();
        int subtask2id = subtask2.getId();
        int subtask3id = subtask3.getId();

        // Получаем актуальную версию эпика
        Epic currentEpic = manager.getEpicById(epicId);
        List<Integer> subtaskIdsBefore = currentEpic.getsubtaskIds();
        assertEquals(3, subtaskIdsBefore.size(), "Должно быть 3 подзадачи в эпике");
        assertTrue(subtaskIdsBefore.containsAll(List.of(subtask1id, subtask2id, subtask3id)));

        // Удаляем одну подзадачу
        manager.deleteSubTaskById(subtask2id);

        // Проверяем, что подзадача удалена из менеджера
        assertNull(manager.getSubTaskById(subtask2id), "Подзадача должна быть удалена из менеджера");

        // Получаем актуальную версию эпика после удаления
        currentEpic = manager.getEpicById(epicId);
        List<Integer> subtaskIdsAfterFirst = currentEpic.getsubtaskIds();

        assertFalse(subtaskIdsAfterFirst.contains(subtask2id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask2id);
        assertEquals(2, subtaskIdsAfterFirst.size(),
                "В эпике должно остаться 2 подзадачи после удаления одной");
        assertTrue(subtaskIdsAfterFirst.containsAll(List.of(subtask1id, subtask3id)),
                "Эпик должен содержать только актуальные id подзадач");

        // Удаляем еще одну подзадачу
        manager.deleteSubTaskById(subtask1id);

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicById(epicId);
        List<Integer> subtaskIdsAfterSecond = currentEpic.getsubtaskIds();

        assertFalse(subtaskIdsAfterSecond.contains(subtask1id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask1id);
        assertFalse(subtaskIdsAfterSecond.contains(subtask2id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask2id);
        assertEquals(1, subtaskIdsAfterSecond.size(),
                "В эпике должна остаться 1 подзадача после удаления двух");
        assertTrue(subtaskIdsAfterSecond.contains(subtask3id),
                "Эпик должен содержать только актуальные id подзадач");

        // Удаляем последнюю подзадачу
        manager.deleteSubTaskById(subtask3id);

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicById(epicId);
        List<Integer> subtaskIdsAfterAll = currentEpic.getsubtaskIds();

        assertEquals(0, subtaskIdsAfterAll.size(),
                "Эпик должен быть пустым после удаления всех подзадач");
        assertFalse(subtaskIdsAfterAll.contains(subtask1id),
                "Эпик не должен содержать старые id подзадач");
        assertFalse(subtaskIdsAfterAll.contains(subtask2id),
                "Эпик не должен содержать старые id подзадач");
        assertFalse(subtaskIdsAfterAll.contains(subtask3id),
                "Эпик не должен содержать старые id подзадач");
    }
}