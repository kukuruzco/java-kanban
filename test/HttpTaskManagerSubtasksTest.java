import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import ru.tasktracker.exceptions.NotFoundException;
import ru.tasktracker.model.Epic;
import ru.tasktracker.model.StatusTask;
import ru.tasktracker.model.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerSubtasksTest extends HttpTaskServerTestBase {

    @Test
    void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));

        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при создании подзадачи");

        List<SubTask> subtasksFromManager = manager.getAllSubTasks();
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Test Subtask", subtasksFromManager.get(0).getTaskName(), "Некорректное имя подзадачи");
        assertEquals(createdEpic.getId(), subtasksFromManager.get(0).getEpicId(), "Некорректный ID эпика");
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Original Subtask", "Original description",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        SubTask createdSubtask = manager.addSubTask(subtask);

        SubTask updatedSubtask = new SubTask(createdSubtask.getId(), "Updated Subtask", "Updated description",
                StatusTask.DONE, createdEpic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));

        String updatedJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при обновлении подзадачи");

        SubTask subtaskFromManager = manager.getSubTaskById(createdSubtask.getId());
        assertEquals("Updated Subtask", subtaskFromManager.getTaskName(), "Имя подзадачи не обновилось");
        assertEquals(StatusTask.DONE, subtaskFromManager.getStatusTask(), "Статус подзадачи не обновился");
    }

    @Test
    void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении подзадач");

        List<SubTask> subtasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {
        }.getType());
        assertEquals(1, subtasks.size(), "Должна вернуться одна подзадача");
        assertEquals("Test Subtask", subtasks.get(0).getTaskName(), "Некорректное имя подзадачи");
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        SubTask createdSubtask = manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении подзадачи по ID");

        SubTask responseSubtask = gson.fromJson(response.body(), SubTask.class);
        assertEquals(createdSubtask.getId(), responseSubtask.getId(), "ID подзадачи не совпадает");
        assertEquals("Test Subtask", responseSubtask.getTaskName(), "Имя подзадачи не совпадает");
    }

    @Test
    void testGetSubtaskByInvalidId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 для несуществующей подзадачи");
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        SubTask createdSubtask = manager.addSubTask(subtask);

        // Проверяем, что подзадача добавлена
        assertEquals(1, manager.getAllSubTasks().size(), "Должна быть одна подзадача перед удалением");
        assertNotNull(manager.getSubTaskById(createdSubtask.getId()), "Подзадача должна существовать перед удалением");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + createdSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при удалении подзадачи");

        // Проверяем, что подзадача действительно удалилась
        assertEquals(0, manager.getAllSubTasks().size(), "Подзадача должна была удалиться");

        // Правильная проверка - после удаления getSubTaskById должен бросать исключение
        assertThrows(NotFoundException.class, () -> manager.getSubTaskById(createdSubtask.getId()),
                "После удаления getSubTaskById должен бросать NotFoundException");

        // Дополнительная проверка: GET после DELETE должен вернуть 404
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode(), "После удаления GET должен возвращать 404");
    }

    @Test
    void testAddSubtaskWithInvalidEpic() throws IOException, InterruptedException {
        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                999, LocalDateTime.now(), Duration.ofMinutes(5));

        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 при создании подзадачи с несуществующим эпиком");
        assertEquals(0, manager.getAllSubTasks().size(), "Подзадача не должна была добавиться");
    }

    @Test
    void testUpdateNonExistentSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        manager.addEpic(epic);

        SubTask nonExistentSubtask = new SubTask(999, "Non-existent Subtask", "Description",
                StatusTask.DONE, 1, LocalDateTime.now(), Duration.ofMinutes(30));

        String subtaskJson = gson.toJson(nonExistentSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 при обновлении несуществующей подзадачи");
    }

    @Test
    void testAddSubtaskWithInvalidData() throws IOException, InterruptedException {
        String invalidJson = "{ invalid json }";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Ожидалась ошибка 400 при некорректных данных");
        assertEquals(0, manager.getAllSubTasks().size(), "Подзадача не должна была добавиться");
    }
}