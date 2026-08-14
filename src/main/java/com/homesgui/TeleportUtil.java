package com.homesgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TeleportUtil {

    // Players currently mid-countdown, so a second click can't stack timers
    private static final Set<UUID> pending = new HashSet<>();

    public static void teleportWithCountdown(Player player, HomeManager.Home home) {
        Location loc = home.toLocation();
        if (loc == null) {
            player.sendMessage("§cThat home's world no longer exists.");
            return;
        }

        UUID uuid = player.getUniqueId();
        if (pending.contains(uuid)) {
            player.sendMessage("§cYou already have a teleport in progress.");
            return;
        }
        pending.add(uuid);

        new BukkitRunnable() {
            int count = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    pending.remove(uuid);
                    cancel();
                    return;
                }

                if (count > 0) {
                    player.sendActionBar(Component.text("Teleporting in " + count + "...", NamedTextColor.GOLD));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    count--;
                } else {
                    player.teleport(loc);
                    player.sendActionBar(Component.text("Teleported!", NamedTextColor.GOLD));
                    player.sendMessage("§aTeleported to §6" + home.name + "§a.");
                    pending.remove(uuid);
                    cancel();
                }
            }
        }.runTaskTimer(HomesGUI.getInstance(), 0L, 20L);
    }
}
