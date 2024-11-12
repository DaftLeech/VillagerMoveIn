package de.daftleech.villagerMoveIn.Listener;

import de.daftleech.villagerMoveIn.Logic.Settings;
import de.daftleech.villagerMoveIn.Logic.VMILogic;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;



public class EventListener implements Listener {

    private static final EventListener instance = new EventListener();

    private EventListener(){
    }

    public static EventListener getInstance(){
        return instance;
    }

    private Logger LOGGER;
    private static final VMILogic LOGIC = VMILogic.getInstance();
    private final boolean enableDebug = Settings.getInstance().DEBUG;

    public void init(JavaPlugin plugin){
        LOGGER = plugin.getLogger();
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event){

        if(event.getFrom().getChunk().equals(event.getTo().getChunk())) return;

        if(enableDebug)
            LOGGER.info("next chunk");

        Player player = event.getPlayer();

        List<Villager> villagerList = player.getWorld().getNearbyEntitiesByType(Villager.class, player.getLocation(), 16).stream().toList();
        if(!villagerList.isEmpty())
            LOGIC.addFoundVillageType(player, villagerList.getFirst().getVillagerType().toString());

    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event){
        if(event.getInventory().getType() != InventoryType.MERCHANT) return;

        if(enableDebug)
            LOGGER.info("closed merchant inventory");
        if(event.getInventory().getHolder() instanceof Villager) {

            if(enableDebug)
                LOGGER.info("inventory holder is villager");


            LOGIC.addFoundVillageType((Player) event.getPlayer(), ((Villager) event.getInventory().getHolder()).getVillagerType().toString());
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent event){

        if (event.getEntityType() != EntityType.WANDERING_TRADER) return;

        WanderingTrader wanderingTrader = (WanderingTrader) event.getEntity();

        if(enableDebug)
            LOGGER.info("wandering trader spawned");

        LOGIC.spawnVillager(wanderingTrader);

    }


}
