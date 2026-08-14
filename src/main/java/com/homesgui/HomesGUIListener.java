package com.homesgui;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomesGUIListener implements Listener {

    private final Map<UUID, Integer> pendingDelete = new HashMap<>();
    private final Map<UUID, Long> pendingDeleteTime = new HashMap<>();
    private static final long CONFIRM_WINDOW_MS = 5000;

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof HomesGUIHolder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) return;

        for (int i = 0; i < HomesMenu.HOME_SLOTS.length; i++) {
            if (HomesMenu.HOME_SLOTS[i] == rawSlot) {
                handleHomeClick(player, i + 1);
                return;
            }
        }

        for (int i = 0; i < HomesMenu.DELETE_SLOTS.length; i++) {
            if (HomesMenu.DELETE_SLOTS[i] == rawSlot) {
                handleDeleteClick(player, i + 1, event.getInventory());
                return;
            }
        }
    }

    private void handleHomeClick(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        if (home != null) {
            player.closeInventory();
            TeleportUtil.teleportWithCountdown(player, home);
        } else {
            if (!HomeManager.hasFreeSlot(player) && HomeManager.getHomeBySlot(player, slot) == null
                    && HomeManager.countHomes(player) >= HomesGUI.MAX_HOMES) {
                player.sendMessage("§cYou already have the maximum of " + HomesGUI.MAX_HOMES + " homes.");
                return;
            }
            Location loc = player.getLocation();
            String name = "Home " + slot;
            HomeManager.setHome(player, slot, name, loc);
            player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
            player.sendMessage("§aHome §6" + name + " §aset at your location!");
            player.closeInventory();
            player.openInventory(HomesMenu.build(player));
        }
    }

    private void handleDeleteClick(Player player, int slot, Inventory inv) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        if (home == null) return;

        UUID uuid = player.getUniqueId();
        Integer pending = pendingDelete.get(uuid);
        Long time = pendingDeleteTime.get(uuid);
        boolean confirmed = pending != null && pending == slot && time != null
                && (System.currentTimeMillis() - time) < CONFIRM_WINDOW_MS;

        if (confirmed) {
            HomeManager.deleteHome(player, slot);
            pendingDelete.remove(uuid);
            pendingDeleteTime.remove(uuid);
            player.sendMessage("§cHome §6" + home.name + " §cdeleted.");
            player.closeInventory();
            player.openInventory(HomesMenu.build(player));
        } else {
            pendingDelete.put(uuid, slot);
            pendingDeleteTime.put(uuid, System.currentTimeMillis());
            player.sendMessage("§eClick the delete button again to confirm deleting §6" + home.name + "§e.");

            int deleteSlotIndex = HomesMenu.DELETE_SLOTS[slot - 1];
            ItemStack confirmItem = inv.getItem(deleteSlotIndex);
            if (confirmItem != null) {
                ItemMeta meta = confirmItem.getItemMeta();
                meta.setDisplayName("§c§lClick again to confirm!");
                confirmItem.setItemMeta(meta);
                inv.setItem(deleteSlotIndex, confirmItem);
            }
        }
    }
}
