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

        int epicid = epic.getid();

        final Task savedEpic = manager.getEpicByid(epicid);

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

        int epic1id = epic1.getid();
        manager.deleteEpicByid(epic1id);
        assertNull(manager.getEpicByid(epic1id), "Такой эпик существует");
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
        int epicid = epic.getid();

        // Получаем актуальную версию эпика
        Epic currentEpic = manager.getEpicByid(epicid);
        SubTask subtask = manager.addSubTask(new SubTask(
                "Normal subtask",
                "Desc",
                epicid
        ));

        // Получаем актуальную версию после добавления подзадачи
        currentEpic = manager.getEpicByid(epicid);

        // Сохраняем содержимое списка
        List<Integer> originalSubtaskList = new ArrayList<>(currentEpic.getSubtaskids());
        int originalSubtaskCount = originalSubtaskList.size();

        // Пытаемся обновить подзадачу с некорректными данными
        SubTask badSubTask = new SubTask(
                epicid, // id подзадачи равен id эпика - это ошибка
                "Bad subtask",
                "Bad desc",
                StatusTask.NEW,
                epicid
        );
        manager.updateSubTask(badSubTask);

        // Получаем актуальную версию эпика после попытки обновления
        Epic epicAfterAttempt = manager.getEpicByid(epicid);
        List<Integer> subtaskidsAfter = epicAfterAttempt.getSubtaskids();

        // Проверяем, что список подзадач не изменился
        assertEquals(originalSubtaskCount, subtaskidsAfter.size(),
                "Количество подзадач не должно измениться");
        assertEquals(originalSubtaskList, subtaskidsAfter,
                "Содержимое списка подзадач не должно измениться");
    }

    @Test
    @DisplayName("Проверка изменения статуса эпика после изменения подзадач")
    void changeStatusAfterChangeSubTasks() {
        Epic epic = manager.addEpic(new Epic(
                "Test addNewEpic",
                "Test addNewEpic description"
        ));

        int epicid = epic.getid();

        Epic currentEpic = manager.getEpicByid(epicid);
        List<Integer> subTasksidsWoTasks = currentEpic.getSubtaskids();
        assertEquals(0, subTasksidsWoTasks.size());

        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Test1 addNewSubtask",
                "Test addNewSubtask description",
                epicid
        ));

        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Test2 addNewSubtask",
                "Test addNewSubtask description",
                epicid
        ));

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicByid(epicid);
        List<Integer> subTasksidsWith2Tasks = currentEpic.getSubtaskids();
        assertEquals(2, subTasksidsWith2Tasks.size());

        // Проверяем статус эпика после добавления двух задач
        assertEquals(currentEpic.getStatusTask(), StatusTask.NEW);

        // Изменяем статус одной из задач на DONE
        int subtask2id = subtask2.getid();
        manager.updateSubTask(new SubTask(
                subtask2id,
                "Test2 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicid
        ));

        // Получаем актуальную версию эпика и проверяем статус
        currentEpic = manager.getEpicByid(epicid);
        assertEquals(currentEpic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Изменяем статус второй задачи на DONE
        int subtask1id = subtask1.getid();
        manager.updateSubTask(new SubTask(
                subtask1id,
                "Test1 addNewSubtask",
                "Test addNewSubtask description",
                StatusTask.DONE,
                epicid
        ));

        // Получаем актуальную версию эпика и проверяем статус
        currentEpic = manager.getEpicByid(epicid);
        assertEquals(currentEpic.getStatusTask(), StatusTask.DONE, "Статус эпика не DONE");

        // Добавляем новую задачу
        SubTask subtask3 = manager.addSubTask(new SubTask(
                "Test3 addNewSubtask",
                "Test addNewSubtask description",
                epicid
        ));

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicByid(epicid);
        assertEquals(3, currentEpic.getSubtaskids().size(), "Неверное количество подзадач.");
        assertEquals(currentEpic.getStatusTask(), StatusTask.IN_PROGRESS, "Статус эпика не IN_PROGRESS");

        // Удаляем все подзадачи
        manager.deleteAllSubTasks();

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicByid(epicid);
        assertEquals(0, currentEpic.getSubtaskids().size(), "Неверное количество подзадач.");
        assertEquals(currentEpic.getStatusTask(), StatusTask.NEW, "Статус эпика не NEW");
    }

    @Test
    @DisplayName("Внутри эпиков не должно оставаться неактуальных id подзадач после удаления")
    void testEpicShouldNotContainStaleSubtaskidsAfterDeletion() {
        Epic epic = manager.addEpic(new Epic(
                "Test Epic for Stale ids",
                "Test Description"
        ));
        int epicid = epic.getid();

        // Создаем несколько подзадач
        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Subtask 1",
                "Description 1",
                epicid
        ));
        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Subtask 2",
                "Description 2",
                epicid
        ));
        SubTask subtask3 = manager.addSubTask(new SubTask(
                "Subtask 3",
                "Description 3",
                epicid
        ));

        int subtask1id = subtask1.getid();
        int subtask2id = subtask2.getid();
        int subtask3id = subtask3.getid();

        // Получаем актуальную версию эпика
        Epic currentEpic = manager.getEpicByid(epicid);
        List<Integer> subtaskidsBefore = currentEpic.getSubtaskids();
        assertEquals(3, subtaskidsBefore.size(), "Должно быть 3 подзадачи в эпике");
        assertTrue(subtaskidsBefore.containsAll(List.of(subtask1id, subtask2id, subtask3id)));

        // Удаляем одну подзадачу
        manager.deleteSubTaskByid(subtask2id);

        // Проверяем, что подзадача удалена из менеджера
        assertNull(manager.getSubTaskByid(subtask2id), "Подзадача должна быть удалена из менеджера");

        // Получаем актуальную версию эпика после удаления
        currentEpic = manager.getEpicByid(epicid);
        List<Integer> subtaskidsAfterFirst = currentEpic.getSubtaskids();

        assertFalse(subtaskidsAfterFirst.contains(subtask2id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask2id);
        assertEquals(2, subtaskidsAfterFirst.size(),
                "В эпике должно остаться 2 подзадачи после удаления одной");
        assertTrue(subtaskidsAfterFirst.containsAll(List.of(subtask1id, subtask3id)),
                "Эпик должен содержать только актуальные id подзадач");

        // Удаляем еще одну подзадачу
        manager.deleteSubTaskByid(subtask1id);

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicByid(epicid);
        List<Integer> subtaskidsAfterSecond = currentEpic.getSubtaskids();

        assertFalse(subtaskidsAfterSecond.contains(subtask1id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask1id);
        assertFalse(subtaskidsAfterSecond.contains(subtask2id),
                "Эпик не должен содержать id удаленной подзадачи " + subtask2id);
        assertEquals(1, subtaskidsAfterSecond.size(),
                "В эпике должна остаться 1 подзадача после удаления двух");
        assertTrue(subtaskidsAfterSecond.contains(subtask3id),
                "Эпик должен содержать только актуальные id подзадач");

        // Удаляем последнюю подзадачу
        manager.deleteSubTaskByid(subtask3id);

        // Получаем актуальную версию эпика
        currentEpic = manager.getEpicByid(epicid);
        List<Integer> subtaskidsAfterAll = currentEpic.getSubtaskids();

        assertEquals(0, subtaskidsAfterAll.size(),
                "Эпик должен быть пустым после удаления всех подзадач");
        assertFalse(subtaskidsAfterAll.contains(subtask1id),
                "Эпик не должен содержать старые id подзадач");
        assertFalse(subtaskidsAfterAll.contains(subtask2id),
                "Эпик не должен содержать старые id подзадач");
        assertFalse(subtaskidsAfterAll.contains(subtask3id),
                "Эпик не должен содержать старые id подзадач");
    }
}