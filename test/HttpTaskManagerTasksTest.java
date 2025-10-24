import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.Task;
import ru.tasktracker.model.TypeTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerTasksTest extends HttpTaskServerTestBase {

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task",
                LocalDateTime.now(), Duration.ofMinutes(5));

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() == 201,
                "Неверный статус код при создании задачи: " + response.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test Task", tasksFromManager.getFirst().getTaskName(), "Некорректное имя задачи");
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Original description",
                LocalDateTime.now(), Duration.ofMinutes(5));
        Task createdTask = manager.addTask(task);

        Task updatedTask = new Task(TypeTask.TASK, createdTask.getId(), "Updated Task", "Updated description",
                StatusTask.IN_PROGRESS, LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        String updatedJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при обновлении задачи");

        Task taskFromManager = manager.getTaskById(createdTask.getId());
        assertEquals("Updated Task", taskFromManager.getTaskName(), "Имя задачи не обновилось");
        assertEquals(StatusTask.IN_PROGRESS, taskFromManager.getStatusTask(), "Статус задачи не обновился");
    }

    @Test
    void testGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task",
                LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении задач");
        assertNotNull(response.body(), "Тело ответа не должно быть null");

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(1, tasks.size(), "Должна вернуться одна задача");
        assertEquals("Test Task", tasks.get(0).getTaskName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task",
                LocalDateTime.now(), Duration.ofMinutes(5));
        Task createdTask = manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении задачи по ID");

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals(createdTask.getId(), responseTask.getId(), "ID задачи не совпадает");
        assertEquals("Test Task", responseTask.getTaskName(), "Имя задачи не совпадает");
    }

    @Test
    void testGetTaskByInvalidId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 для несуществующей задачи");
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Testing task",
                LocalDateTime.now(), Duration.ofMinutes(5));
        Task createdTask = manager.addTask(task);

        // Проверяем, что задача добавлена
        assertEquals(1, manager.getAllTasks().size(), "Должна быть одна задача перед удалением");
        assertNotNull(manager.getTaskById(createdTask.getId()), "Задача должна существовать перед удалением");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + createdTask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при удалении задачи");

        // Проверяем, что задача действительно удалилась
        assertEquals(0, manager.getAllTasks().size(), "Задача должна была удалиться");
        assertNull(manager.getTaskById(createdTask.getId()), "Задача не должна находиться по ID");

        // Дополнительная проверка: GET после DELETE должен вернуть 404
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode(), "После удаления GET должен возвращать 404");
    }

    @Test
    void testAddTaskWithInvalidData() throws IOException, InterruptedException {
        String invalidJson = "{ invalid json }";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка 400 при некорректных данных");
        assertEquals(0, manager.getAllTasks().size(), "Задача не должна была добавиться");
    }
}