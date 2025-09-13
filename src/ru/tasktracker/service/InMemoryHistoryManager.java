package ru.tasktracker.service;

import ru.tasktracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private Node head;
    private Node tail;
    private int size = 0;
    private final Map<Integer, Node> nodeMap;

    public InMemoryHistoryManager() {
        head = null;
        tail = null;
        nodeMap = new HashMap<>();
    }

    @Override
    public void addHistory(Task task) {
        if (task == null) {
            return;
        }

        int taskid = task.getId();

        if (nodeMap.containsKey(taskid)) {
            removeNode(nodeMap.get(taskid));
        }

        // Добавляем задачу в конец списка
        Node newNode = new Node(task);
        linkLast(newNode);
        nodeMap.put(taskid, newNode);
        size++;
    }

    @Override
    public void remove(int id) {
        if (nodeMap.containsKey(id)) {
            removeNode(nodeMap.get(id));
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Node node) {
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        nodeMap.remove(node.task.getId());
        size--;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head;

        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
        nodeMap.clear();
    }

    public boolean contains(int taskId) {
        return nodeMap.containsKey(taskId);
    }
}