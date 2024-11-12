package de.daftleech.villagerMoveIn.Commands;


import de.daftleech.villagerMoveIn.Logic.VMILogic;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("UnstableApiUsage")
public class VMICommand implements BasicCommand {

    private static final VMICommand instance = new VMICommand();
    private VMICommand (){}

    public static VMICommand getInstance(){
        return instance;
    }



    @Override
    public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("info"))
            info(commandSourceStack);
        if (args.length == 1 && args[0].equalsIgnoreCase("reset"))
            reset(commandSourceStack);
        if (args.length == 2 && args[0].equalsIgnoreCase("add"))
            add(commandSourceStack, args[1]);
        /*
            if (args.length == 2 && args[0].equalsIgnoreCase("remove"))
            remove(commandSourceStack, args[1]);
            */
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
        if (args.length == 1)
            return List.of("info","reset","add","remove");
        return BasicCommand.super.suggest(commandSourceStack, args);
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public @Nullable String permission() {
        return BasicCommand.super.permission();
    }



    public void info(CommandSourceStack commandSourceStack){
        for(Player player : getServer().getOnlinePlayers()){
            commandSourceStack.getSender().sendMessage(player.getName()+":");
            PersistentDataContainer playerData = player.getPersistentDataContainer();
            List<String> villagerTypes = playerData.get(VMILogic.getInstance().VILLAGE_TYPES, PersistentDataType.LIST.strings());
            if(villagerTypes == null) continue;
            for(String villagerType : villagerTypes){
                commandSourceStack.getSender().sendMessage("    "+villagerType);
            }
        }

    }

    public void reset(CommandSourceStack commandSourceStack){
        for(Player player : getServer().getOnlinePlayers()){
            PersistentDataContainer playerData = player.getPersistentDataContainer();
            playerData.remove(VMILogic.getInstance().VILLAGE_TYPES);
            playerData.remove(VMILogic.getInstance().FOUND_VILLAGE);
            commandSourceStack.getSender().sendMessage(player.getName()+" reset.");
        }
    }

    public void add(CommandSourceStack commandSourceStack, String villageType){

        if(!(commandSourceStack.getSender() instanceof Player)) return;

        VMILogic.getInstance().addFoundVillageType((Player) commandSourceStack.getSender(), villageType);
    }
}
