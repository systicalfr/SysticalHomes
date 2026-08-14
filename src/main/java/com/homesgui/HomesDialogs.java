package com.homesgui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds every Dialog screen for the plugin, using Paper's Dialog API
 * (io.papermc.paper.dialog). Note: on this Paper build, ActionButton has
 * no per-button icon support, so buttons are text-only.
 */
public class HomesDialogs {

    private static final int COLUMNS = 4;

    // Small curated set shown before the player searches for something specific
    private static final String[] DEFAULT_ICONS = {
            "OAK_SIGN", "WHITE_BED", "COMPASS", "MAP", "CHEST", "BELL",
            "NETHER_STAR", "DIAMOND", "EMERALD", "TORCH", "ANVIL", "BARREL"
    };

    public static Dialog mainMenu(Player player) {
        List<ActionButton> buttons = new ArrayList<>();

        for (int i = 1; i <= HomesGUI.MAX_HOMES; i++) {
            HomeManager.Home home = HomeManager.getHomeBySlot(player, i);
            if (home != null) {
                buttons.add(button(home.name, action("home_detail_" + i)));
            } else {
                buttons.add(button("New Home", action("new_home_" + i)));
            }
        }

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Homes"))
                        .build())
                .type(DialogType.multiAction(buttons, null, COLUMNS)));
    }

    public static Dialog homeDetail(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        String title = home != null ? home.name : "Home " + slot;

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Teleport", action("teleport_" + slot)));
        buttons.add(button("Change Icon", action("icon_open_" + slot)));
        buttons.add(button("Rename",
