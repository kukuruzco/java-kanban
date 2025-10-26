import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

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
                epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));

        int subtaskId = subtask.getId();

        // Подзадача ссылается на правильный эпик
        assertEquals(epicId, subtask.getEpicId());
        assertEquals(LocalDateTime.of(2024, 1, 15, 10, 0), subtask.getStartTime());
        assertEquals(Duration.ofMinutes(30), subtask.getDuration());

        // Эпик должен содержать id подзадачи в своем списке
        Epic updatedEpic = manager.getEpicById(epicId);
        assertTrue(updatedEpic.getSubTaskIds().contains(subtaskId));

        // Количество подзадач у эпика должно увеличиться
        assertEquals(1, updatedEpic.getSubTaskIds().size());
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
                epicId,
                LocalDateTime.of(2024, 1, 15, 11, 0),
                Duration.ofMinutes(45)
        ));
        int subtaskId = subtask.getId();

        // Проверяем, что подзадача была добавлена
        Epic epicBeforeRemoval = manager.getEpicById(epicId);
        assertTrue(epicBeforeRemoval.getSubTaskIds().contains(subtaskId));
        assertEquals(1, epicBeforeRemoval.getSubTaskIds().size());

        // Удаляем подзадачу
        manager.deleteSubTaskById(subtaskId);

        assertThrows(Exception.class, () -> manager.getSubTaskById(subtaskId),
                "Должно выбрасываться исключение при поиске удаленной подзадачи");

        // Проверяем, что эпик больше не содержит id удаленной подзадачи
        Epic epicAfterRemoval = manager.getEpicById(epicId);
        assertFalse(epicAfterRemoval.getSubTaskIds().contains(subtaskId),
                "Эпик не должен содержать id удаленной подзадачи");
        assertEquals(0, epicAfterRemoval.getSubTaskIds().size(),
                "Список подзадач эпика должен быть пустым");

        // Дополнительная проверка: убеждаемся, что подзадача удалена из общего списка
        assertEquals(0, manager.getAllSubTasks().size(),
                "Общий список подзадач должен быть пустым");
    }

    @Test
    public void testSubtaskCannotReferenceNonExistentEpic() {
        TaskManager manager = Managers.getDefault();

        // Пытаемся создать подзадачу с несуществующим эпиком
        assertThrows(Exception.class, () ->
                manager.addSubTask(new SubTask(
                        "Invalid Subtask",
                        "Should fail",
                        999, // несуществующий ID эпика
                        LocalDateTime.of(2024, 1, 15, 12, 0),
                        Duration.ofMinutes(30)
                )), "Нельзя создать подзадачу с несуществующим эпиком");
    }

    @Test
    public void testUpdateSubtask() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        SubTask subtask = manager.addSubTask(new SubTask(
                "Original Subtask",
                "Original Description",
                epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));
        int subtaskId = subtask.getId();

        // Проверяем начальный статус (должен быть NEW)
        assertEquals(StatusTask.NEW, subtask.getStatusTask(), "Новая подзадача должна иметь статус NEW");

        // Обновляем подзадачу
        SubTask updatedSubtask = new SubTask(
                subtaskId,
                "Updated Subtask",
                "Updated Description",
                StatusTask.DONE,
                epicId,
                LocalDateTime.of(2024, 1, 15, 14, 0),
                Duration.ofMinutes(45)
        );
        manager.updateSubTask(updatedSubtask);

        // Проверяем обновленные данные
        SubTask retrievedSubtask = manager.getSubTaskById(subtaskId);
        assertEquals("Updated Subtask", retrievedSubtask.getTaskName());
        assertEquals("Updated Description", retrievedSubtask.getTaskDescription());
        assertEquals(StatusTask.DONE, retrievedSubtask.getStatusTask(), "Статус должен быть обновлен на DONE");
        assertEquals(LocalDateTime.of(2024, 1, 15, 14, 0), retrievedSubtask.getStartTime());
        assertEquals(Duration.ofMinutes(45), retrievedSubtask.getDuration());
    }

    @Test
    public void testEpicContainsCorrectSubtaskIdsAfterMultipleOperations() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        // Добавляем несколько подзадач
        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Subtask 1", "Desc 1", epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30)
        ));
        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Subtask 2", "Desc 2", epicId,
                LocalDateTime.of(2024, 1, 15, 11, 0), Duration.ofMinutes(45)
        ));

        Epic currentEpic = manager.getEpicById(epicId);
        assertEquals(2, currentEpic.getSubTaskIds().size());
        assertTrue(currentEpic.getSubTaskIds().contains(subtask1.getId()));
        assertTrue(currentEpic.getSubTaskIds().contains(subtask2.getId()));

        // Удаляем одну подзадачу
        manager.deleteSubTaskById(subtask1.getId());

        currentEpic = manager.getEpicById(epicId);
        assertEquals(1, currentEpic.getSubTaskIds().size());
        assertFalse(currentEpic.getSubTaskIds().contains(subtask1.getId()));
        assertTrue(currentEpic.getSubTaskIds().contains(subtask2.getId()));

        // Удаляем вторую подзадачу
        manager.deleteSubTaskById(subtask2.getId());

        currentEpic = manager.getEpicById(epicId);
        assertEquals(0, currentEpic.getSubTaskIds().size());
        assertFalse(currentEpic.getSubTaskIds().contains(subtask2.getId()));
    }

    @Test
    public void testSubtaskStatusChangesAffectEpicStatus() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        // Создаем подзадачи
        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Subtask 1", "Desc 1", epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0), Duration.ofMinutes(30)
        ));
        SubTask subtask2 = manager.addSubTask(new SubTask(
                "Subtask 2", "Desc 2", epicId,
                LocalDateTime.of(2024, 1, 15, 11, 0), Duration.ofMinutes(45)
        ));

        // Проверяем начальный статус эпика (все подзадачи NEW -> эпик NEW)
        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask());

        // Меняем статус одной подзадачи на IN_PROGRESS
        SubTask updatedSubtask1 = new SubTask(
                subtask1.getId(),
                "Subtask 1",
                "Desc 1",
                StatusTask.IN_PROGRESS,
                epicId,
                subtask1.getStartTime(),
                subtask1.getDuration()
        );
        manager.updateSubTask(updatedSubtask1);

        // Эпик должен быть IN_PROGRESS
        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask());

        // Меняем обе подзадачи на DONE
        SubTask updatedSubtask2 = new SubTask(
                subtask2.getId(),
                "Subtask 2",
                "Desc 2",
                StatusTask.DONE,
                epicId,
                subtask2.getStartTime(),
                subtask2.getDuration()
        );
        manager.updateSubTask(updatedSubtask1); // Первая уже IN_PROGRESS
        manager.updateSubTask(updatedSubtask2); // Вторая становится DONE

        // Эпик должен быть IN_PROGRESS (смешанные статусы)
        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask());

        // Меняем обе подзадачи на DONE
        SubTask doneSubtask1 = new SubTask(
                subtask1.getId(),
                "Subtask 1",
                "Desc 1",
                StatusTask.DONE,
                epicId,
                subtask1.getStartTime(),
                subtask1.getDuration()
        );
        manager.updateSubTask(doneSubtask1);

        // Теперь эпик должен быть DONE
        assertEquals(StatusTask.DONE, manager.getEpicById(epicId).getStatusTask());
    }

    @Test
    public void testSubtaskTimeCalculation() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        Duration duration = Duration.ofMinutes(45);

        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask",
                "Test Description",
                epicId,
                startTime,
                duration
        ));

        // Проверяем расчет времени
        assertEquals(startTime, subtask.getStartTime());
        assertEquals(duration, subtask.getDuration());
        assertEquals(startTime.plus(duration), subtask.getEndTime());
    }

    @Test
    public void testSubtaskEqualsAndHashCode() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        SubTask subtask1 = manager.addSubTask(new SubTask(
                "Subtask",
                "Description",
                epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));

        SubTask subtask2 = manager.getSubTaskById(subtask1.getId());

        // Подзадачи с одинаковым ID должны быть равны
        assertEquals(subtask1, subtask2);
        assertEquals(subtask1.hashCode(), subtask2.hashCode());

        // Проверяем неравенство с null и другим классом
        assertNotEquals(null, subtask1);
        assertNotEquals("string", subtask1);
    }

    @Test
    public void testSubtaskToString() {
        TaskManager manager = Managers.getDefault();

        Epic epic = manager.addEpic(new Epic("Test Epic", "Description"));
        int epicId = epic.getId();

        SubTask subtask = manager.addSubTask(new SubTask(
                "Test Subtask",
                "Test Description",
                epicId,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                Duration.ofMinutes(30)
        ));

        String toString = subtask.toString();

        // Проверяем, что toString содержит важную информацию
        assertTrue(toString.contains("Test Subtask"));
        assertTrue(toString.contains("Test Description"));
        assertTrue(toString.contains(String.valueOf(epicId)));
        assertTrue(toString.contains(StatusTask.NEW.toString()));
    }
}