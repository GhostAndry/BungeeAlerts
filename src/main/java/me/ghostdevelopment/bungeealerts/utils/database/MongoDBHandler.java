package me.ghostdevelopment.bungeealerts.utils.database;

import com.mongodb.client.*;
import me.ghostdevelopment.bungeealerts.BungeeAlerts;
import me.ghostdevelopment.bungeealerts.Settings;
import me.ghostdevelopment.bungeealerts.utils.Check;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MongoDB storage backend for AC violation logs.
 * <p>
 * Stores each violation as a document in the configured collection.
 * Logs are sorted by {@code _id} descending (newest first).
 */
public final class MongoDBHandler implements DatabaseHandler {

    private MongoClient mongoClient;
    private MongoCollection<Document> collection;
    private final Logger logger = Bukkit.getLogger();

    @Override
    public void init() {
        try {
            var config = BungeeAlerts.getInstance().getConfig();
            String uri = config.getString(Settings.CFG_MONGO_URI, "mongodb://localhost:27017");
            String dbName = config.getString(Settings.CFG_MONGO_DATABASE, "bungeealerts");
            String colName = config.getString(Settings.CFG_MONGO_COLLECTION, "aclogs");

            mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase(dbName);
            collection = database.getCollection(colName);
            logger.info("[MongoDB] Connected to " + dbName + "." + colName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[MongoDB] Connection failed", e);
        }
    }

    @Override
    public void addLog(Check check) {
        if (collection == null) return;
        Document doc = new Document("time", check.getTime())
                .append("playername", check.getPlayerName())
                .append("check_value", check.getCheck())
                .append("vl", check.getVl())
                .append("server", check.getServer())
                .append("description", check.getDescription())
                .append("check_info", check.getInfo())
                .append("count", check.getCount());
        collection.insertOne(doc);
    }

    @Override
    public List<Check> getLogs(String playerName) {
        List<Check> checks = new ArrayList<>();
        if (collection == null) return checks;

        FindIterable<Document> results = collection
                .find(new Document("playername", playerName))
                .sort(new Document("_id", -1));

        for (Document doc : results) {
            checks.add(mapDocument(doc));
        }
        return checks;
    }

    @Override
    public List<Check> getLogsPage(String playerName, int page, int pageSize) {
        List<Check> checks = new ArrayList<>();
        if (collection == null) return checks;

        int skip = (page - 1) * pageSize;
        FindIterable<Document> results = collection
                .find(new Document("playername", playerName))
                .sort(new Document("_id", -1))
                .skip(skip)
                .limit(pageSize);

        for (Document doc : results) {
            checks.add(mapDocument(doc));
        }
        return checks;
    }

    @Override
    public int getLogCount(String playerName) {
        if (collection == null) return 0;
        return (int) collection.countDocuments(new Document("playername", playerName));
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }

    // ── internal ─────────────────────────────────────────────

    private static Check mapDocument(Document doc) {
        int count = doc.getInteger("count", 1);
        return new Check(
                doc.getString("time"),
                doc.getString("playername"),
                doc.getString("check_value"),
                doc.getInteger("vl", 0),
                doc.getString("server"),
                doc.getString("description"),
                doc.getString("check_info"),
                count
        );
    }
}
