package de.daftleech.villagerMoveIn.Logic;

import org.bukkit.plugin.java.JavaPlugin;

public class Settings {

    private static final Settings instance = new Settings();

    private Settings (){}

    public static Settings getInstance(){
        return instance;
    }

    public boolean DEBUG = false;
    public double VILLAGER_SPAWN_PERCENTAGE = 1.0;
    public String VILLAGER_DISCOVERED = "";
    public String WANDERER_SPAWN = "";

    public void loadConfig(JavaPlugin plugin){
        try {
            DEBUG = plugin.getConfig().getBoolean("settings.debug");
            VILLAGER_SPAWN_PERCENTAGE = plugin.getConfig().getDouble("settings.villager-spawn-percentage");
            VILLAGER_DISCOVERED = plugin.getConfig().getString("messages.village-discover");
            WANDERER_SPAWN = plugin.getConfig().getString("messages.wanderer-spawn");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
