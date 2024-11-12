package de.daftleech.villagerMoveIn.Logic;

import de.daftleech.villagerMoveIn.VillagerMoveIn;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;


public class VMILogic {

    private static final VMILogic instance = new VMILogic();
    private VMILogic (){}

    public static VMILogic getInstance(){
        return instance;
    }

    public void init(JavaPlugin plugin){
        LOGGER = plugin.getLogger();
        getInstance().plugin = plugin;
    }

    private Logger LOGGER;
    private JavaPlugin plugin;
    private final boolean enableDebug = Settings.getInstance().DEBUG;
    public final NamespacedKey FOUND_VILLAGE = new NamespacedKey("villagermovein", "foundvillage");
    public final NamespacedKey VILLAGE_TYPES = new NamespacedKey("villagermovein", "villagetypes");


    /**
     * Gets called by the <code>onInventoryCloseEvent</code> or <code>onPlayerMoveEvent</code>.
     * <p>
     * Saves the information for the <code>player</code> that he had found a village and the type of village he found.
     * <p>
     * Sends out a message about his discovery on the server.
     * @param player  Player who found the village
     * @param villageType  Type of village found
     */
    public void addFoundVillageType(Player player, String villageType){
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        //Player found a village
        playerData.set(FOUND_VILLAGE, PersistentDataType.BOOLEAN, true);

        //Add village type to list
        List<String> type_list = new ArrayList<>();
        if(playerData.has(VILLAGE_TYPES, PersistentDataType.LIST.strings()))
            type_list = playerData.get(VILLAGE_TYPES, PersistentDataType.LIST.strings());

        //only if the type didn't exist before
        if (type_list != null && !type_list.contains(villageType)) {
            type_list.add(villageType);

            playerData.set(VILLAGE_TYPES
                    , PersistentDataType.LIST.strings()
                    , type_list);

            if (enableDebug)
                LOGGER.info(player.getName() + " found a village. " + villageType);

            //tell everyone about it
            TextComponent broadcastMsg = getBroadcastMsg(Settings.getInstance().VILLAGER_DISCOVERED, player, villageType);

            if(!Settings.getInstance().VILLAGER_DISCOVERED.isEmpty())
                plugin.getServer().sendMessage(broadcastMsg);


            if (enableDebug)
                LOGGER.info(broadcastMsg.content());

        }

    }

    /**
     * Format the <code>msg</code> provided. Replaces < playerName > and < villagerType > with
     * the provided parameters.
     * @param msg  Text message
     * @param player Player used for playerName replacements
     * @param villagerType Villager type used for replacements
     * @return TextComponent
     */
    private static @NotNull TextComponent getBroadcastMsg(String msg, Player player, String villagerType) {

        TextComponent broadcastMsg = Component.text("", NamedTextColor.WHITE);
        for (String msgPart : msg.split("[<>]"))
            broadcastMsg = broadcastMsg.append(Component.text(
                              msgPart.equals("playerName") ? player.getName()
                            : msgPart.equals("villagerType") ? villagerType
                            : msgPart
                    , List.of("playerName", "villagerType").contains(msgPart)
                            ? NamedTextColor.YELLOW : NamedTextColor.WHITE));
        return broadcastMsg;
    }

    /**
     * Gets called by the <code>onEntitySpawnEvent</code>.
     * <p>
     * Looks for the closest player to the <code>wanderingTrader</code>
     * and checks whether this player had found a village yet.
     * <p>
     * Spawns a villager of a random type of discovered villager types.
     * @param wanderingTrader The wandering Trader that just spawned
     */
    public void spawnVillager(WanderingTrader wanderingTrader){

        //get the closest player to the wandering trader
        int radius = 48 * 48;
        Player other = wanderingTrader.getWorld().getPlayers().stream()
                .filter((p) -> p.getLocation().distanceSquared(wanderingTrader.getLocation()) <= radius)
                .min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(wanderingTrader.getLocation())))
                .orElse(null);

        //no player nearby no villager for you; yikes
        if(other == null) {
            if(enableDebug)
                LOGGER.severe("no player nearby");
            return;
        }
        if(enableDebug)
            LOGGER.info("player nearby: "+other.getName());

        PersistentDataContainer playerData = other.getPersistentDataContainer();

        //played found a village
        if (playerData.has(FOUND_VILLAGE, PersistentDataType.BOOLEAN)){
            if(Boolean.TRUE.equals(playerData.get(FOUND_VILLAGE, PersistentDataType.BOOLEAN))){

                Villager.Type villagerType = null;

                //get a random villager type out of all discovered types
                if(playerData.has(VILLAGE_TYPES, PersistentDataType.LIST.strings())) {
                    List<String> typeList = playerData.get(VILLAGE_TYPES, PersistentDataType.LIST.strings());
                    if (typeList != null) {
                        Collections.shuffle(typeList);
                        NamespacedKey villager_type_key = NamespacedKey.fromString(typeList.getFirst().toLowerCase(Locale.ROOT));
                        if(villager_type_key == null) return;
                        villagerType = Registry.VILLAGER_TYPE.get(villager_type_key);
                        if(enableDebug)
                            LOGGER.info("selected villagerType for spawning: "+villagerType.toString());
                    }
                }

                //shouldn't be possible but whatever
                if(villagerType == null)
                    villagerType = Villager.Type.PLAINS;

                //don't spawn sometimes I guess
                if(Settings.getInstance().VILLAGER_SPAWN_PERCENTAGE < Math.random()) return;

                //spawn villager at wanderer location with random discovered type
                Villager villager = (Villager) wanderingTrader.getWorld().spawnEntity(wanderingTrader.getLocation(), EntityType.VILLAGER);
                villager.setVillagerType(villagerType);
                if(enableDebug)
                    LOGGER.info("villager spawned");


                //tell everyone about it
                TextComponent broadcastMsg = getBroadcastMsg(Settings.getInstance().WANDERER_SPAWN, other, villagerType.toString());

                if(!Settings.getInstance().WANDERER_SPAWN.isEmpty())
                    plugin.getServer().sendMessage(broadcastMsg);
            }
        }
    }
}
