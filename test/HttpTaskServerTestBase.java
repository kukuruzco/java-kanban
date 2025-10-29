import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.tasktracker.main.HttpTaskServer;
import ru.tasktracker.service.managers.TaskManager;
import ru.tasktracker.util.Managers;

import java.io.IOException;
import java.net.http.HttpClient;

public class HttpTaskServerTestBase {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected HttpClient client;

    @BeforeEach
    public void setUp() throws IOException {
        manager = Managers.getDefault();
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        client = HttpClient.newHttpClient();
        taskServer.start();

        manager.deleteAllTasks();
        manager.deleteAllSubTasks();
        manager.deleteAllEpics();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }
}