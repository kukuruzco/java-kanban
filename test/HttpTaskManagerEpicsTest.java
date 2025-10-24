import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HttpTaskManagerEpicsTest extends HttpTaskServerTestBase {

    @Test
    void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");

        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при создании эпика");

        List<Epic> epicsFromManager = manager.getAllEpics();
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Test Epic", epicsFromManager.get(0).getTaskName(), "Некорректное имя эпика");
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Original Epic", "Original description");
        Epic createdEpic = manager.addEpic(epic);

        Epic updatedEpic = new Epic(createdEpic.getId(), "Updated Epic", "Updated description", StatusTask.IN_PROGRESS);

        String updatedJson = gson.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный статус код при обновлении эпика");

        Epic epicFromManager = manager.getEpicById(createdEpic.getId());
        assertEquals("Updated Epic", epicFromManager.getTaskName(), "Имя эпика не обновилось");
        assertEquals("Updated description", epicFromManager.getTaskDescription(), "Описание эпика не обновилось");

    }

    @Test
    void testEpicStatusCalculation() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        // Создаем подзадачи
        SubTask subtask1 = new SubTask("Subtask 1", "Description 1",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        SubTask createdSubtask1 = manager.addSubTask(subtask1);

        SubTask subtask2 = new SubTask("Subtask 2", "Description 2",
                createdEpic.getId(), LocalDateTime.now().plusHours(1), Duration.ofMinutes(10));
        manager.addSubTask(subtask2);

        // Проверяем начальный статус (все подзадачи NEW -> эпик NEW)
        Epic epicFromManager = manager.getEpicById(createdEpic.getId());
        assertEquals(StatusTask.NEW, epicFromManager.getStatusTask(),
                "Эпик должен быть NEW когда все подзадачи NEW");

        // Обновляем одну подзадачу на IN_PROGRESS
        SubTask updatedSubtask1 = new SubTask(createdSubtask1.getId(), "Subtask 1", "Description 1",
                StatusTask.IN_PROGRESS, createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        String updatedJson = gson.toJson(updatedSubtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный статус код при обновлении подзадачи");

        // Проверяем, что статус эпика изменился на IN_PROGRESS
        epicFromManager = manager.getEpicById(createdEpic.getId());
        assertEquals(StatusTask.IN_PROGRESS, epicFromManager.getStatusTask(),
                "Эпик должен быть IN_PROGRESS когда есть подзадача IN_PROGRESS");
    }

    @Test
    void testGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении эпиков");

        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());
        assertEquals(1, epics.size(), "Должен вернуться один эпик");
        assertEquals("Test Epic", epics.get(0).getTaskName(), "Некорректное имя эпика");
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении эпика по ID");

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(createdEpic.getId(), responseEpic.getId(), "ID эпика не совпадает");
        assertEquals("Test Epic", responseEpic.getTaskName(), "Имя эпика не совпадает");
    }

    @Test
    void testGetEpicByInvalidId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Ожидалась ошибка 404 для несуществующего эпика");
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + createdEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при удалении эпика");

        assertEquals(0, manager.getAllEpics().size(), "Эпик должен был удалиться");
        assertNull(manager.getEpicById(createdEpic.getId()), "Эпик не должен находиться по ID");
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Testing epic");
        Epic createdEpic = manager.addEpic(epic);

        SubTask subtask = new SubTask("Test Subtask", "Testing subtask",
                createdEpic.getId(), LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + createdEpic.getId() + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код при получении подзадач эпика");

        List<SubTask> subtasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>(){}.getType());
        assertEquals(1, subtasks.size(), "Должна вернуться одна подзадача");
        assertEquals("Test Subtask", subtasks.get(0).getTaskName(), "Некорректное имя подзадачи");
    }
}