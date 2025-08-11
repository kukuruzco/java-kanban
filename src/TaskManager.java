import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private int lastId = 0;

    public Task addTask(Task task) {
        int newId = ++lastId;
        Task newTask = new Task(
                newId,
                task.getTaskName(),
                task.getTaskDescription(),
                task.getStatusTask()
        );
        tasks.put(newId, newTask);
        return newTask;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int Id) {
        return tasks.get(Id);
    }

    public void updateTask(Task updatedTask) {
        tasks.put(updatedTask.getId(), updatedTask);
    }

    public void deleteTaskById(int Id) {
        tasks.remove(Id);
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public Epic addEpic(Epic epic) {
        int newId = ++lastId;
        Epic newEpic = new Epic(
                newId,
                epic.getTaskName(),
                epic.getTaskDescription(),
                StatusTask.NEW
        );
        epics.put(newId, newEpic);
        return newEpic;
    }

    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public Epic getEpicById(int Id) {
        return epics.get(Id);
    }

    public void updateEpic(Epic updatedEpic) {
        Epic existingEpic = epics.get(updatedEpic.getId());
        if (existingEpic == null) {
            System.out.println("Эпик не найден");
        }

        Epic newEpic = new Epic(
                updatedEpic.getId(),
                updatedEpic.getTaskName(),
                updatedEpic.getTaskDescription(),
                existingEpic.getStatusTask()
        );

        newEpic.setSubtaskIds(new ArrayList<>(existingEpic.getSubtaskIds()));

        epics.put(newEpic.getId(), newEpic);
    }

    public void deleteEpicById(int epicId) {
        ArrayList<Integer> subtaskIdsToRemove = new ArrayList<>();
        for (SubTask subTask : subtasks.values()) {
            if (subTask.getEpicId() == epicId) {
                subtaskIdsToRemove.add(subTask.getId());
            }
        }
        for (Integer id : subtaskIdsToRemove) {
            subtasks.remove(id);
        }
        epics.remove(epicId);
    }

    public SubTask addSubTask(SubTask subtask) {
        int epicId = subtask.getEpicId();
        if (!epics.containsKey(epicId)) {
            System.out.println("Эпик с ID " + epicId + " не существует");
        }

        int newId = ++lastId;
        SubTask newSubTask = new SubTask(
                newId,
                subtask.getTaskName(),
                subtask.getTaskDescription(),
                subtask.getStatusTask(),
                subtask.getEpicId()
        );
        subtasks.put(newId, newSubTask);
        System.out.println(newSubTask.getEpicId());
        updateEpicStatus(newSubTask.getEpicId());
        return newSubTask;
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<SubTask> getSubtasksByEpic(int epicId) {
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epicId) {
                result.add(subtask);
            }
        }
        return result;
    }

    public SubTask getSubTaskById(int id) {
        return subtasks.get(id);
    }

    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = subtasks.get(subTaskId);
        if (subTask == null) {
            System.out.println("Подзадача с ID " + subTaskId + " не найдена");
        }

        int epicId = subTask.getEpicId();
        subtasks.remove(subTaskId);
        updateEpicStatus(epicId);
    }

    public void deleteAllSubTasks() {
        subtasks.clear();
        for (int epicId : epics.keySet()) {
            updateEpicStatus(epicId);
        }
    }

    public void updateSubTask(SubTask updatedSubTask) {
        if (subtasks.containsKey(updatedSubTask.getId())) {
            subtasks.put(updatedSubTask.getId(), updatedSubTask);
            updateEpicStatus(updatedSubTask.getEpicId());
        } else {
            System.out.println("Ошибка: Подзадача не найдена!");
        }
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        ArrayList<SubTask> epicSubtasks = getSubtasksByEpic(epicId);
        StatusTask newEpicStatus = epic.updateStatus(epicSubtasks);

        Epic updatedEpic = new Epic(
                epic.getId(),
                epic.getTaskName(),
                epic.getTaskDescription(),
                newEpicStatus
        );

        epics.put(epicId, updatedEpic);
    }

}
