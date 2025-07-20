package com.sebxstt.instances;

import com.sebxstt.managers.menus.InfoPageManager;
import com.sebxstt.managers.menus.WarpsPageManager;
import com.sebxstt.nextinventory.NextInventory;
import com.sebxstt.nextinventory.enums.InventorySize;
import com.sebxstt.nextinventory.enums.InventoryType;
import com.sebxstt.nextinventory.instances.NextItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupMenu {
    public UUID id;
    public String title;
    public NextInventory NextGUI;
    public UUID group;

    // Buttons
    public NextItem InfoButton;
    public NextItem WarpsButton;
    public NextItem LeaveButton;
    public NextItem DisbandButton;
    public NextItem KickButton;
    public NextItem RoleButton;

    public NextItem BackButton;

    // Buttons Iterable
    public ArrayList<NextItem> Buttons = new ArrayList<>();

    // Managers
    public WarpsPageManager warpsPageManager;

    public GroupMenu(String title, UUID group) {
        this.title = title;
        this.id = UUID.randomUUID();
        this.group = group;

        this.NextGUI = NextInventory.builder()
                .title(title)
                .type(InventoryType.NORMAL)
                .size(InventorySize.MEDIUM)
                .pages(6);

        this.init();
    }

    private void init() {
        this.InfoButton = NextGUI.CustomItem("Info Group", "", Material.GREEN_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.GREEN_STAINED_GLASS, Material.GREEN_STAINED_GLASS_PANE, 5L);

        this.WarpsButton = NextGUI.CustomItem("Warps Group", "", Material.BLUE_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS_PANE, 5L);

        this.DisbandButton = NextGUI.CustomItem("Disband Group", "", Material.RED_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE, 5L);

        this.LeaveButton = NextGUI.CustomItem("Leave Group", "", Material.ORANGE_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.ORANGE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS_PANE, 5L);

        this.KickButton = NextGUI.CustomItem("Kick Group", "", Material.YELLOW_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE, 5L);

        this.RoleButton = NextGUI.CustomItem("Role Group", "", Material.PURPLE_STAINED_GLASS_PANE, 0)
                .draggable(false)
                .button(true)
                .swap(Material.PURPLE_STAINED_GLASS, Material.PURPLE_STAINED_GLASS_PANE, 5L);

        this.BackButton = NextGUI.CustomItem("Return Home", "", Material.BLACK_WOOL, 20)
                .draggable(false)
                .button(true);

        this.Buttons.addAll(List.of(InfoButton, WarpsButton, DisbandButton, LeaveButton, KickButton, RoleButton));

        int size = NextGUI.getSize().getContentSlots();
        int rows = size / 7;
        int[] pattern = new int[]{1,3,5};
        ArrayList<NextItem> items = new ArrayList<>(this.Buttons);

        for (int i = 0; i < rows; i++) {
            if ((i + 1) % 2 == 0) continue;
            for (int patternIndex : pattern) {
                if (items.isEmpty() || items.getFirst() == null) continue;
                int index = i * 7 + patternIndex;
                items.getFirst().insert().move(index);
                items.remove(items.getFirst());
            }
        }

        this.warpsPageManager = new WarpsPageManager(this.group, NextGUI);

        this.execute();
    }

    private void execute() {
        this.InfoButton.onClick(event -> {
            Player player = event.getPlayer();
            InfoPageManager.RenderInfoPage(this.group, NextGUI);
            NextGUI.current(2);
            this.BackButton.move(20, 2);
        });

        this.WarpsButton.onClick(event -> {
            Player player = event.getPlayer();
            this.warpsPageManager.RenderWarpsPage();
            NextGUI.current(3);
            this.BackButton.move(20, 3);
            System.out.println("[GroupMenu] Show Warps Group to " + player.getName());
        });

        this.DisbandButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.current(4);
            this.BackButton.move(20, 4);
            System.out.println("[GroupMenu] Disband Group to " + player.getName());
        });

        this.LeaveButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.current(5);
            this.BackButton.move(20, 5);
            System.out.println("[GroupMenu] Leave Group to " + player.getName());
        });

        this.KickButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.current(6);
            this.BackButton.move(20, 6);
            System.out.println("[GroupMenu] Kick Group to " + player.getName());
        });

        this.RoleButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.current(7);
            this.BackButton.move(20, 7);
            System.out.println("[GroupMenu] Role Group to " + player.getName());
        });

        this.BackButton.onClick(event -> {
            Player player = event.getPlayer();
            NextGUI.current(1);
        });
    }
    public void open(Player player) {
        this.NextGUI.open(player.getUniqueId());
    }

    public void close(Player player) {
        this.NextGUI.close(player.getUniqueId());
    }

    public void setGroup(UUID group) {
        this.group = group;
        this.warpsPageManager.setGroup(group);
    }
}
