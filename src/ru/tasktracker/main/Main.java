package ru.tasktracker.main;

import ru.tasktracker.model.*;
import ru.tasktracker.service.TaskManager;
import ru.tasktracker.util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager taskManager = Managers.getDefault();
        taskManager.addTask(new Task("Помыть посуду"
                ,"Вымыть всю посуду на кухне"
                , LocalDateTime.of(2025, 9, 11, 11, 30)
                , Duration.ofMinutes(25)));
        taskManager.addTask(new Task("Сделать ДЗ"
                , "Решить задачи по Java"
                , LocalDateTime.of(2025, 9, 12, 9, 20)
                , Duration.ofMinutes(25)));
        taskManager.addEpic(new Epic("Задачи на месяц", "Что надо сделать в августе"));
        taskManager.addEpic(new Epic("Построить дом", "Создать план постройки дома"));
        taskManager.addSubTask(new SubTask("Победить победителя"
                , "Не обязательно прям победить, но постараться"
                , 3
                , LocalDateTime.of(2025, 9, 12, 14, 30)
                , Duration.ofMinutes(15)));
            taskManager.addSubTask(new SubTask("Демонтаж стен"
                    , "Снести перегородку"
                    , 4
                    , LocalDateTime.of(2025, 9, 12, 15, 30)
                    , Duration.ofMinutes(20)));
        taskManager.addSubTask(new SubTask("Замена дверей"
                , "Монтаж дверных проемов"
                , 4
                , LocalDateTime.of(2025, 9, 12, 15, 55)
                , Duration.ofMinutes(20)));

        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getSubTaskById(6);
        taskManager.getTaskById(2);
        taskManager.getEpicById(4);

        Epic epic = taskManager.getEpicById(4);
        System.out.println(epic);
        System.out.println(epic.getSubTaskIds());
        taskManager.getSubTaskById(6);

        printAllTasks(taskManager);

        System.out.println("================================");

        // Изменяем статусы задач
        taskManager.updateTask(new Task(1
                , "Помыть посуду"
                , "Вымыть всю посуду на кухне"
                , StatusTask.DONE
                , LocalDateTime.of(2025, 9, 12, 16, 30)
                , Duration.ofMinutes(30)));
        taskManager.updateTask(new Task(2
                , "Сделать ДЗ"
                , "Решить задачи по Java"
                , StatusTask.IN_PROGRESS
                , LocalDateTime.of(2025, 9, 12, 17, 40)
                , Duration.ofMinutes(15)));
        taskManager.updateSubTask(new SubTask(5
                , "Победить победителя"
                , "Не обязательно прям победить, но постараться"
                , StatusTask.DONE
                , 3
                , LocalDateTime.of(2025, 9, 12, 19, 20)
                , Duration.ofMinutes(25)));
        taskManager.updateSubTask(new SubTask(6
                , "Демонтаж стен"
                , "Снести перегородку"
                , StatusTask.DONE
                , 4
                , LocalDateTime.of(2025, 9, 12, 19, 50)
                , Duration.ofMinutes(25)));
        taskManager.updateSubTask(new SubTask(7
                , "Замена дверей"
                , "Установка новых дверей"
                , StatusTask.IN_PROGRESS
                , 4
                , LocalDateTime.of(2025, 9, 12, 20, 20)
                , Duration.ofMinutes(15)));

//        taskManager.getEpicById(3);

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

            for (Task task : taskManager.getSubtasksByEpic(epic.getId())) {
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
