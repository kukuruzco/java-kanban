import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    protected final ArrayList<Task> historyList = new ArrayList<>(10);

    public void addHistoryList(Task task) {
        if (historyList.size() == 10) {
            historyList.removeFirst();
        }
        historyList.add(task);
    }

    public ArrayList<Task> getHistory() {
        return historyList;
    }
}
