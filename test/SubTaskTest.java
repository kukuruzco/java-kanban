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

        int epicId = epic.getId();

        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask",
                "Test Subtask Desc",
                epicId
        ));

        int subtaskId = subtask.getId();

        // Подзадача ссылается на правильный эпик
        assertEquals(epicId, subtask.getepicId());

        // Эпик должен содержать id подзадачи в своем списке
        Epic updatedEpic = manager.getEpicByid(epicId);
        assertTrue(updatedEpic.getsubtaskIds().contains(subtaskId));

        // Количество подзадач у эпика должно увеличиться
        assertEquals(1, updatedEpic.getsubtaskIds().size());
    }

    @Test
    public void testRemovedSubtaskShouldNotContainOldids() {
        TaskManager manager = Managers.getDefault();

        // Создаем эпик
        Epic epic = manager.addEpic(new Epic(
                "Test Epic for Removal",
                "Test Description for Removal"
        ));
        int epicId = epic.getId();

        // Создаем подзадачу
        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask to Remove",
                "Test Subtask Desc to Remove",
                epicId
        ));
        int subtaskId = subtask.getId();

        // Проверяем, что подзадача была добавлена
        Epic epicBeforeRemoval = manager.getEpicByid(epicId);
        assertTrue(epicBeforeRemoval.getsubtaskIds().contains(subtaskId));
        assertEquals(1, epicBeforeRemoval.getsubtaskIds().size());

        // Удаляем подзадачу
        manager.deleteSubTaskByid(subtaskId);

        // Проверяем, что подзадача действительно удалена из менеджера
        assertNull(manager.getSubTaskByid(subtaskId), "Подзадача должна быть удалена из менеджера");

        // Проверяем, что эпик больше не содержит id удаленной подзадачи
        Epic epicAfterRemoval = manager.getEpicByid(epicId);
        assertFalse(epicAfterRemoval.getsubtaskIds().contains(subtaskId),
                "Эпик не должен содержать id удаленной подзадачи");
        assertEquals(0, epicAfterRemoval.getsubtaskIds().size(),
                "Список подзадач эпика должен быть пустым");

        // Пытаемся получить удаленную подзадачу
        SubTask removedSubtask = manager.getSubTaskByid(subtaskId);
        assertNull(removedSubtask, "Удаленная подзадача не должна быть доступна через менеджер");

    }

}