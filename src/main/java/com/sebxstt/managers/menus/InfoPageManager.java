package com.sebxstt.managers.menus;

import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.instances.PlayersGroup;
import com.sebxstt.nextinventory.NextInventory;
import org.bukkit.Material;

import java.util.UUID;

public class InfoPageManager {
    public static void RenderInfoPage(UUID groupID, NextInventory NextGUI) {
        PlayersGroup group = InPlayer.group(groupID);
        if (group == null) {
            System.out.println("[GroupMenu] [ERROR] Group not found for menu ID: " + groupID);
            return;
        }

        NextGUI.CustomItem("Owner", InPlayer.name(group.getOwner()), Material.PLAYER_HEAD, 0)
                .insert(2).head(InPlayer.name(group.getOwner())).draggable(false);

        NextGUI.CustomItem("Level", "Level: " + group.level, Material.EXPERIENCE_BOTTLE, 1)
                .insert(2).draggable(false);

        String colorName = group.getColor().name().toLowerCase();
        NextGUI.CustomItem("Group Color", colorName, Material.valueOf(group.getColor().name() + "_WOOL"), 2)
                .insert(2).draggable(false);

        String members = group.getMembers().isEmpty()
                ? "No members"
                : group.getMembers().stream()
                .map(InPlayer::name).sorted()
                .reduce((a, b) -> a + ", " + b).orElse("");

        NextGUI.CustomItem("Members", members, Material.BOOK, 7)
                .insert(2).draggable(false);

        String allies = group.allies.isEmpty()
                ? "No allies"
                : group.allies.stream()
                .map(PlayersGroup::getName).sorted()
                .reduce((a, b) -> a + ", " + b).orElse("");

        NextGUI.CustomItem("Allies", allies, Material.EMERALD, 8)
                .insert(2).draggable(false);

        String enemies = group.enemies.isEmpty()
                ? "No enemies"
                : group.enemies.stream()
                .map(PlayersGroup::getName).sorted()
                .reduce((a, b) -> a + ", " + b).orElse("");

        NextGUI.CustomItem("Enemies", enemies, Material.BLAZE_POWDER, 9)
                .insert(2).draggable(false);
    }
}
