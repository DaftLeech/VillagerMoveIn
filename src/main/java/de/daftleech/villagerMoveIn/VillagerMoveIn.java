package de.daftleech.villagerMoveIn;

import de.daftleech.villagerMoveIn.Commands.VMICommand;
import de.daftleech.villagerMoveIn.Listener.EventListener;
import de.daftleech.villagerMoveIn.Logic.Settings;
import de.daftleech.villagerMoveIn.Logic.VMILogic;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;


public final class VillagerMoveIn extends JavaPlugin {



    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void onEnable() {

        EventListener.getInstance().init(this);
        VMILogic.getInstance().init(this);

        //EventListener
        getServer().getPluginManager().registerEvents(EventListener.getInstance(), this);

        //Commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("vmi", "VillagerMoveIn basic command", VMICommand.getInstance());
        });

        //Config
        saveResource("config.yml", /* replace */ false);
        Settings.getInstance().loadConfig(this);

        if(Settings.getInstance().DEBUG)
            getLogger().info("Enable VillagerMoveIn v1.0 (normal Logger)");



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



}
