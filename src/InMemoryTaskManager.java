import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    protected int lastId = 0;
    private final HistoryManager historyManager;


    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
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

    @Override
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

    @Override
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

        Epic epic = epics.get(epicId);
        epic.getSubtaskIds().add(newId);

        updateEpicStatus(newSubTask.getEpicId());
        return newSubTask;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
        }

        for (int epicId : epics.keySet()) {
            updateEpicStatus(epicId);
        }
    }

    @Override
    public Task getTaskById(int Id) {
        historyManager.addHistoryList(tasks.get(Id));
        return tasks.get(Id);
    }

    @Override
    public Epic getEpicById(int Id) {
        historyManager.addHistoryList(epics.get(Id));
        return epics.get(Id);
    }

    @Override
    public SubTask getSubTaskById(int Id) {
        historyManager.addHistoryList(subtasks.get(Id));
        return subtasks.get(Id);
    }

    @Override
    public void deleteTaskById(int Id) {
        tasks.remove(Id);
    }

    @Override
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

    @Override
    public void deleteSubTaskById(int subTaskId) {
        SubTask subTask = subtasks.get(subTaskId);
        if (subTask == null) {
            System.out.println("Подзадача с ID " + subTaskId + " не найдена");
        }

        int epicId = subTask.getEpicId();
        subtasks.remove(subTaskId);
        updateEpicStatus(epicId);
    }

    public void updateTask(Task updatedTask) {
        tasks.put(updatedTask.getId(), updatedTask);
    }

    public void updateEpic(Epic updatedEpic) {
        int epicId = updatedEpic.getId();
        Epic existingEpic = epics.get(epicId);
        if (existingEpic == null) {
            System.out.println("Эпик не найден");
        }

        existingEpic.setTaskName(updatedEpic.getTaskName());
        existingEpic.setTaskDescription(updatedEpic.getTaskDescription());

        updateEpicStatus(epicId);
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

        epic.setStatusTask(newEpicStatus);
    }
}
