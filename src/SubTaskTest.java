import org.junit.jupiter.api.Test;

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

}