package com.homesgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.openInventory(HomesMenu.build(player));
            return true;
        }

        HomeManager.Home home = HomeManager.getHomeByName(player, args[0]);
        if (home == null) {
            player.sendMessage("§cNo home found named §6" + args[0] + "§c.");
            return true;
        }
        TeleportUtil.teleportWithCountdown(player, home);
        return true;
    }
}
