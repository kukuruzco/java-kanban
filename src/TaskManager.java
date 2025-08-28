import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    List<Task> getHistory();

    Task addTask(Task task);

    ArrayList<Task> getAllTasks();

    void deleteAllTasks();

    Task getTaskById(int Id);

    void updateTask(Task updatedTask);

    void deleteTaskById(int Id);

    ArrayList<Epic> getAllEpics();

    Epic addEpic(Epic epic);

    void deleteAllEpics();

    Epic getEpicById(int Id);

    void updateEpic(Epic updatedEpic);

    void deleteEpicById(int epicId);

    SubTask addSubTask(SubTask subtask);

    ArrayList<SubTask> getAllSubTasks();

    ArrayList<SubTask> getSubtasksByEpic(int epicId);

    SubTask getSubTaskById(int id);

    void deleteSubTaskById(int subTaskId);

    void deleteAllSubTasks();

    void updateSubTask(SubTask updatedSubTask);

}
