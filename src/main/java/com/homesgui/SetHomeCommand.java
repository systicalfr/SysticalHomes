package com.homesgui;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        String name = args.length > 0 ? args[0] : null;

        if (name != null) {
            HomeManager.Home existing = HomeManager.getHomeByName(player, name);
            if (existing != null) {
                HomeManager.setHome(player, existing.slot, name, player.getLocation());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                player.sendMessage("§aHome §6" + name + " §aupdated at your location!");
                return true;
            }
        }

        if (!HomeManager.hasFreeSlot(player)) {
            player.sendMessage("§cYou already have the maximum of " + HomesGUI.MAX_HOMES + " homes. Delete one first with /delhome or the /homes menu.");
            return true;
        }

        int slot = HomeManager.firstFreeSlot(player);
        String finalName = name != null ? name : "Home " + slot;
        HomeManager.setHome(player, slot, finalName, player.getLocation());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        player.sendMessage("§aHome §6" + finalName + " §aset at your location!");
        return true;
    }
}
