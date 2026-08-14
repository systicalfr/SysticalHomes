package com.homesgui;

import io.papermc.paper.connection.PlayerGameConnection;
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
        if (!(event.getCommonConnection() instanceof PlayerGameConnection conn)) return;
        Player player = conn.getPlayer();

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
