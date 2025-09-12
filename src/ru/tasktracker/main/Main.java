package ru.tasktracker.main;

import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();
        taskManager.addTask(new Task("Помыть посуду", "Вымыть всю посуду на кухне"));
        taskManager.addTask(new Task("Сделать ДЗ", "Решить задачи по Java"));
        taskManager.addEpic(new Epic("Задачи на месяц", "Что надо сделать в августе"));
        taskManager.addEpic(new Epic("Построить дом", "Создать план постройки дома"));
        taskManager.addSubTask(new SubTask("Победить победителя", "Не обязательно прям победить, " +
                "но постараться", 3));
        taskManager.addSubTask(new SubTask("Демонтаж стен", "Снести перегородку", 4));
        taskManager.addSubTask(new SubTask("Замена дверей", "Монтаж дверных проемов", 4));

        taskManager.getTaskByid(1);
        taskManager.getTaskByid(2);
        taskManager.getSubTaskByid(6);
        taskManager.getTaskByid(2);
        taskManager.getEpicByid(4);

        Epic epic = taskManager.getEpicByid(4);
        System.out.println(epic);
        System.out.println(epic.getSubtaskids());
        taskManager.getSubTaskByid(6);

        printAllTasks(taskManager);

        System.out.println("================================");

        // Изменяем статусы задач
        taskManager.updateTask(new Task(1, "Помыть посуду",
                "Вымыть всю посуду на кухне", StatusTask.DONE));
        taskManager.updateTask(new Task(2, "Сделать ДЗ",
                "Решить задачи по Java", StatusTask.IN_PROGRESS));
        taskManager.updateSubTask(new SubTask(5, "Победить победителя",
                "Не обязательно прям победить, " +
                        "но постараться", StatusTask.DONE, 3));
        taskManager.updateSubTask(new SubTask(6, "Демонтаж стен",
                "Снести перегородку", StatusTask.DONE, 4));
        taskManager.updateSubTask(new SubTask(7, "Замена дверей",
                "Установка новых дверей", StatusTask.IN_PROGRESS, 4));

//        taskManager.getEpicByid(3);

        printAllTasks(taskManager);

    }


    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : taskManager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : taskManager.getSubtasksByEpic(epic.getid())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : taskManager.getAllSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}
