package com.homesgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HomesMenu {

    public static final int SIZE = 36;
    // Row 2 of 4 (indices 9-17), centered horizontally
    public static final int[] HOME_SLOTS = {11, 12, 13, 14, 15};
    // Row 3 of 4 (indices 18-26), directly below each home slot
    public static final int[] DELETE_SLOTS = {20, 21, 22, 23, 24};

    public static Inventory build(Player player) {
        HomesGUIHolder holder = new HomesGUIHolder();
        Inventory inv = Bukkit.createInventory(holder, SIZE, "HOMES");
        holder.setInventory(inv);

        // Background filler
        ItemStack filler = namedItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler);
        }

        for (int i = 0; i < HomesGUI.MAX_HOMES; i++) {
            int slotNum = i + 1;
            HomeManager.Home home = HomeManager.getHomeBySlot(player, slotNum);

            if (home != null) {
                List<String> lore = new ArrayList<>();
                lore.add("§7World: §f" + home.world);
                lore.add("§7X: §f" + Math.round(home.x) + "  §7Y: §f" + Math.round(home.y) + "  §7Z: §f" + Math.round(home.z));
                lore.add("");
                lore.add("§aClick to teleport here");
                ItemStack bed = namedItem(Material.ORANGE_BED, "§6§l" + home.name, lore);
                inv.setItem(HOME_SLOTS[i], bed);

                List<String> dlore = new ArrayList<>();
                dlore.add("§7Click twice to delete this home");
                ItemStack delete = namedItem(Material.RED_DYE, "§c§lDelete " + home.name, dlore);
                inv.setItem(DELETE_SLOTS[i], delete);
            } else {
                List<String> lore = new ArrayList<>();
                lore.add("§eClick to set a home here");
                lore.add("§7at your current location");
                ItemStack empty = namedItem(Material.GRAY_BED, "§7Empty Slot " + slotNum, lore);
                inv.setItem(HOME_SLOTS[i], empty);
                // delete slot underneath stays as filler glass
            }
        }

        return inv;
    }

    private static ItemStack namedItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
