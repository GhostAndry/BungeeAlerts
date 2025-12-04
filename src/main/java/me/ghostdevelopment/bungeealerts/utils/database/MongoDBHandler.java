package me.ghostdevelopment.bungeealerts.utils.database;

import com.mongodb.client.*;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.utils.Check;
import org.bson.Document;
import org.bukkit.Bukkit;
import java.util.ArrayList;
import java.util.List;

public class MongoDBHandler implements DatabaseHandler {

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    @Override
    public void init() {
        try {
            String uri = BungeeAlerts.getInstance().getConfig().getString("aclogs.mongodb.uri");
            String dbName = BungeeAlerts.getInstance().getConfig().getString("aclogs.mongodb.database");
            String colName = BungeeAlerts.getInstance().getConfig().getString("aclogs.mongodb.collection");

            mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(dbName);
            collection = database.getCollection(colName);
            Bukkit.getLogger().info("✅ [MongoDB] Connected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addLog(Check check) {
        Document doc = new Document("time", check.getTime())
                .append("playername", check.getPlayerName())
                .append("check_value", check.getCheck())
                .append("vl", check.getVl())
                .append("server", check.getServer())
                .append("description", check.getDescription())
                .append("check_info", check.getInfo());
        collection.insertOne(doc);
    }

    @Override
    public List<Check> getLogs(String playerName) {
        List<Check> checks = new ArrayList<>();
        // Trova documenti, ordina per _id (che contiene il timestamp) decrescente
        FindIterable<Document> results = collection.find(new Document("playername", playerName))
                .sort(new Document("_id", -1)); 

        for (Document doc : results) {
            checks.add(new Check(
                    doc.getString("time"),
                    doc.getString("playername"),
                    doc.getString("check_value"),
                    doc.getInteger("vl"),
                    doc.getString("server"),
                    doc.getString("description"),
                    doc.getString("check_info")
            ));
        }
        return checks;
    }

    @Override
    public void close() {
        if (mongoClient != null) mongoClient.close();
    }
}