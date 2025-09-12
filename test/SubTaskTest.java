import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    @Test
    public void testAddSubtaskInExistingEpic() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic(
                "Test Epic",
                "Test Description"
        ));

        int epicid = epic.getid();

        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask",
                "Test Subtask Desc",
                epicid
        ));

        int subtaskid = subtask.getid();

        // Подзадача ссылается на правильный эпик
        assertEquals(epicid, subtask.getEpicid());

        // Эпик должен содержать id подзадачи в своем списке
        Epic updatedEpic = manager.getEpicByid(epicid);
        assertTrue(updatedEpic.getSubtaskids().contains(subtaskid));

        // Количество подзадач у эпика должно увеличиться
        assertEquals(1, updatedEpic.getSubtaskids().size());
    }

    @Test
    public void testRemovedSubtaskShouldNotContainOldids() {
        TaskManager manager = Managers.getDefault();

        // Создаем эпик
        Epic epic = manager.addEpic(new Epic(
                "Test Epic for Removal",
                "Test Description for Removal"
        ));
        int epicid = epic.getid();

        // Создаем подзадачу
        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask to Remove",
                "Test Subtask Desc to Remove",
                epicid
        ));
        int subtaskid = subtask.getid();

        // Проверяем, что подзадача была добавлена
        Epic epicBeforeRemoval = manager.getEpicByid(epicid);
        assertTrue(epicBeforeRemoval.getSubtaskids().contains(subtaskid));
        assertEquals(1, epicBeforeRemoval.getSubtaskids().size());

        // Удаляем подзадачу
        manager.deleteSubTaskByid(subtaskid);

        // Проверяем, что подзадача действительно удалена из менеджера
        assertNull(manager.getSubTaskByid(subtaskid), "Подзадача должна быть удалена из менеджера");

        // Проверяем, что эпик больше не содержит id удаленной подзадачи
        Epic epicAfterRemoval = manager.getEpicByid(epicid);
        assertFalse(epicAfterRemoval.getSubtaskids().contains(subtaskid),
                "Эпик не должен содержать id удаленной подзадачи");
        assertEquals(0, epicAfterRemoval.getSubtaskids().size(),
                "Список подзадач эпика должен быть пустым");

        // Пытаемся получить удаленную подзадачу
        SubTask removedSubtask = manager.getSubTaskByid(subtaskid);
        assertNull(removedSubtask, "Удаленная подзадача не должна быть доступна через менеджер");

    }

}