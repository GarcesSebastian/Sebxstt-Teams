package com.sebxstt.functions.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.sebxstt.nextinventory.enums.InventoryType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.functions.utils.Lib;
import com.sebxstt.instances.PlayerConfig;
import com.sebxstt.instances.PlayersGroup;
import com.sebxstt.instances.RequestGroup;
import com.sebxstt.nextinventory.NextInventory;
import com.sebxstt.nextinventory.NextInventoryProvider;
import com.sebxstt.providers.PluginProvider;
import com.sebxstt.serialize.data.PlayerConfigData;
import com.sebxstt.serialize.data.PlayerGroupData;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static com.sebxstt.index.*;
import static com.sebxstt.providers.DataStoreProvider.DS;

public class FunctionPlayer {
    public static void test(CommandContext<CommandSourceStack> ctx, String target) {
        CommandSender senderRaw = ctx.getSource().getSender();
        if(!(senderRaw instanceof Player p)) return;
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(target);

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;
        PlayersGroup pg = InPlayer.group(pc.getCurrentGroup());
        if (pg == null) return;
    }

    public static void npc(CommandContext<CommandSourceStack> ctx, String name) throws Exception {
        if (!(ctx.getSource().getSender() instanceof Player viewer)) return;
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel world = ((CraftWorld) viewer.getWorld()).getHandle();
        ServerPlayer npc = new ServerPlayer(server, world, profile, ClientInformation.createDefault());
        Location loc = viewer.getLocation();
        npc.setPos(loc.getX(), loc.getY(), loc.getZ());

        ServerGamePacketListenerImpl conn = ((CraftPlayer) viewer).getHandle().connection;
        server.getPlayerList().placeNewPlayer(conn.connection, npc, CommonListenerCookie.createInitial(profile, true));

        conn.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npc));
        conn.send(new ClientboundAddEntityPacket(npc, null));
        conn.send(new ClientboundSetEntityDataPacket(npc.getId(), null));
    }

    public static void ClearTeams(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        ArrayList<PlayersGroup> copy = new ArrayList<>(mainData.playersGroups);
        for (PlayersGroup group : copy) {
            group.dissolve();
        }

        Lib.clearOrphanNameTags();

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        for (Team team : new ArrayList<>(main.getTeams())) {
            team.unregister();
        }

        p.sendMessage(mm.deserialize("<green><bold>Todos los equipos han sido eliminados correctamente.</bold></green>"));
    }


    public static void ReturnPlayer(CommandContext<CommandSourceStack> ctx) {
        var sender = ctx.getSource().getSender();
        if (!(sender instanceof Player p)) return;

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;

        pc.useLastDeath();
        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
    }

    public static void PreviewPlayer(CommandContext<CommandSourceStack> ctx) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        PlayerConfig pc = Lib.getPlayerConfig(p);

        if (pc == null) return;

        pc.showInfo();
    }

    public static void RequestPlayer(CommandContext<CommandSourceStack> ctx, String name, String option) {
        var senderRaw = ctx.getSource().getSender();
        if (!(senderRaw instanceof Player p)) return;

        boolean isMatch = Arrays.stream(PluginProvider.optionsInvitations).anyMatch(op -> op.equalsIgnoreCase(option));
        if (!isMatch) {
            p.sendMessage(mm.deserialize(
                    "<red>Opción inválida.</red>\n" +
                            "<gray>Usa: </gray><gold>/group request <nombre> aceptar|rechazar</gold>"
            ));
            return;
        }

        PlayersGroup grp = Lib.FindPlayerInGroup(p.getName());
        if (grp != null) {
            p.sendMessage(mm.deserialize(
                    "<red><bold>Ya perteneces a un grupo activo.</bold></red>\n" +
                            "<gray>Si deseas unirte a otro grupo, primero debes salir con:</gray> <gold>/group leave</gold>"
            ));
            return;
        }

        PlayerConfig pc = Lib.getPlayerConfig(p);
        if (pc == null) return;

        RequestGroup requestGroupMatch = pc.getRequestGroup().stream()
                .filter(req -> InPlayer.group(req.getGroup()).getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);

        if (requestGroupMatch == null) {
            p.sendMessage(mm.deserialize(
                    "<red>No se encontró una invitación del grupo <white><bold>" + name + "</bold></white>.</red>\n" +
                            "<gray>Verifica que el nombre esté escrito correctamente.</gray>"
            ));
            return;
        }

        if (!pc.getInvited()) {
            p.sendMessage(mm.deserialize(
                    "<yellow>No tienes invitaciones activas.</yellow>\n" +
                            "<gray>Revisa con <gold>/group list</gold> para ver las disponibles.</gray>"
            ));
            return;
        }

        boolean confirm = option.equalsIgnoreCase("aceptar");
        if (!confirm) {
            pc.getRequestGroup().remove(requestGroupMatch);

            InPlayer.message(requestGroupMatch.invitator,
                    "<red><bold>Invitación rechazada.</bold></red>\n" +
                            "<gray>El jugador <aqua>" + p.getName() + "</aqua> ha rechazado la invitación al grupo <white>" + InPlayer.group(requestGroupMatch.getGroup()).getName() + "</white>.</gray>"
            );

            p.sendMessage(mm.deserialize(
                    "<red><bold>Invitación rechazada.</bold></red>\n" +
                            "<gray>Has rechazado la invitación al grupo <aqua>" + InPlayer.group(requestGroupMatch.getGroup()).getName() + "</aqua>.</gray>"
            ));
            return;
        }

        PlayersGroup groupInvited = InPlayer.group(requestGroupMatch.getGroup());
        groupInvited.addMember(p, requestGroupMatch.post);

        groupInvited.getPlayers().forEach(pl -> {
            pl.sendMessage(mm.deserialize(
                    "<gradient:#00ff99:#0099ff><bold>" + p.getName() + " se ha unido al grupo <white>" + groupInvited.getName() + "</white>!</bold></gradient>"
            ));
        });

        p.sendMessage(mm.deserialize(
                "<green><bold>Has aceptado la invitación al grupo <white>" + groupInvited.getName() + "</white>.</bold></green>\n" +
                        "<gray>¡Ya puedes colaborar con tus compañeros!</gray>"
        ));

        groupInvited.pending.removeIf(pre -> InPlayer.name(pre.player).equalsIgnoreCase(p.getName()));
        pc.getRequestGroup().remove(requestGroupMatch);

        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
        DS.edit("id", groupInvited.id.toString(), PlayerGroupData.create(groupInvited), PlayerGroupData.class);
    }
}
