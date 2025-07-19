package com.sebxstt.functions.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.sebxstt.helpers.GroupPermissions;
import com.sebxstt.instances.WarpPoint;
import com.sebxstt.instances.enums.PlayerTypeGroup;
import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.functions.utils.Lib;
import com.sebxstt.instances.PlayerConfig;
import com.sebxstt.instances.PlayersGroup;
import com.sebxstt.instances.StorageTeam;
import com.sebxstt.providers.PlayerProvider;
import com.sebxstt.providers.PluginProvider;
import com.sebxstt.serialize.data.PlayerConfigData;
import com.sebxstt.serialize.data.PlayerGroupData;
import com.sebxstt.serialize.data.WarpPointData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static com.sebxstt.index.mainData;
import static com.sebxstt.index.mm;
import static com.sebxstt.providers.DataStoreProvider.DS;

public class FunctionGroup {
    public static void ChangePostGroup(CommandContext<CommandSourceStack> ctx, String target, String cargo) {
        PlayerTypeGroup type;

        try {
            type = PlayerTypeGroup.valueOf(cargo.toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.getSource().getSender().sendMessage(Component.text("Cargo inválido: " + cargo));
            return;
        }

        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayerConfig senderConfig = Lib.getPlayerConfig(p);
        PlayerTypeGroup senderType = senderConfig.getPlayerType();

        var targetPlayer = Bukkit.getPlayerExact(target);
        if (targetPlayer == null) {
            p.sendMessage(mm.deserialize("<red><bold>Jugador no encontrado:</bold> " + target + "</red>"));
            return;
        }

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize("<red><bold>No tienes grupo activo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"));
            return;
        }

        if (InPlayer.name(grp.getOwner()).equals(targetPlayer.getName())) {
            p.sendMessage(mm.deserialize("<red><bold>Sin permiso:</bold> No puedes cambiar el cargo del dueño.</red>"));
            return;
        }

        if (targetPlayer.getName().equalsIgnoreCase(p.getName())) {
            p.sendMessage(mm.deserialize("<red><bold>Sin permiso:</bold> No puedes cambiarte el rango.</red>"));
            return;
        }

        if (!GroupPermissions.canManagerMembers(senderType)) {
            p.sendMessage(mm.deserialize("<red><bold>Sin permiso:</bold> No tienes el cargo suficiente para hacer esta acción.</red>"));
            return;
        }

        PlayerConfig targetConfig = Lib.getPlayerConfig(targetPlayer);
        PlayerTypeGroup previousType = targetConfig.getPlayerType();

        targetConfig.setPlayerType(type);

        p.sendMessage(mm.deserialize(
                "<green><bold>Cargo actualizado:</bold></green> <white>" + target + "</white> ahora es <yellow>" + type.name() + "</yellow>"
        ));

        targetPlayer.sendMessage(mm.deserialize(
                "<gold><bold>Tu cargo ha sido actualizado:</bold></gold> Ahora eres <yellow>" + type.name() + "</yellow> en el grupo <white>" + grp.getName() + "</white>"
        ));

        grp.getPlayers().forEach(player -> {
            player.sendMessage(mm.deserialize(
                    "<gray>[</gray><blue>Grupo</blue><gray>]</gray> <white>" + p.getName() +
                            "</white> <green>cambió el cargo de</green> <yellow>" + target +
                            "</yellow> <green>de</green> <red>" + previousType.name() +
                            "</red> <green>a</green> <aqua>" + type.name() + "</aqua>"
            ));
        });
    }

    public static void CreateGroup(CommandContext<CommandSourceStack> ctx, String name, String colorInput) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        if (Lib.FindPlayerInGroup(p.getName()) != null) {
            p.sendMessage(mm.deserialize("<gold><bold>Ya tienes un grupo activo.</bold></gold>"));
            return;
        }
        if (name == null || name.isBlank()) {
            p.sendMessage(mm.deserialize("<red><bold>Nombre inválido.</bold></red>"));
            return;
        }

        ChatColor color;
        try {
            color = ChatColor.valueOf(colorInput.toUpperCase());
            if (!color.isColor()) throw new IllegalArgumentException();
        } catch (Exception e) {
            p.sendMessage(mm.deserialize("<red>Color inválido. Usa: /group create <color> <nombre></red>"));
            Lib.ChooseGroupColor(p);
            return;
        }

        if (Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name) != null) {
            p.sendMessage(mm.deserialize("<red>Ya existe un team con ese nombre. Usa otro nombre.</red>"));
            return;
        }

        PlayersGroup grp = new PlayersGroup(name, p.getUniqueId(), color);
        mainData.playersGroups.add(grp);

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) {
            p.sendMessage(mm.deserialize("<red>No se encontro PlayerConfig para: </red> " + p.getName()));
            return;
        }

        pc.setCurrentGroup(grp.getId());
        pc.setPlayerType(PlayerTypeGroup.LEADER);

        PlayerProvider.setup(p.getUniqueId());
        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
        DS.create(PlayerGroupData.create(grp), PlayerGroupData.class);
        p.sendMessage(mm.deserialize(
                "<green><bold>Grupo creado:</bold> <white>" + name + "</white>\n" +
                        "<gold>Color:</gold> <" + color.name().toLowerCase() + "><bold>" + color.name().toLowerCase() + "</bold></" + color.name().toLowerCase() + ">"
        ));
    }

    public static void PreviewGroup(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No perteneces a ningún grupo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }

        grp.showInfo(p);
    }

    public static void LeaveGroup(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;
        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No tienes un grupo activo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }
        if (p.getName().equals(InPlayer.name(grp.getOwner()))) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No puedes salir</bold> Eres dueño del grupo</red>\n"
                            + "<gold>Usa el comando <bold>/group disband</bold> para disolver el grupo</gold>"
            ));
            return;
        }
        UUID playerRemovedUUID = grp.getMembers().stream().filter(plr -> InPlayer.name(plr).equals(p.getName())).findFirst().orElse(null);
        if(!(InPlayer.instance(playerRemovedUUID) instanceof Player playerRemoved)) return;
        grp.kickMember(playerRemoved);
        p.sendMessage(mm.deserialize("<green>Has salido de <gold><bold>" + grp.getName() + "</bold></gold></green>"));
        var owner = grp.getOwner();
        InPlayer.message(owner,
                "<yellow><italic>" + p.getName() + "</italic> ha salido de tu grupo</yellow>"
        );
    }

    public static void InviteGroup(CommandContext<CommandSourceStack> ctx, String target, String cargo) {
        PlayerTypeGroup type;

        try {
            type = PlayerTypeGroup.valueOf(cargo.toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.getSource().getSender().sendMessage(Component.text("Cargo inválido: " + cargo));
            return;
        }

        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;
        var invited = Bukkit.getPlayerExact(target);
        if (invited == null) {
            p.sendMessage(mm.deserialize("<red><bold>Jugador no encontrado:</bold></red> <white>" + target + "</white>"));
            return;
        }

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No tienes grupo activo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;

        if (!GroupPermissions.canManagerMembers(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red><bold>Sin permiso:</bold> Solo el dueño puede invitar miembros.</red>"));
            return;
        }

        if (grp.getMembers().stream().anyMatch(m -> InPlayer.name(m).equals(target)) || InPlayer.name(grp.getOwner()).equals(target)) {
            p.sendMessage(mm.deserialize("<yellow>" + target + " ya es miembro</yellow>"));
            return;
        }
        grp.sendInvitation(invited, p, type);
        p.sendMessage(mm.deserialize(
                "<green>Invitaste a <white><bold>" + target + "</bold></white> al grupo <gold><bold>" + grp.getName() + "</bold></gold></green>"
        ));

        invited.sendMessage(mm.deserialize(
                "<gradient:#00ff99:#0099ff><bold>¡Has recibido una invitación!</bold></gradient>\n" +
                        "<gold>Grupo:</gold> <white><bold>" + grp.getName() + "</bold></white>\n" +
                        "<gold>Invitado por:</gold> <white>" + p.getName() + "</white>\n" +
                        "<gray>Puedes ver y gestionar tus invitaciones usando:</gray> <aqua>/invitations aceptar|rechazar <tab></aqua>\n" +
                        "<gray>Ejemplo:</gray> <aqua>/invitations aceptar " + grp.getName() + "</aqua>"
        ));
    }

    public static void KickGroup(CommandContext<CommandSourceStack> ctx, String target) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;
        var kicked = Bukkit.getPlayerExact(target);
        if (kicked == null) {
            p.sendMessage(mm.deserialize("<red><bold>Jugador no encontrado:</bold> " + target + "</red>"));
            return;
        }

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No tienes un grupo activo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;

        if (!GroupPermissions.canManagerMembers(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red><bold>Sin permiso:</bold> Solo el dueño puede sacar miembros.</red>"));
            return;
        }

        if (InPlayer.name(grp.getOwner()).equals(target)) {
            p.sendMessage(mm.deserialize("<yellow><bold>No puedes expulsar al dueño del grupo.</bold></yellow>"));
            return;
        }

        var members = grp.getMembers() != null ? grp.getMembers() : new ArrayList<UUID>();
        if (members.stream().noneMatch(m -> InPlayer.name(m).equals(target))) {
            p.sendMessage(mm.deserialize("<yellow>" + target + " no es miembro del grupo.</yellow>"));
            return;
        }

        grp.kickMember(kicked);
        p.sendMessage(mm.deserialize(
                "<green>Expulsaste a <white><bold>" + target + "</bold></white> de <gold><bold>" + grp.getName() + "</bold></gold>.</green>"
        ));

        kicked.sendMessage(mm.deserialize(
                "<red><bold>Has sido expulsado del grupo:</bold> <white>" + grp.getName() + "</white>\n" +
                        "<gold>Por:</gold> <white>" + p.getName() + "</white>"
        ));
    }

    public static void DisbandGroup(CommandContext<CommandSourceStack> ctx) {
        var sender = ctx.getSource().getSender();
        if (!(sender instanceof Player p)) return;
        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }
        if (!p.getName().equals(InPlayer.name(grp.getOwner()))) {
            p.sendMessage(mm.deserialize("<red>Solo el dueño del grupo puede disolverlo.</red>"));
            return;
        }
        grp.dissolve();
        p.sendMessage(mm.deserialize("<green>Grupo <white><bold>" + grp.getName() + "</bold></white> disuelto correctamente.</green>"));
    }

    public static void ShowStorage(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No perteneces a ningún grupo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }

        StorageTeam storage = grp.getStorage();
        storage.open(p);
    }

    public static void ChatStateGroup(CommandContext<CommandSourceStack> ctx, String state) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        boolean isMatch = Arrays.stream(PluginProvider.optionsStates)
                .anyMatch(op -> op.equalsIgnoreCase(state));
        if (!isMatch) {
            p.sendMessage(mm.deserialize(
                    "<red>Opción inválida.</red>\n" +
                            "<gray>Usa: </gray><gold>/group chat on|off</gold>"
            ));
            return;
        }

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp == null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>No perteneces a ningún grupo.</bold> Usa <yellow>/group create <color> <nombre></yellow>"
            ));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;

        boolean enable = state.equalsIgnoreCase("ON");
        pc.setChatEnabledGroup(enable);
        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);

        if (enable) {
            p.sendMessage(mm.deserialize("<green>Chat de grupo <bold>activado</bold>.</green>"));
        } else {
            p.sendMessage(mm.deserialize("<yellow>Chat de grupo <bold>desactivado</bold>.</yellow>"));
        }
    }

    public static void SetWarp(CommandContext<CommandSourceStack> ctx, String name) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo. Usa <yellow>/group create <color> <nombre></yellow></red>"));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null || !GroupPermissions.canManageWarps(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red>No tienes permiso para eliminar warps.</red>"));
            return;
        }

        if (pg.getWarpPoints().stream().anyMatch(wp -> wp.name.equalsIgnoreCase(name))) {
            p.sendMessage(mm.deserialize("<yellow>El warp <white>" + name + "</white> ya existe en tu grupo.</yellow>"));
            return;
        }

        WarpPoint wp = new WarpPoint(name, p.getUniqueId(), pg.id, p.getLocation());
        pg.getWarpPoints().add(wp);

        p.sendMessage(mm.deserialize("<green>Warp <white>" + name + "</white> creado exitosamente.</green>"));
        DS.edit("id", pg.id.toString(), PlayerGroupData.create(pg), PlayerGroupData.class);

        pg.getPlayers().forEach(member -> {
            if (!member.equals(p)) {
                member.sendMessage(mm.deserialize("<gray>[Grupo]</gray> <white>" + p.getName() + "</white> creó un nuevo warp: <aqua>" + name + "</aqua>"));
            }
        });
    }

    public static void DeleteWarp(CommandContext<CommandSourceStack> ctx, String warpName) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null || !GroupPermissions.canManageWarps(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red>No tienes permiso para eliminar warps.</red>"));
            return;
        }

        WarpPoint wp = pg.getWarpPoints().stream()
                .filter(w -> w.name.equalsIgnoreCase(warpName))
                .findFirst()
                .orElse(null);

        if (wp == null) {
            p.sendMessage(mm.deserialize("<yellow>No existe un warp con el nombre <white>" + warpName + "</white>.</yellow>"));
            return;
        }

        pg.getWarpPoints().remove(wp);

        p.sendMessage(mm.deserialize("<green>Warp <white>" + warpName + "</white> eliminado correctamente.</green>"));
        DS.edit("id", pg.id.toString(), PlayerGroupData.create(pg), PlayerGroupData.class);

        pg.getPlayers().forEach(member -> {
            if (!member.equals(p)) {
                member.sendMessage(mm.deserialize("<gray>[Grupo] <white>" + p.getName() + "</white> eliminó el warp <red>" + warpName + "</red>"));
            }
        });
    }

    public static void WarpMember(CommandContext<CommandSourceStack> ctx, String warpName) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null || !GroupPermissions.canUseWarps(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red>No tienes permiso para usar warps.</red>"));
            return;
        }

        WarpPoint wp = pg.getWarpPoints().stream().filter(wpp -> wpp.name.equalsIgnoreCase(warpName)).findFirst().orElse(null);
        if (wp == null) {
            p.sendMessage(mm.deserialize("<yellow>El warp <white>" + warpName + "</white> no existe.</yellow>"));
            return;
        }

        wp.teleport(p.getUniqueId());

        p.sendMessage(mm.deserialize("<green>Has sido teletransportado al warp <aqua>" + warpName + "</aqua>.</green>"));
    }

    public static void WarpAll(CommandContext<CommandSourceStack> ctx, String warpName) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null || !GroupPermissions.canManageWarps(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red>No tienes permiso para usar warps entre los miembros.</red>"));
            return;
        }

        WarpPoint wp = pg.getWarpPoints().stream().filter(wpp -> wpp.name.equalsIgnoreCase(warpName)).findFirst().orElse(null);
        if (wp == null) {
            p.sendMessage(mm.deserialize("<yellow>El warp <white>" + warpName + "</white> no existe.</yellow>"));
            return;
        }

        for (UUID member : pg.getMembers()) {
            Player memberPlayer = InPlayer.instance(member);
            if (memberPlayer != null) {
                wp.teleport(member);
                memberPlayer.sendMessage(mm.deserialize("<gray>Has sido teletransportado al warp <aqua>" + warpName + "</aqua> por <white>" + p.getName() + "</white>.</gray>"));
            }
        }

        p.sendMessage(mm.deserialize("<green>Has teletransportado a todos los miembros al warp <aqua>" + warpName + "</aqua>.</green>"));
    }

    public static void WarpPost(CommandContext<CommandSourceStack> ctx, String post, String warpName) {
        PlayerTypeGroup type;
        try {
            type = PlayerTypeGroup.valueOf(post.toUpperCase());
        } catch (IllegalArgumentException e) {
            ctx.getSource().getSender().sendMessage(mm.deserialize("<red>Cargo inválido: <white>" + post + "</white></red>"));
            return;
        }

        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null || !GroupPermissions.canManageWarps(pc.getPlayerType())) {
            p.sendMessage(mm.deserialize("<red>No tienes permiso para usar warps entre los miembros.</red>"));
            return;
        }

        WarpPoint wp = pg.getWarpPoints().stream().filter(wpp -> wpp.name.equalsIgnoreCase(warpName)).findFirst().orElse(null);
        if (wp == null) {
            p.sendMessage(mm.deserialize("<yellow>El warp <white>" + warpName + "</white> no existe.</yellow>"));
            return;
        }

        ArrayList<Player> members = pg.getMembers().stream()
                .map(mm -> Lib.getPlayerConfig(InPlayer.instance(mm)))
                .filter(pcc -> pcc.getPlayerType().equals(type))
                .map(pcc -> InPlayer.instance(pcc.player))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if (members.isEmpty()) {
            p.sendMessage(mm.deserialize("<yellow>No hay miembros con el cargo <white>" + post.toUpperCase() + "</white> en el grupo.</yellow>"));
            return;
        }

        for (Player member : members) {
            wp.teleport(member.getUniqueId());
            member.sendMessage(mm.deserialize("<gray>Has sido teletransportado al warp <aqua>" + warpName + "</aqua> por <white>" + p.getName() + "</white>.</gray>"));
        }

        p.sendMessage(mm.deserialize("<green>Has teletransportado a los <white>" + post.toUpperCase() + "</white> al warp <aqua>" + warpName + "</aqua>.</green>"));
    }

    public static void ListWarps(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        List<WarpPoint> warps = pg.getWarpPoints();
        if (warps.isEmpty()) {
            p.sendMessage(mm.deserialize("<gray>Tu grupo no tiene warps creados.</gray>"));
            return;
        }

        p.sendMessage(mm.deserialize("<aqua><bold>Lista de Warps del Grupo:</bold></aqua>"));
        for (WarpPoint wp : warps) {
            String line = "<white>• <aqua>" + wp.name + "</aqua> <gray>(" +
                    wp.location.getWorld().getName() + " - " +
                    wp.location.getBlockX() + ", " +
                    wp.location.getBlockY() + ", " +
                    wp.location.getBlockZ() + ")</gray>";
            p.sendMessage(mm.deserialize(line));
        }
    }

    public static void ShowMenu(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayersGroup pg = Lib.FindPlayerInGroup(p.getName());
        if (pg == null) {
            p.sendMessage(mm.deserialize("<red>No perteneces a ningún grupo.</red>"));
            return;
        }

        pg.menu.open(p);
    }
}
