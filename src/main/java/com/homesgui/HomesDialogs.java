package com.homesgui;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HomesDialogs {

    private static final int COLUMNS = 4;

    private HomesDialogs() {
    }

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
                .base(DialogBase.builder(Component.text("Homes")).build())
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

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text(title)).build())
                .type(DialogType.multiAction(
                        buttons,
                        button("Back", action("back_main")),
                        2
                )));
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

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Delete " + name + "?")).build())
                .type(DialogType.confirmation(
                        redButton("Delete", action("delete_confirm_" + slot)),
                        button("Cancel", action("home_detail_" + slot))
                )));
    }

    public static Dialog iconPicker(Player player, int slot, String search) {
        DialogInput searchInput = DialogInput.text("icon_search", Component.text("Search"))
                .initial(search == null ? "" : search)
                .build();

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(button("Search", action("icon_search_" + slot)));
        buttons.add(button("Back", action("home_detail_" + slot)));

        String needle = search == null ? "" : search.toLowerCase(Locale.ROOT);

        for (Material material : Material.values()) {
            if (!material.isItem() || material.isLegacy()) {
                continue;
            }

            if (!needle.isEmpty()
                    && !material.name().toLowerCase(Locale.ROOT).contains(needle)) {
                continue;
            }

            buttons.add(button(
                    prettyName(material.name()),
                    action("icon_pick_" + slot + "_" + material.name())
            ));
        }

        return Dialog.create(factory -> factory
                .empty()
                .base(DialogBase.builder(Component.text("Choose Icon"))
                        .inputs(List.of(searchInput))
                        .build())
                .type(DialogType.multiAction(buttons, null, COLUMNS)));
    }

    private static DialogAction action(String value) {
        return DialogAction.customClick((response, audience) -> {
            if (audience instanceof Player player) {
                HomesGUIListener.handle(player, value, response);
            }
        }, ClickCallback.Options.builder().uses(1).build());
    }

    private static String prettyName(String materialName) {
        String[] parts = materialName.split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase(Locale.ROOT))
                        .append(' ');
            }
        }

        return builder.toString().trim();
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
