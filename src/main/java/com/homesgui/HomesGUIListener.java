package com.homesgui;

import io.papermc.paper.dialog.DialogResponseView;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class HomesGUIListener {

    private HomesGUIListener() {
    }

    public static void handle(Player player, String value, DialogResponseView response) {
        if (value.equals("back_main")) {
            player.showDialog(HomesDialogs.mainMenu(player));
            return;
        }

        if (value.startsWith("new_home_")) {
            createHome(player, parseTrailingInt(value, "new_home_"));
            return;
        }

        if (value.startsWith("home_detail_")) {
            player.showDialog(HomesDialogs.homeDetail(player, parseTrailingInt(value, "home_detail_")));
            return;
        }

        if (value.startsWith("teleport_")) {
            int slot = parseTrailingInt(value, "teleport_");
            HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
            if (home != null) {
                TeleportUtil.teleportWithCountdown(player, home);
            }
            return;
        }

        if (value.startsWith("rename_open_")) {
            player.showDialog(HomesDialogs.renameDialog(player, parseTrailingInt(value, "rename_open_")));
            return;
        }

        if (value.startsWith("rename_save_")) {
            int slot = parseTrailingInt(value, "rename_save_");
            String newName = readText(response, "new_name");

            if (newName != null && !newName.isBlank()) {
                HomeManager.renameHome(player, slot, newName.trim());
            }

            player.showDialog(HomesDialogs.homeDetail(player, slot));
            return;
        }

        if (value.startsWith("icon_open_")) {
            player.showDialog(HomesDialogs.iconPicker(player, parseTrailingInt(value, "icon_open_"), null));
            return;
        }

        if (value.startsWith("icon_search_")) {
            int slot = parseTrailingInt(value, "icon_search_");
            player.showDialog(HomesDialogs.iconPicker(player, slot, readText(response, "icon_search")));
            return;
        }

        if (value.startsWith("icon_pick_")) {
            String rest = value.substring("icon_pick_".length());
            int underscore = rest.indexOf('_');

            if (underscore == -1) {
                return;
            }

            int slot = Integer.parseInt(rest.substring(0, underscore));
            String material = rest.substring(underscore + 1).toUpperCase();

            HomeManager.setIcon(player, slot, material);
            player.showDialog(HomesDialogs.homeDetail(player, slot));
            return;
        }

        if (value.startsWith("delete_open_")) {
            player.showDialog(HomesDialogs.deleteConfirm(player, parseTrailingInt(value, "delete_open_")));
            return;
        }

        if (value.startsWith("delete_confirm_")) {
            int slot = parseTrailingInt(value, "delete_confirm_");
            HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
            String name = home != null ? home.name : "Home " + slot;

            HomeManager.deleteHome(player, slot);
            player.sendMessage("§cHome §6" + name + " §cdeleted.");
            player.showDialog(HomesDialogs.mainMenu(player));
        }
    }

    private static void createHome(Player player, int slot) {
        if (HomeManager.getHomeBySlot(player, slot) == null
                && HomeManager.countHomes(player) >= HomesGUI.MAX_HOMES) {
            player.sendMessage("§cYou already have the maximum of " + HomesGUI.MAX_HOMES + " homes.");
            player.showDialog(HomesDialogs.mainMenu(player));
            return;
        }

        Location location = player.getLocation();
        String name = "Home " + slot;

        HomeManager.setHome(player, slot, name, location);
        player.playSound(location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        player.sendMessage("§aHome §6" + name + " §aset at your location!");
        player.showDialog(HomesDialogs.homeDetail(player, slot));
    }

    private static int parseTrailingInt(String value, String prefix) {
        return Integer.parseInt(value.substring(prefix.length()));
    }

    private static String readText(DialogResponseView response, String inputKey) {
        return response == null ? null : response.getText(inputKey);
    }
}
