package me.ghostdevelopment.bungeealerts.utils.database;

import me.ghostdevelopment.bungeealerts.utils.Check;
import java.util.List;

public interface DatabaseHandler {
    void init();
    void addLog(Check check);
    List<Check> getLogs(String playerName);
    void close();
}