package com.spygamingog.spynetherportals;

import com.spygamingog.spycore.api.SpyAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class GroupMessagingListener implements Listener {

    private final SpyNetherPortals plugin;

    public GroupMessagingListener(SpyNetherPortals plugin) {
        this.plugin = plugin;
    }

    private List<Player> getGroupPlayers(World world) {
        List<Player> players = new ArrayList<>();
        String name = world.getName();
        String baseName = name;

        if (name.endsWith("_nether")) {
            baseName = name.substring(0, name.length() - 7);
        } else if (name.endsWith("_the_end")) {
            baseName = name.substring(0, name.length() - 8);
        }

        // Add players from base world
        World base = Bukkit.getWorld(baseName);
        if (base != null) players.addAll(base.getPlayers());

        // Add players from nether
        World nether = Bukkit.getWorld(baseName + "_nether");
        if (nether != null) players.addAll(nether.getPlayers());

        // Add players from end
        World end = Bukkit.getWorld(baseName + "_the_end");
        if (end != null) players.addAll(end.getPlayers());
        
        return players;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        // SpyCore (LOWEST) removes everyone not in the same world.
        // We (HIGH) add back players who are in the linked group (Nether/End).
        Player sender = event.getPlayer();
        List<Player> group = getGroupPlayers(sender.getWorld());
        
        for (Player p : group) {
            if (!event.getRecipients().contains(p)) {
                event.getRecipients().add(p);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinTablist(PlayerJoinEvent event) {
        updateTabVisibility(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChangeTablist(PlayerChangedWorldEvent event) {
        updateTabVisibility(event.getPlayer());
    }

    private void updateTabVisibility(Player player) {
        // SpyCore hides everyone not in the same world.
        // We need to show players in the linked group.
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            
            List<Player> group = getGroupPlayers(player.getWorld());
            for (Player other : group) {
                if (player.equals(other)) continue;
                // Reveal each other
                player.showPlayer(plugin, other);
                other.showPlayer(plugin, player);
            }
        }, 3L); // Run slightly after SpyCore (which runs at 2L)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        String msg = event.getJoinMessage();
        if (msg == null) return;

        event.setJoinMessage(null);
        
        List<Player> recipients = getGroupPlayers(event.getPlayer().getWorld());
        for (Player p : recipients) {
            p.sendMessage(msg);
        }
        // Also send to console
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        String msg = event.getQuitMessage();
        if (msg == null) return;

        event.setQuitMessage(null);

        List<Player> recipients = getGroupPlayers(event.getPlayer().getWorld());
        for (Player p : recipients) {
            p.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        String msg = event.getDeathMessage();
        if (msg == null) return;

        event.setDeathMessage(null);

        List<Player> recipients = getGroupPlayers(event.getEntity().getWorld());
        for (Player p : recipients) {
            p.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        // Skip recipe unlocks
        if (event.getAdvancement().getKey().getKey().startsWith("recipes/")) return;

        net.kyori.adventure.text.Component msg = event.message();
        if (msg == null) return;

        event.message(null); // Suppress global broadcast

        List<Player> recipients = getGroupPlayers(event.getPlayer().getWorld());
        for (Player p : recipients) {
            p.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }
}
