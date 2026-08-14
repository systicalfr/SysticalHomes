package com.homesgui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
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
 * (io.papermc.paper.dialog). Note: on this Paper build, ActionButton has
 * no per-button icon support, so buttons are text-only.
 */
public class HomesDialogs {

    private static final int COLUMNS = 4;

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
        buttons.add(button("Rename", action("rename_open_" + slot)));
        buttons.add(redButton("Delete", action("delete_open_" + slot)));

        ActionButton back = button("Back", action("back_main"));

        DialogBase.Builder base = DialogBase.builder(Component.text(title));
        if (home != null) {
            // FIXED: Added .build() so it resolves as a DialogBody instead of a Builder!
            base.body(List.of(DialogBody.item(new ItemStack(materialOf(home.icon))).build()));
        }

        return Dialog.create(factory -> factory
                .empty()
                .base(base.build())
                .type(DialogType.multiAction(buttons, back, 2)));
    }

    public static Dialog renameDialog(Player player, int slot) {
        HomeManager.Home home = HomeManager.getHomeBySlot(player, slot);
        String current = home != null ? home.name : "Home " + slot;

        DialogInput nameInput = DialogInput.text("new_name", Component.text("New Name"))
                .initial(current)
                .build();

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Save", action("rename_save_" + slot)));
        buttons.add(button("Cancel", action("home_detail_" + slot)));

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

        ActionButton cancel = button("Cancel", action("home_detail_" + slot));
        ActionButton delete = redButton("Delete", action("delete_confirm_" + slot));

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
        buttons.add(button("Search", action("icon_search_" + slot)));
        buttons.add(button("Back", action("home_detail_" + slot)));

        String needle = (search == null) ? "" : search.toLowerCase(Locale.ROOT);
        for (Material m : Material.values()) {
            if (!m.isItem() || m.isLegacy()) continue;
            if (m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR) continue;
            if (!needle.isEmpty() && !m.name().toLowerCase(Locale.ROOT).contains(needle)) continue;
            buttons.add(button(prettyName(m.name()), action("icon_pick_" + slot + "_" + m.name())));
        }

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Choose Icon"))
                        .inputs(List.of(searchInput))
                        .build())
                .type(DialogType.multiAction(buttons, null, COLUMNS)));
    }

    // --- helpers ---

    private static DialogAction action(String value) {
        return DialogAction.customClick(Key.key("homesgui", value.toLowerCase(Locale.ROOT)), null);
    }

    private static Material materialOf(String name) {
        Material m = Material.matchMaterial(name == null ? "" : name);
        return m != null ? m : Material.CHEST;
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

    private static ActionButton button(String label, DialogAction action) {
        return ActionButton.builder(Component.text(label))
                .action(action)
                .build();
    }

    private static ActionButton redButton(String label, DialogAction action) {
        return ActionButton.builder(Component.text(label, NamedTextColor.RED))
                .action(action)
                .build();
    }
}
