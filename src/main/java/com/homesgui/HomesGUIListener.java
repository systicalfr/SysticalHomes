package com.homesgui;

import io.papermc.paper.event.player.PlayerCustomClickEvent;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles every button click from the Dialog-based /home menu.
 * Each button's key encodes an action and a home slot, e.g. "home_detail_1",
 * "delete_confirm_3", "icon_pick_2_DIAMOND".
 */
public class HomesGUIListener implements Listener {

    @EventHandler
    public void onCustomClick(PlayerCustomClickEvent event) {
        Key id = event.getIdentifier();
        if (!"homesgui".equals(id.namespace())) return;
        Player player = event.getPlayer();
        if (player == null) return;

        String value = id.value();

        if (value.equals("back_main")) {
            player.showDialog(HomesDialogs.mainMenu(player));
            return;
        }

        if (value.startsWith("new_home_")) {
            int slot = parseTrailingInt(value, "new_home_");
            createHome(player, slot);
            return;
        }

        if (value.startsWith("home_detail_")) {
            int slot = parseTrailingInt(value, "home_detail_");
            player.showDialog(HomesDialogs.homeDetail(player, slot));
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
            int slot = parseTrailingInt(value, "rename_open_");
            player.showDialog(HomesDialogs.renameDialog(player, slot));
            return;
        }

        if (value.startsWith("rename_save_")) {
            int slot = parseTrailingInt(value, "rename_save_");
            String newName = readText(event, "new_name");
            if (newName != null && !newName.isBlank()) {
                HomeManager.renameHome(player, slot, newName.trim());
            }
            player.showDialog(HomesDialogs.homeDetail(player, slot));
            return;
        }

        if (value.startsWith("icon_open_")) {
            int slot = parseTrailingInt(value, "icon_open_");
            player.showDialog(HomesDialogs.iconPicker(player, slot, null));
            return;
        }

        if (value.startsWith("icon_search_")) {
            int slot = parseTrailingInt(value, "icon_search_");
            String search = readText(event, "icon_search");
            player.showDialog(HomesDialogs.iconPicker(player, slot, search));
            return;
        }

        if (value.startsWith("icon_pick_")) {
            // format: icon_pick_<slot>_<MATERIAL_NAME>
            String rest = value.substring("icon_pick_".length());
            int underscore = rest.indexOf('_');
            int slot = Integer.parseInt(rest.substring(0, underscore));
            String material = rest.substring(underscore + 1).toUpperCase();
            HomeManager.setIcon(player, slot, material);
            player.showDialog(HomesDialogs.homeDetail(player, slot));
            return;
        }

        if (value.startsWith("delete_open_")) {
            int slot = parseTrailingInt(value, "delete_open_");
            player.showDialog(HomesDialogs.deleteConfirm(player, slot));
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

    private void createHome(Player player, int slot) {
        if (HomeManager.getHomeBySlot(player, slot) == null && HomeManager.countHomes(player) >= HomesGUI.MAX_HOMES) {
            player.sendMessage("§cYou already have the maximum of " + HomesGUI.MAX_HOMES + " homes.");
            player.showDialog(HomesDialogs.mainMenu(player));
            return;
        }
        Location loc = player.getLocation();
        String name = "Home " + slot;
        HomeManager.setHome(player, slot, name, loc);
        player.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
        player.sendMessage("§aHome §6" + name + " §aset at your location!");
        player.showDialog(HomesDialogs.homeDetail(player, slot));
    }

    private int parseTrailingInt(String value, String prefix) {
        return Integer.parseInt(value.substring(prefix.length()));
    }

    /**
     * Reads a submitted text input's value from the dialog response.
     * Exact method name/shape may need adjusting for your Paper build.
     */
    private String readText(PlayerCustomClickEvent event, String inputKey) {
        try {
            var response = event.getDialogResponseView();
            if (response == null) return null;
            return response.getText(inputKey);
        } catch (Exception e) {
            return null;
        }
    }
}

