package com.sebxstt.instances;

import com.sebxstt.instances.enums.PlayerTypeGroup;
import com.sebxstt.functions.utils.InPlayer;
import com.sebxstt.functions.utils.Lib;
import com.sebxstt.providers.PlayerProvider;
import com.sebxstt.serialize.data.PlayerConfigData;
import com.sebxstt.serialize.data.PlayerGroupData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.UUID;

import static com.sebxstt.index.*;
import static com.sebxstt.providers.DataStoreProvider.DS;

public class PlayersGroup {
    public UUID id;
    public String name;
    public UUID owner;
    public ArrayList<UUID> members;
    public int level = 1;
    public ChatColor color;
    public StorageTeam storage;
    public GroupMenu menu;

    public ArrayList<PlayersGroup> enemies = new ArrayList<>();
    public ArrayList<PlayersGroup> allies = new ArrayList<>();
    public ArrayList<WarpPoint> warpPoints = new ArrayList<>();
    public ArrayList<PlayerConfig> pending = new ArrayList<>();

    private final Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
    private final Team team;

    public PlayersGroup(String name, UUID owner, ChatColor color) {
        this.id = UUID.randomUUID();
        this.name   = name;
        this.owner  = owner;
        this.color  = color;
        this.members = new ArrayList<>();

        String teamName = "grupo_" + this.id.toString().substring(0, 8);
        Team temp = main.getTeam(teamName);
        if (temp == null) {
            temp = main.registerNewTeam(teamName);
        }

        this.team = temp;

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        team.setColor(color);

        this.TargetMembers();

        this.storage = new StorageTeam(18, this);
        System.out.println("[PlayersGroup] Debug Setter ID " + this.id);
        this.menu = new GroupMenu("Menu Grupal " + this.name, this.id);
    }

    public void addMember(Player member, PlayerTypeGroup post) {
        this.members.add(member.getUniqueId());
        this.TargetMembers();

        if (this.storage != null) {
            this.storage.setupContents();
            System.out.println("[PlayersGroup] Se actualizo el storage de " + this.name);
        }

        PlayerConfig pc = Lib.getPlayerConfig(member);
        pc.setPlayerType(post);
        pc.setCurrentGroup(this.getId());
        PlayerProvider.setup(member.getUniqueId());
        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
        DS.edit("id", this.id.toString(), PlayerGroupData.create(this), PlayerGroupData.class);
    }

    public void kickMember(Player member) {
        team.removeEntry(member.getName());
        Lib.removeCustomNameTag(member);
        member.removePotionEffect(PotionEffectType.GLOWING);
        member.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        members.removeIf(id -> {
            Player p = InPlayer.instance(id);
            return p != null && p.getName().equals(member.getName());
        });

        String plain = "<white>" + member.getName() + "</white>";
        member.setPlayerListName(
                LegacyComponentSerializer.legacySection().serialize(mm.deserialize(plain))
        );
        PlayerConfig pc = Lib.getPlayerConfig(member);
        pc.setCurrentGroup(null);
        pc.setPlayerType(PlayerTypeGroup.NONE);
        PlayerProvider.setup(member.getUniqueId());
        DS.edit("id", pc.id.toString(), PlayerConfigData.create(pc), PlayerConfigData.class);
    }

    public void dissolve() {
        mainData.playersGroups.remove(this);

        unTargetMembers();

        members.clear();
        pending.clear();

        Team t = main.getTeam(team.getName());
        if (t != null) t.unregister();
        Objective obj = main.getObjective(this.name);
        if (obj != null) obj.unregister();

        DS.delete("id", this.id.toString(), PlayerGroupData.class);
    }

    public void sendInvitation(Player invited, Player invitator, PlayerTypeGroup cargo) {
        for (PlayerConfig pc : mainData.playerConfigs){
            if(!(InPlayer.instance(pc.player) instanceof Player p)) return;
            if(!p.getName().equalsIgnoreCase(invited.getName())) continue;
            RequestGroup requestGroup = new RequestGroup(invited.getUniqueId(), invitator.getUniqueId(), this, cargo);

            PlayerConfig pcInvited = Lib.getPlayerConfig(invited);
            pcInvited.invitate(requestGroup);
            this.pending.add(pcInvited);
            DS.edit("id", pc.id.toString(), PlayerConfigData.create(pcInvited), PlayerConfigData.class);
            break;
        }

        DS.edit("id", this.id.toString(), PlayerGroupData.create(this), PlayerGroupData.class);
    }

    public void showInfo(Player executor) {
        String colorTag = this.color.name().toLowerCase();

        String memberList = this.members.isEmpty()
                ? "<italic><gray>— Ninguno —</gray></italic>"
                : this.members.stream()
                .map(m -> "<white>" + InPlayer.name(m) + "</white>")
                .sorted()
                .reduce((a, b) -> a + "<gray>, </gray>" + b)
                .orElse("");

        String alliesList = this.allies.isEmpty()
                ? "<italic><gray>— Ninguno —</gray></italic>"
                : this.allies.stream()
                .map(g -> "<" + g.getColor().name().toLowerCase() + ">" + g.getName() + "</" + g.getColor().name().toLowerCase() + ">")
                .sorted()
                .reduce((a, b) -> a + "<gray>, </gray>" + b)
                .orElse("");

        String enemiesList = this.enemies.isEmpty()
                ? "<italic><gray>— Ninguno —</gray></italic>"
                : this.enemies.stream()
                .map(g -> "<" + g.getColor().name().toLowerCase() + ">" + g.getName() + "</" + g.getColor().name().toLowerCase() + ">")
                .sorted()
                .reduce((a, b) -> a + "<gray>, </gray>" + b)
                .orElse("");

        Component infoMessage = mm.deserialize(
                "<gradient:#00ff99:#0099ff><bold>===== INFORMACIÓN DEL GRUPO =====</bold></gradient>\n" +
                        "<gold><bold>Nombre:</bold></gold> <" + colorTag + "><bold>" + this.name + "</bold></" + colorTag + ">\n" +
                        "<gold><bold>Dueño:</bold></gold> <white>" + InPlayer.name(owner) + "</white>\n" +
                        "<gold><bold>Nivel:</bold></gold> <white>" + this.level + "</white>\n" +
                        "<gold><bold>Color:</bold></gold> <" + colorTag + ">" + colorTag + "</" + colorTag + ">\n\n" +
                        "<gold><bold>Miembros:</bold></gold> " + memberList + "\n\n" +
                        "<gold><bold>Aliados:</bold></gold> " + alliesList + "\n\n" +
                        "<gold><bold>Enemigos:</bold></gold> " + enemiesList + "\n" +
                        "<gradient:#00ff99:#0099ff><bold>=================================</bold></gradient>"
        );

        executor.sendMessage(infoMessage);
    }

    public void TargetMembers() {
        for (Player p : getPlayers()) {
            team.addEntry(p.getName());
            p.setScoreboard(main);

            String output = "<gray>" + p.getName() + "</gray> [<" + this.getColor().name().toLowerCase() + ">" + this.getName() + "</" + this.getColor().name().toLowerCase() + ">]";
            Component comp = mm.deserialize(output);
            String colored = LegacyComponentSerializer.legacySection().serialize(comp);
            p.setPlayerListName(colored);

            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false, true));
        }
    }

    public void unTargetMembers() {
        for (Player p : getPlayers()) {
            team.removeEntry(p.getName());
            Lib.removeCustomNameTag(p);

            String plain = "<white>" + p.getName() + "</white>";
            p.setPlayerListName(
                    LegacyComponentSerializer.legacySection().serialize(mm.deserialize(plain))
            );

            p.removePotionEffect(PotionEffectType.GLOWING);
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            PlayerProvider.setup(p.getUniqueId());
        }
    }

    public UUID getId() {
        return id;
    }
    public String getName()                   { return name; }
    public UUID getOwner()                  { return owner; }
    public ArrayList<UUID> getMembers()     { return members; }
    public ChatColor getColor()               { return color; }
    public StorageTeam getStorage() {
        return storage;
    }
    public ArrayList<Player> getPlayers() {
        ArrayList<Player> list = new ArrayList<>();
        if(InPlayer.instance(owner) instanceof Player p) {
            list.add(p);
        }

        for (UUID m : members) {
            if(InPlayer.instance(m) instanceof Player p) {
                list.add(p);
            }
        }
        return list;
    }
    public ArrayList<WarpPoint> getWarpPoints() {
        return warpPoints;
    }

    public void setId(UUID id) {
        this.id = id;
        this.menu.setGroup(id);
    }
    public void setName(String name)          { this.name = name; }
    public void setOwner(UUID owner)        { this.owner = owner; }
    public void setColor(ChatColor color)     { this.color = color; }
    public void setStorage(StorageTeam storage) {
        this.storage = storage;
    }
    public void setWarpPoints(ArrayList<WarpPoint> warpPoints) {
        this.warpPoints = warpPoints;
    }
}
