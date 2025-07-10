package me.ghostdevelopment.bungeealerts.utils;

public class Check {
    private final String time;
    private final String playerName;
    private final String check;
    private final int vl;
    private final String server;
    private final String description;
    private final String info;

    public Check(String time, String playerName, String check, int vl, String server, String description, String info) {
        this.time = time;
        this.playerName = playerName;
        this.check = check;
        this.vl = vl;
        this.server = server;
        this.description = description;
        this.info = info;
    }

    // Getters
    public String getTime() { return time; }
    public String getPlayerName() { return playerName; }
    public String getCheck() { return check; }
    public int getVl() { return vl; }
    public String getServer() { return server; }
    public String getDescription() { return description; }
    public String getInfo() { return info; }
}