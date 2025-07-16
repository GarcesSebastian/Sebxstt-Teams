package com.sebxstt.managers;

import com.sebxstt.nextinventory.NextInventory;
import com.sebxstt.nextinventory.enums.InventorySize;
import com.sebxstt.nextinventory.enums.InventoryType;
import com.sebxstt.nextinventory.instances.NextItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

import static com.sebxstt.nextinventory.InventoryHelper.contentIndex;

public class TeamGUI {
    public static final NextInventory NextGUI = NextInventory.builder()
            .title("Team Manager")
            .type(InventoryType.PAGINATION)
            .size(InventorySize.LARGE);

    public static void setup() {
        NextGUI.pages(4); // Create Inventory GUI with 4 pages

//        NextGUI.getBack().setMaterialType(Material.APPLE); // Change Material Type of Back Item in Pagination
//        NextGUI.getCurrent().setMaterialType(Material.DIAMOND); // Change Material Type of Current Item in Pagination
//        NextGUI.getNext().setMaterialType(Material.ACACIA_LEAVES); // Change Material Type of Next Item in Pagination

        NextItem StorageButton = NextGUI.CustomItem("Open Storage Team", "", Material.GREEN_STAINED_GLASS, 1)
                .button(true) // Is Button
                .swap(Material.DIAMOND, Material.EMERALD, 10L) // Swap between two materials
                .insert(); // Insert into inventory
        NextItem WarpsButton = NextGUI.CustomItem("Warps Team", "", Material.BLUE_STAINED_GLASS, 3)
                .button(true) // Is Button
                .cycle(List.of(
                        Material.AIR,
                        Material.BLUE_STAINED_GLASS_PANE,
                        Material.AIR,
                        Material.RED_STAINED_GLASS_PANE
                ), 10L) // Swap between some materials
                .insert(); // Insert into inventory
        NextItem ExitButton = NextGUI.CustomItem("Exit", "", Material.RED_STAINED_GLASS, 5)
                .button(true) // Is Button
                .insert(); // Insert into inventory

        StorageButton.onClick(event -> { // Callback when player clicked item
            int count = 0;
            for (Player plr : Bukkit.getOnlinePlayers()) {
                NextItem playerItem = NextGUI.CustomItem(plr.getName(), "Jugador del Equipo", Material.EMERALD, count)
                        .button(true)
                        .insert(2); // Create Custom Item to show member

                playerItem.onClick(eventClick -> {
                    playerItem.remove();
                });
                count++;
            }

            NextGUI.current(2); // Move to page index 2
        });

        WarpsButton.onClick(event -> { // Callback when player clicked item
            int count = 0;
            for (Player plr : Bukkit.getOnlinePlayers()) {
                NextItem warpItem = NextGUI.CustomItem(plr.getName(), "Jugador del Equipo", Material.DIAMOND, count)
                        .button(true)
                        .insert(3); // Create Custom Item to show member

                warpItem.onClick(eventClick -> {
                    Integer index = contentIndex(NextGUI, warpItem.getIndex()); // Get Index in slot allowed
                    warpItem.move(index + 1); // Move item to another index (index + 1)
                });

                count++;
            }

            NextGUI.current(3); // Move to page index 3
        });

        ExitButton.onClick(event -> { // Callback when player clicked item
            NextGUI.close(event.getPlayer().getUniqueId());
        });

        NextGUI.onBack(event -> { // Callback when player clicked back pagination
            event.abort(true); // Abort event, this cancels event default (back page)
        });

        NextGUI.onNext(event -> { // Callback when the player clicked next pagination
            event.abort(true); // Abort event, this cancels event default (next page)
        });
    }
}
