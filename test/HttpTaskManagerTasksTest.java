import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.tasktracker.exceptions.NotFoundException;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.Task;

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
        System.out.println("Отправляемый JSON: " + taskJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код: " + response.statusCode());
        System.out.println("Тело ответа: " + response.body());

        assertEquals(201, response.statusCode(),
                "Неверный статус код при создании задачи: " + response.statusCode() + ". Тело: " + response.body());

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

        Task updatedTask = new Task(createdTask.getId(), "Updated Task", "Updated description",
                StatusTask.IN_PROGRESS, LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        String updatedJson = gson.toJson(updatedTask);
        System.out.println("Отправляемый JSON для обновления: " + updatedJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код обновления: " + response.statusCode());
        System.out.println("Тело ответа обновления: " + response.body());

        assertEquals(201, response.statusCode(),
                "Неверный статус код при обновлении задачи: " + response.statusCode() + ". Тело: " + response.body());

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

        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
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

        assertEquals(0, manager.getAllTasks().size(), "Задача должна была удалиться");

        assertThrows(NotFoundException.class, () -> manager.getTaskById(createdTask.getId()),
                "После удаления getTaskById должен бросать NotFoundException");

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

    @Test
    void testAddTaskWithEmptyName() throws IOException, InterruptedException {
        String taskJson = "{\"taskName\": \"\", \"taskDescription\": \"Description\", \"startTime\": \"2025-10-26T10:00:00\", \"duration\": 30}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка 400 при пустом имени задачи");
        assertTrue(response.body().contains("Имя задачи обязательно"), "Должна быть ошибка о необходимости имени задачи");
    }

    @Test
    void testAddTaskWithNullTime() throws IOException, InterruptedException {
        Task task = new Task("Test Task Null Time", "Testing task with null time", null, null);

        String taskJson = gson.toJson(task);
        System.out.println("Отправляемый JSON с null временем: " + taskJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код для null времени: " + response.statusCode());
        System.out.println("Тело ответа для null времени: " + response.body());

        assertEquals(201, response.statusCode(),
                "Неверный статус код при создании задачи с null временем: " + response.statusCode() + ". Тело: " + response.body());

        List<Task> tasksFromManager = manager.getAllTasks();
        assertEquals(1, tasksFromManager.size(), "Задача с null временем должна быть добавлена");

        Task createdTask = tasksFromManager.getFirst();
        assertEquals("Test Task Null Time", createdTask.getTaskName(), "Некорректное имя задачи");
        assertNull(createdTask.getStartTime(), "StartTime должен быть null");
        if (createdTask.getDuration() != null) {
            assertEquals(Duration.ZERO, createdTask.getDuration(), "Duration должен быть ZERO");
        } else {
            assertNull(createdTask.getDuration(), "Duration должен быть null");
        }
    }

    @Test
    void testUpdateNonExistentTask() throws IOException, InterruptedException {
        Task nonExistentTask = new Task(999, "Non-existent Task", "Description",
                StatusTask.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(30));

        String taskJson = gson.toJson(nonExistentTask);
        System.out.println("Отправляемый JSON для несуществующей задачи: " + taskJson);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код для несуществующей задачи: " + response.statusCode());
        System.out.println("Тело ответа для несуществующей задачи: " + response.body());

        assertEquals(400, response.statusCode(),
                "Ожидалась ошибка 400 при обновлении несуществующей задачи: " + response.statusCode() + ". Тело: " + response.body());
        assertTrue(response.body().contains("Ошибка обновления"), "Должна быть ошибка обновления");
    }

    @Test
    void testAddOverlappingTask() throws IOException, InterruptedException {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("First Task", "First task description",
                baseTime, Duration.ofHours(1));

        String task1Json = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Первая задача должна быть создана успешно");

        Task overlappingTask = new Task("Overlapping Task", "Overlapping task description",
                baseTime.plusMinutes(30), Duration.ofHours(1));

        String overlappingTaskJson = gson.toJson(overlappingTask);
        System.out.println("Отправляемый JSON для пересекающейся задачи: " + overlappingTaskJson);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(overlappingTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код для пересекающейся задачи: " + response2.statusCode());
        System.out.println("Тело ответа для пересекающейся задачи: " + response2.body());

        assertEquals(406, response2.statusCode(),
                "Ожидалась ошибка 406 при добавлении пересекающейся задачи: " + response2.statusCode() + ". Тело: " + response2.body());
        assertTrue(response2.body().contains("Обнаружено пересечение по времени"), "Должна быть ошибка пересечения времени");

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size(), "Пересекающаяся задача не должна была добавиться");
        assertEquals("First Task", tasks.getFirst().getTaskName(), "Должна остаться только первая задача");
    }

    @Test
    void testUpdateTaskToOverlappingTime() throws IOException, InterruptedException {
        LocalDateTime baseTime = LocalDateTime.now();

        Task task1 = new Task("First Task", "First task description",
                baseTime, Duration.ofHours(1));

        String task1Json = gson.toJson(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task1Json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Первая задача должна быть создана успешно");

        Task task2 = new Task("Second Task", "Second task description",
                baseTime.plusHours(2), Duration.ofHours(1));

        String task2Json = gson.toJson(task2);

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(task2Json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode(), "Вторая задача должна быть создана успешно");

        List<Task> tasks = manager.getAllTasks();
        Task createdTask2 = tasks.stream()
                .filter(t -> t.getTaskName().equals("Second Task"))
                .findFirst()
                .orElseThrow();

        Task updatedTask2 = new Task(createdTask2.getId(), "Updated Second Task", "Updated description",
                createdTask2.getStatusTask(), baseTime.plusMinutes(30), Duration.ofHours(1));

        String updatedTaskJson = gson.toJson(updatedTask2);
        System.out.println("Отправляемый JSON для обновления на пересекающееся время: " + updatedTaskJson);

        HttpRequest request3 = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response3 = client.send(request3, HttpResponse.BodyHandlers.ofString());

        System.out.println("Статус код для обновления на пересекающееся время: " + response3.statusCode());
        System.out.println("Тело ответа для обновления на пересекающееся время: " + response3.body());

        assertEquals(400, response3.statusCode(),
                "Ожидалась ошибка 400 при обновлении задачи на пересекающееся время: " + response3.statusCode() + ". Тело: " + response3.body());
        assertTrue(response3.body().contains("Ошибка обновления"), "Должна быть ошибка обновления");

        Task taskAfterUpdate = manager.getTaskById(createdTask2.getId());
        assertEquals("Second Task", taskAfterUpdate.getTaskName(), "Имя задачи не должно было измениться");
        assertEquals(baseTime.plusHours(2), taskAfterUpdate.getStartTime(), "Время начала не должно было измениться");
    }

    @Test
    void testAddTaskWithEmptyBody() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка 400 при пустом теле запроса");
        assertTrue(response.body().contains("Тело запроса пустое"), "Должна быть ошибка пустого тела");
    }

    @Test
    void testInvalidPath() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 при неверном пути");
    }

    @Test
    void testMethodNotAllowed() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(), "Ожидалась ошибка 405 для неподдерживаемого метода");
    }
}