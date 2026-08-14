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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds every Dialog screen for the plugin, using Paper's Dialog API
 * (io.papermc.paper.dialog). This API is relatively new, so some method
 * names below are a best effort and may need small fixes for your exact
 * Paper build - if the project fails to compile, paste the error and we
 * fix it together.
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
                buttons.add(button(home.name, materialOf(home.icon),
                        DialogAction.customClick(key("home_detail_" + i))));
            } else {
                buttons.add(button("New Home", Material.OAK_SIGN,
                        DialogAction.customClick(key("new_home_" + i))));
            }
        }

        Dialog dialog = Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Homes"))
                        .build())
                .type(DialogType.multiAction(buttons, null, COLUMNS)));
        return dialog;
    }

    public static Dialog homeDetail(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        String title = home != null ? home.name : "Home " + slot;

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Teleport", Material.ENDER_PEARL, DialogAction.customClick(key("teleport_" + slot))));
        buttons.add(button("Change Icon", Material.PAINTING, DialogAction.customClick(key("icon_open_" + slot))));
        buttons.add(button("Rename", Material.NAME_TAG, DialogAction.customClick(key("rename_open_" + slot))));
        buttons.add(redButton("Delete", Material.BARRIER, DialogAction.customClick(key("delete_open_" + slot))));

        ActionButton back = button("Back", Material.ARROW, DialogAction.customClick(key("back_main")));

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text(title)).build())
                .type(DialogType.multiAction(buttons, back, 2)));
    }

    public static Dialog renameDialog(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        String current = home != null ? home.name : "Home " + slot;

        DialogInput nameInput = DialogInput.text("new_name", Component.text("New Name"))
                .initial(current)
                .build();

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Save", Material.LIME_DYE, DialogAction.customClick(key("rename_save_" + slot))));
        buttons.add(button("Cancel", Material.GRAY_DYE, DialogAction.customClick(key("home_detail_" + slot))));

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Rename"))
                        .inputs(List.of(nameInput))
                        .build())
                .type(DialogType.multiAction(buttons, null, 2)));
    }

    public static Dialog deleteConfirm(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        String name = home != null ? home.name : "this home";

        ActionButton cancel = button("Cancel", Material.LIME_DYE, DialogAction.customClick(key("home_detail_" + slot)));
        ActionButton delete = redButton("Delete", Material.BARRIER, DialogAction.customClick(key("delete_confirm_" + slot)));

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Delete " + name + "?")).build())
                .type(DialogType.confirmation(delete, cancel)));
    }

    public static Dialog iconPicker(Player player, int slot, String search) {
        DialogInput searchInput = DialogInput.text("icon_search", Component.text("Search"))
                .initial(search == null ? "" : search)
                .build();

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Search", Material.COMPASS, DialogAction.customClick(key("icon_search_" + slot))));
        buttons.add(button("Back", Material.ARROW, DialogAction.customClick(key("home_detail_" + slot))));

        if (search == null || search.isBlank()) {
            for (String matName : DEFAULT_ICONS) {
                buttons.add(button(prettyName(matName), materialOf(matName),
                        DialogAction.customClick(key("icon_pick_" + slot + "_" + matName))));
            }
        } else {
            String needle = search.toLowerCase(Locale.ROOT);
            int found = 0;
            for (Material m : Material.values()) {
                if (!m.isItem() || m.isLegacy()) continue;
                if (!m.name().toLowerCase(Locale.ROOT).contains(needle)) continue;
                buttons.add(button(prettyName(m.name()), m,
                        DialogAction.customClick(key("icon_pick_" + slot + "_" + m.name()))));
                found++;
                if (found >= 150) break; // safety cap
            }
        }

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Choose Icon"))
                        .inputs(List.of(searchInput))
                        .build())
                .type(DialogType.multiAction(buttons, null, COLUMNS)));
    }

    // --- helpers ---

    private static Key key(String value) {
        return Key.key("homesgui", value.toLowerCase(Locale.ROOT));
    }

    private static Material materialOf(String name) {
        try {
            Material m = Material.matchMaterial(name);
            return m != null ? m : Material.OAK_SIGN;
        } catch (Exception e) {
            return Material.OAK_SIGN;
        }
    }

    private static String prettyName(String materialName) {
        String[] parts = materialName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase(Locale.ROOT)).append(' ');
        }
        return sb.toString().trim();
    }

    private static ActionButton button(String label, Material icon, DialogAction action) {
        return ActionButton.builder(Component.text(label))
                .icon(new ItemStack(icon))
                .action(action)
                .build();
    }

    private static ActionButton redButton(String label, Material icon, DialogAction action) {
        return ActionButton.builder(Component.text(label, NamedTextColor.RED))
                .icon(new ItemStack(icon))
                .action(action)
                .build();
    }
}
