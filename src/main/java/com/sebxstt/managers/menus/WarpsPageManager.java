package com.sebxstt.managers.menus;

import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.instances.PlayersGroup;
import com.sebxstt.instances.WarpPoint;
import com.sebxstt.nextinventory.InventoryHelper;
import com.sebxstt.nextinventory.NextInventory;
import com.sebxstt.nextinventory.enums.InventorySize;
import com.sebxstt.nextinventory.enums.InventoryType;
import com.sebxstt.nextinventory.instances.NextItem;
import com.sebxstt.serialize.data.PlayerGroupData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

import static com.sebxstt.index.mm;
import static com.sebxstt.providers.DataStoreProvider.DS;

public class WarpsPageManager {
    private UUID group;
    private NextInventory MainGUI;

    private NextInventory NextGUI;

    // Buttons
    private NextItem DeleteButton;
    private NextItem ListButton;
    private NextItem TeleportButton;
    private NextItem AllButton;
    private NextItem PostButton;
    private NextItem BackButton;

    public WarpsPageManager(UUID group, NextInventory MainGUI) {
        this.group = group;
        this.MainGUI = MainGUI;
        this.NextGUI = NextInventory.builder()
                .title("Manager Warps")
                .size(InventorySize.MEDIUM)
                .type(InventoryType.NORMAL)
                .pages(5);

        this.init();
    }

    private void init() {
        this.DeleteButton = MainGUI.CustomItem("<red>Eliminar Warp", "<gray>Elimina un warp existente.", Material.BARRIER, 1)
                .insert(3).button(true).draggable(false);

        this.ListButton = MainGUI.CustomItem("<aqua>Listar Warps", "<gray>Muestra todos los warps del grupo.", Material.BOOK, 2)
                .insert(3).button(true).draggable(false);

        this.TeleportButton = MainGUI.CustomItem("<yellow>Teleport Warp", "<gray>Teletransportarse a un warp.", Material.ENDER_PEARL, 3)
                .insert(3).button(true).draggable(false);

        this.AllButton = MainGUI.CustomItem("<gold>Warp Todos", "<gray>Teletransporta a todos al warp.", Material.NETHER_STAR, 4)
                .insert(3).button(true).draggable(false);

        this.PostButton = MainGUI.CustomItem("<light_purple>Post Warp", "<gray>Teletransporta a los miembros con <dark_gray><i><cargo></i></dark_gray>.", Material.FIREWORK_ROCKET, 5)
                .insert(3).button(true).draggable(false);

        this.BackButton = NextGUI.CustomItem("Return Menu", "", Material.BLACK_WOOL, 20)
                .insert(1)
                .draggable(false)
                .button(true);
    }

    public void RenderWarpsPage() {
        this.execute();
    }

    private void execute() {
        PlayersGroup group = InPlayer.group(this.group);
        if (group == null) {
            System.out.println("[GroupMenu] [ERROR] Group not found for menu ID: " + this.group);
            return;
        }

        this.DeleteButton.onClick(event -> {
            Player player = event.getPlayer();

            for (NextItem item : new ArrayList<>(NextGUI.getItems())) {
                if (item.equals(this.BackButton)) continue;
                item.remove();
            }

            for (int i = 0; i < group.getWarpPoints().size(); i++) {
                WarpPoint wp = group.getWarpPoints().get(i);
                NextItem option = NextGUI.CustomItem("Delete Warp: " + wp.name, "Eliminar el warp", Material.RED_WOOL, i)
                        .draggable(false)
                        .button(true)
                        .insert(1);

                option.onClick(eventOption -> {
                    group.getWarpPoints().remove(wp);

                    player.sendMessage(mm.deserialize("<green>Warp <white>" + wp.name + "</white> eliminado correctamente.</green>"));
                    DS.edit("id", group.id.toString(), PlayerGroupData.create(group), PlayerGroupData.class);

                    group.getPlayers().forEach(member -> {
                        if (!member.equals(player)) {
                            member.sendMessage(mm.deserialize("<gray>[Grupo] <white>" + player.getName() + "</white> eliminó el warp <red>" + wp.name + "</red>"));
                        }
                    });
                    option.remove();
                });
            }

            MainGUI.close(player.getUniqueId());
            NextGUI.open(player.getUniqueId());
            NextGUI.current(1);
            this.BackButton.move(20, 1);
        });

        this.ListButton.onClick(event -> {
            // Acción para listar warps
            this.BackButton.move(20, 2);
        });

        this.TeleportButton.onClick(event -> {
            // Acción para tp
            this.BackButton.move(20, 3);
        });

        this.AllButton.onClick(event -> {
            // Acción para warp all
            this.BackButton.move(20, 4);
        });

        this.PostButton.onClick(event -> {
            // Acción para warp post
            this.BackButton.move(20, 5);
        });

        this.BackButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.close(player.getUniqueId());
            MainGUI.open(player.getUniqueId());
            MainGUI.current(1);
        });
    }

    public void setGroup(UUID group) {
        this.group = group;
    }
}
