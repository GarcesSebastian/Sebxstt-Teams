package com.sebxstt;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import com.sebxstt.helpers.GroupPermissions;
import com.sebxstt.instances.enums.PlayerTypeGroup;
import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.instances.CheckPoint;
import com.sebxstt.instances.Main;
import com.sebxstt.instances.PlayerConfig;
import com.sebxstt.instances.PlayersGroup;
import com.sebxstt.functions.utils.Lib;
import com.sebxstt.nextinventory.NextInventoryProvider;
import com.sebxstt.managers.CommandManager;
import com.sebxstt.providers.ConfigurationProvider;
import com.sebxstt.providers.DataStoreProvider;
import com.sebxstt.providers.PlayerProvider;
import com.sebxstt.providers.PluginProvider;
import com.sebxstt.serialize.data.PlayerConfigData;
import com.sebxstt.serialize.data.PlayerGroupData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import static com.sebxstt.providers.DataStoreProvider.DS;

import java.util.*;

public class index extends JavaPlugin implements Listener {
    public static Main mainData = new Main();
    public static final Map<UUID, ArmorStand> nameTags = new HashMap<>();
    public static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        ConfigurationProvider.load(this);
        DataStoreProvider.init(this);
        PluginProvider.init(this);

        NextInventoryProvider.setup(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            CommandManager.registerAll(event);
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("[Sebxstt] Guardando datos antes del apagado...");

        for (PlayerConfig pc : mainData.playerConfigs) {
            DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
        }

        for (PlayersGroup pg : mainData.playersGroups) {
            DS.edit("id", pg.id.toString(), PlayerGroupData.create(pg), PlayerGroupData.class);
        }

        for (ArmorStand as : nameTags.values()) {
            if (as != null) as.remove();
        }
        nameTags.clear();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inventory = event.getInventory();
        PlayerConfig pc = Lib.getPlayerConfig(player);
        PlayerTypeGroup playerTypeGroup = pc.getPlayerType();

        PlayersGroup grp = Lib.FindPlayerInGroup(player.getName());
        if (grp == null) return;

        if (!inventory.equals(grp.getStorage().instance)) return;

        int rawSlot = event.getRawSlot();
        InventoryView view = event.getView();

        if (rawSlot < view.getTopInventory().getSize() && rawSlot == 0) {
            event.setCancelled(true);
            player.sendMessage(Component.text("Este ítem no se puede mover."));
            return;
        }

        if (GroupPermissions.canEditStorage(playerTypeGroup)) {
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().equals(event.getView().getTopInventory())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        mainData = DS.load();

        if (mainData.playersGroups == null) {
            System.out.println("[Index] PlayersGroup is NULL");
        }

        if (mainData.playerConfigs == null) {
            System.out.println("[Index] PlayersConfig is NULL");
        }

        Player player = event.getPlayer();
        PlayerProvider.setup(player.getUniqueId());
        PlayersGroup grp = Lib.FindPlayerInGroup(player.getName());
        player.removePotionEffect(PotionEffectType.GLOWING);

        PlayerConfigData pcd = DS.get("player", player.getUniqueId().toString(), PlayerConfigData.class);

        if(pcd == null) {
            PlayerConfig pc = new PlayerConfig(player.getUniqueId());
            mainData.playerConfigs.add(pc);
            DS.create(PlayerConfigData.create(pc), PlayerConfigData.class);
            InPlayer.message(player.getUniqueId(),
                    "<blue><bold>Se ha creado tu PlayerConfig</bold></blue>"
            );
        }

        PlayerConfig pc = Lib.getPlayerConfig(player);

        if (pc.getChatEnabledGroup()) {
            player.sendMessage(mm.deserialize("<aqua>Tu chat grupal está <bold>activado</bold>. Usa <gold>/group chat off</gold> para desactivar.</aqua>"));
        }

        if (grp != null) {
            grp.TargetMembers();
        }
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {
            case SUCCESSFULLY_LOADED -> e.getPlayer().sendMessage("Resource pack cargado.");
            case DECLINED -> e.getPlayer().kickPlayer("Debes aceptar el resource pack.");
            case FAILED_DOWNLOAD -> e.getPlayer().sendMessage("Falló la descarga del pack.");
            default -> {}
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        Lib.removeCustomNameTag(player);

        PlayerConfig pc = Lib.getPlayerConfig(player);
        if (pc == null) {
            this.getLogger().warning("No se encontró configuración para el jugador " + player.getName() + " al desconectarse.");
            return;
        }

        try {
            DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
        } catch (Exception e) {
            this.getLogger().severe("Error al guardar la configuración del jugador " + player.getName());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ArmorStand as = nameTags.get(player.getUniqueId());
        if (as != null && !as.isDead()) {
            Location to = player.getLocation().clone();
            Location newLoc = to.clone().add(0, 2.6, 0);
            as.teleport(newLoc);
            player.showEntity(this, as);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            PlayerProvider.setup(player.getUniqueId());
            PlayersGroup grp = Lib.FindPlayerInGroup(player.getName());
            player.removePotionEffect(PotionEffectType.GLOWING);

            if (grp != null) {
                grp.TargetMembers();
            }
        }, 1L);
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(event.message());
        var grp = Lib.FindPlayerInGroup(player.getName());
        event.setCancelled(true);

        PlayerConfig pc = Lib.getPlayerConfig(player);
        if (pc != null && pc.getChatEnabledGroup()) {
            String color = grp.getColor().name().toLowerCase();
            String msgRaw = "[Grupo: <" + color + ">" + grp.getName() + "</" + color + ">] "
                    + "<gray>" + player.getName() + "</gray>: "
                    + message;

            for (Player p : grp.getPlayers()) {
                p.sendMessage(mm.deserialize(msgRaw));
            }
            return;
        }

        String output = "<white>" + player.getName() + "</white>";

        if (grp != null) {
            String color = grp.getColor().name().toLowerCase();
            output = "<gray>" + player.getName() + "</gray> [<" + color + ">" + grp.getName() + "</" + color + ">]";
        }

        Bukkit.broadcast(mm.deserialize(output + ": " + message));
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player damager)) return;
        if(!(event.getEntity() instanceof Player victim)) return;

        PlayersGroup grpDamager = Lib.FindPlayerInGroup(damager.getName());
        PlayersGroup grpVictim = Lib.FindPlayerInGroup(victim.getName());

        if(grpVictim == null || grpDamager == null) return;
        if(!grpVictim.getName().equalsIgnoreCase(grpDamager.getName())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onDeathEvent(EntityDeathEvent event) {
        if(!(event.getEntity() instanceof Player player)) return;

        PlayerConfig pc = Lib.getPlayerConfig(player);
        if(pc == null) return;

        Location loc = player.getLocation();
        CheckPoint cp = new CheckPoint(player, loc, "return");

        pc.saveLastDeath(cp);
    }

    @EventHandler
    public void onExpPickup(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();
        event.setAmount(0);

        player.sendMessage(mm.deserialize(
                "<gold>Se retuvo tu experiencia: <white>" + amount + "</white></gold>"
        ));

        PlayersGroup grp = Lib.FindPlayerInGroup(player.getName());
        if (grp == null) {
            player.giveExp(amount);
            player.sendMessage(mm.deserialize(
                    "<red>No perteneces a ningún grupo. Recuperaste toda tu experiencia.</red>"
            ));
            return;
        }

        List<Player> closest = Lib.ClosestMembers(grp, player, 30.0);

        Set<Player> recipients = new LinkedHashSet<>(closest);
        recipients.add(player);

        if (recipients.size() <= 1) {
            player.giveExp(amount);
            player.sendMessage(mm.deserialize(
                    "<yellow>No hay miembros cercanos. Recuperaste toda tu experiencia.</yellow>"
            ));
            return;
        }

        int count = recipients.size();
        float perPlayer = (float) amount / count;

        player.sendMessage(mm.deserialize(
                "<green>Distribuyendo <white>" + amount + "</white> exp entre <white>" + count + "</white> miembros.</green>"
        ));

        for (Player m : recipients) {
            int toGive = Math.round(perPlayer);
            m.giveExp(toGive);
            m.sendMessage(mm.deserialize(
                    "<aqua>Has recibido <white>" + toGive + "</white> exp compartida.</aqua>"
            ));
        }
    }
}