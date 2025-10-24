import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private TaskManager manager;
    private Epic epic;
    private int epicId;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.addEpic(new Epic("Test Epic", "Test Description"));
        epicId = epic.getId();
    }

    @Test
    void addNewEpic() {
        Epic savedEpic = manager.getEpicById(epicId);
        List<Epic> epics = manager.getAllEpics();

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void deleteEpicById() {
        Epic epic2 = manager.addEpic(new Epic("Test2 Epic", "Description"));
        assertEquals(2, manager.getAllEpics().size());

        manager.deleteEpicById(epicId);

        assertNull(manager.getEpicById(epicId), "Эпик должен быть удален");
        assertEquals(1, manager.getAllEpics().size());
    }

    @Test
    void deleteAllEpics() {
        manager.addEpic(new Epic("Test2 Epic", "Description"));
        assertEquals(2, manager.getAllEpics().size());

        manager.deleteAllEpics();

        assertEquals(0, manager.getAllEpics().size(), "Все эпики должны быть удалены");
    }

    @Test
    @DisplayName("Проверка изменения статуса эпика")
    void changeEpicStatus() {
        // Добавляем подзадачи
        SubTask subtask1 = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subtask2 = manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        // Проверяем начальный статус
        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask(), "Статус должен быть NEW");

        // Меняем одну подзадачу на DONE
        manager.updateSubTask(new SubTask(subtask2.getId(), "Subtask 2", "Description",
                StatusTask.DONE, epicId, subtask2.getStartTime(), subtask2.getDuration()));
        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Статус должен быть IN_PROGRESS");

        // Меняем обе подзадачи на DONE
        manager.updateSubTask(new SubTask(subtask1.getId(), "Subtask 1", "Description",
                StatusTask.DONE, epicId, subtask1.getStartTime(), subtask1.getDuration()));
        assertEquals(StatusTask.DONE, manager.getEpicById(epicId).getStatusTask(),
                "Статус должен быть DONE");

        // Добавляем новую подзадачу
        manager.addSubTask(new SubTask("Subtask 3", "Description", epicId,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(60)));
        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Статус должен быть IN_PROGRESS");

        // Удаляем все подзадачи
        manager.deleteAllSubTasks();
        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask(),
                "Статус должен быть NEW");
    }

    @Test
    @DisplayName("Проверка расчета времени для эпика")
    void testEpicTimeCalculation() {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 15, 11, 0);

        manager.addSubTask(new SubTask("Subtask 1", "Description", epicId, startTime1, Duration.ofMinutes(30)));
        manager.addSubTask(new SubTask("Subtask 2", "Description", epicId, startTime2, Duration.ofMinutes(45)));

        Epic currentEpic = manager.getEpicById(epicId);

        assertEquals(startTime1, currentEpic.getStartTime(), "StartTime должен быть самым ранним");
        assertEquals(startTime2.plusMinutes(45), currentEpic.getEndTime(), "EndTime должен быть самым поздним");
        assertEquals(Duration.ofMinutes(75), currentEpic.getDuration(), "Duration должен быть суммой");
    }

    // ГРАНИЧНЫЕ УСЛОВИЯ ДЛЯ СТАТУСА EPIC

    @Test
    @DisplayName("a. Все подзадачи со статусом NEW")
    void epicStatus_AllSubtasksNew() {
        manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask(),
                "Эпик со всеми подзадачами NEW должен иметь статус NEW");
    }

    @Test
    @DisplayName("b. Все подзадачи со статусом DONE")
    void epicStatus_AllSubtasksDone() {
        SubTask subtask1 = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subtask2 = manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        manager.updateSubTask(new SubTask(subtask1.getId(), "Subtask 1", "Description",
                StatusTask.DONE, epicId, subtask1.getStartTime(), subtask1.getDuration()));
        manager.updateSubTask(new SubTask(subtask2.getId(), "Subtask 2", "Description",
                StatusTask.DONE, epicId, subtask2.getStartTime(), subtask2.getDuration()));

        assertEquals(StatusTask.DONE, manager.getEpicById(epicId).getStatusTask(),
                "Эпик со всеми подзадачами DONE должен иметь статус DONE");
    }

    @Test
    @DisplayName("c. Подзадачи со статусами NEW и DONE")
    void epicStatus_SubtasksNewAndDone() {
        SubTask subtask1 = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subtask2 = manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        manager.updateSubTask(new SubTask(subtask1.getId(), "Subtask 1", "Description",
                StatusTask.DONE, epicId, subtask1.getStartTime(), subtask1.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Эпик с подзадачами NEW и DONE должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("d. Подзадачи со статусом IN_PROGRESS")
    void epicStatus_SubtasksInProgress() {
        SubTask subtask1 = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subtask2 = manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));

        manager.updateSubTask(new SubTask(subtask1.getId(), "Subtask 1", "Description",
                StatusTask.IN_PROGRESS, epicId, subtask1.getStartTime(), subtask1.getDuration()));
        manager.updateSubTask(new SubTask(subtask2.getId(), "Subtask 2", "Description",
                StatusTask.IN_PROGRESS, epicId, subtask2.getStartTime(), subtask2.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Эпик с подзадачами IN_PROGRESS должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("e. Смешанные статусы: NEW, IN_PROGRESS, DONE")
    void epicStatus_MixedStatuses() {
        SubTask subtask1 = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));
        SubTask subtask2 = manager.addSubTask(new SubTask("Subtask 2", "Description", epicId,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(45)));
        SubTask subtask3 = manager.addSubTask(new SubTask("Subtask 3", "Description", epicId,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(60)));

        // subtask1 остается NEW (по умолчанию)
        manager.updateSubTask(new SubTask(subtask2.getId(), "Subtask 2", "Description",
                StatusTask.IN_PROGRESS, epicId, subtask2.getStartTime(), subtask2.getDuration()));
        manager.updateSubTask(new SubTask(subtask3.getId(), "Subtask 3", "Description",
                StatusTask.DONE, epicId, subtask3.getStartTime(), subtask3.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Эпик с подзадачами разных статусов должен иметь статус IN_PROGRESS");
    }

    @Test
    @DisplayName("f. Эпик без подзадач")
    void epicStatus_NoSubtasks() {
        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask(),
                "Эпик без подзадач должен иметь статус NEW");
    }

    @Test
    @DisplayName("g. Изменение статуса одной подзадачи на IN_PROGRESS")
    void epicStatus_SingleSubtaskInProgress() {
        SubTask subtask = manager.addSubTask(new SubTask("Subtask 1", "Description", epicId,
                LocalDateTime.now(), Duration.ofMinutes(30)));

        assertEquals(StatusTask.NEW, manager.getEpicById(epicId).getStatusTask(),
                "Эпик с одной подзадачей NEW должен иметь статус NEW");

        manager.updateSubTask(new SubTask(subtask.getId(), "Subtask 1", "Description",
                StatusTask.IN_PROGRESS, epicId, subtask.getStartTime(), subtask.getDuration()));

        assertEquals(StatusTask.IN_PROGRESS, manager.getEpicById(epicId).getStatusTask(),
                "Эпик с одной подзадачей IN_PROGRESS должен иметь статус IN_PROGRESS");
    }
}