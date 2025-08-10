import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("Помыть посуду", "Вымыть всю посуду на кухне");
        Task task2 = new Task("Сделать ДЗ", "Решить задачи по Java");
        Epic epic1 = new Epic("Задачи на месяц", "Что надо сделать в августе");
        Epic epic2 = new Epic("Построить дом", "Создать план постройки дома");
        SubTask subtask1 = new SubTask("Победить победителя", "Не обязательно прям победить, " +
                "но постараться", 3);
        SubTask subtask2 = new SubTask("Демонтаж стен", "Снести перегородку", 4);
        SubTask subtask3 = new SubTask("Замена дверей", "Монтаж дверных проемов", 4);

        Task savedTask1 = taskManager.addTask(task1);
        Task savedTask2 = taskManager.addTask(task2);
        Epic savedEpic3 = taskManager.addEpic(epic1);
        Epic savedEpic4 = taskManager.addEpic(epic2);
        SubTask savedSubTask5 = taskManager.addSubTask(subtask1);
        SubTask savedSubTask6 = taskManager.addSubTask(subtask2);
        SubTask savedSubTask7 = taskManager.addSubTask(subtask3);

        // Вывод списка задач
        System.out.println("Список задач:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("================================");

        // Вывод списка эпиков
        System.out.println("Список эпиков:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("================================");

        // Вывод списка подзадач
        System.out.println("Список подзадач:");
        for (SubTask subtask : taskManager.getAllSubTasks()) {
            System.out.println(subtask);
        }
        System.out.println("================================");

        // Изменяем статусы задач
        Task updatedTask1 = new Task(savedTask1.getId(), "Помыть посуду",
                "Вымыть всю посуду на кухне", StatusTask.DONE);
        Task updatedTask2 = new Task(savedTask2.getId(), "Сделать ДЗ",
                "Решить задачи по Java", StatusTask.IN_PROGRESS);

        taskManager.updateTask(updatedTask1);
        taskManager.updateTask(updatedTask2);

        System.out.println("Список обновленных задач:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("================================");


        SubTask updatedSubTask5 = new SubTask(savedSubTask5.getId(), "Победить победителя",
                "Не обязательно прям победить, " +
                        "но постараться", StatusTask.DONE, savedSubTask5.getEpicId());
        SubTask updatedSubTask6 = new SubTask(savedSubTask6.getId(), "Демонтаж стен",
                "Снести перегородку", StatusTask.DONE, savedSubTask6.getEpicId());
        SubTask updatedSubTask7 = new SubTask(savedSubTask7.getId(), "Замена дверей",
                "Установка новых дверей", StatusTask.IN_PROGRESS, savedSubTask7.getEpicId());

        taskManager.updateSubTask(updatedSubTask5);
        taskManager.updateSubTask(updatedSubTask6);
        taskManager.updateSubTask(updatedSubTask7);

        // Вывод списка эпиков
        System.out.println("Список обновленных эпиков:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("================================");

        // Вывод списка подзадач
        System.out.println("Список обновленных подзадач:");
        for (SubTask subtask : taskManager.getAllSubTasks()) {
            System.out.println(subtask);
        }
        System.out.println("================================");

        // Удаляем задачу и эпик
        System.out.println();
        System.out.println("Удаляем задачу ID:1 и эпик ID:4");
        taskManager.deleteTaskById(1);
        taskManager.deleteEpicById(4);

        // Вывод списка задач
        System.out.println("Список задач:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("================================");

        // Вывод списка эпиков
        System.out.println("Список эпиков:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }
        System.out.println("================================");

        // Вывод списка подзадач
        System.out.println("Список подзадач:");
        for (SubTask subtask : taskManager.getAllSubTasks()) {
            System.out.println(subtask);
        }
        System.out.println("================================");
    }
}
