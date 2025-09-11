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
        assertEquals(epicId, subtask.getEpicId());

        // Эпик должен содержать ID подзадачи в своем списке
        Epic updatedEpic = manager.getEpicById(epicId);
        assertTrue(updatedEpic.getSubtaskIds().contains(subtaskId));

        // Количество подзадач у эпика должно увеличиться
        assertEquals(1, updatedEpic.getSubtaskIds().size());
    }

    @Test
    public void testRemovedSubtaskShouldNotContainOldIds() {
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
        Epic epicBeforeRemoval = manager.getEpicById(epicId);
        assertTrue(epicBeforeRemoval.getSubtaskIds().contains(subtaskId));
        assertEquals(1, epicBeforeRemoval.getSubtaskIds().size());

        // Удаляем подзадачу
        manager.deleteSubTaskById(subtaskId);

        // Проверяем, что подзадача действительно удалена из менеджера
        assertNull(manager.getSubTaskById(subtaskId), "Подзадача должна быть удалена из менеджера");

        // Проверяем, что эпик больше не содержит ID удаленной подзадачи
        Epic epicAfterRemoval = manager.getEpicById(epicId);
        assertFalse(epicAfterRemoval.getSubtaskIds().contains(subtaskId),
                "Эпик не должен содержать ID удаленной подзадачи");
        assertEquals(0, epicAfterRemoval.getSubtaskIds().size(),
                "Список подзадач эпика должен быть пустым");

        // Пытаемся получить удаленную подзадачу
        SubTask removedSubtask = manager.getSubTaskById(subtaskId);
        assertNull(removedSubtask, "Удаленная подзадача не должна быть доступна через менеджер");

    }

}