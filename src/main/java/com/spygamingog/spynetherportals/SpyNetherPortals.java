package com.spygamingog.spynetherportals;

import com.spygamingog.spycore.api.SpyAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SpyNetherPortals extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("SpyNetherPortals v1.0.0 enabled. Linker active.");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        World fromWorld = from.getWorld();
        if (fromWorld == null) return;

        String fromName = fromWorld.getName();
        World targetWorld = null;

        // Handle Nether Portals
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // Overworld -> Nether
                targetWorld = findLinkedWorld(fromName, "_nether");
            } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
                // Nether -> Overworld
                String baseName = fromName.endsWith("_nether") ? fromName.substring(0, fromName.length() - 7) : fromName;
                targetWorld = SpyAPI.getWorld(baseName);
            }
        } 
        // Handle End Portals
        else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // Overworld -> End
                targetWorld = findLinkedWorld(fromName, "_the_end");
            } else if (fromWorld.getEnvironment() == World.Environment.THE_END) {
                // End -> Overworld
                String baseName = fromName.endsWith("_the_end") ? fromName.substring(0, fromName.length() - 8) : fromName;
                targetWorld = SpyAPI.getWorld(baseName);
            }
        }

        if (targetWorld != null) {
            event.getTo().setWorld(targetWorld);
            // Simple coordinate scaling for nether
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                double scale = (fromWorld.getEnvironment() == World.Environment.NORMAL) ? 0.125 : 8.0;
                event.getTo().setX(from.getX() * scale);
                event.getTo().setZ(from.getZ() * scale);
            }
            
            // Ensure the destination is safe
            Location safe = SpyAPI.getWorldManager().findSafeLocation(targetWorld);
            // We only use the safe location if the current 'to' is unsafe (optional, but safer to just use it)
            event.setTo(safe);
            
            getLogger().info("Redirecting " + event.getPlayer().getName() + " to " + targetWorld.getName() + " (Safe Spawn)");
        } else {
            // No linked world found, block the portal and notify
            event.setCancelled(true);
            String dimension = event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? "Nether" : "The End";
            event.getPlayer().sendMessage("§cThis world does not have a linked " + dimension + ".");
        }
    }

    private World findLinkedWorld(String baseName, String suffix) {
        // Try direct name + suffix
        World target = SpyAPI.getWorld(baseName + suffix);
        if (target != null) return target;

        // If it's a container world, try to find the linked world in the same container
        String container = SpyAPI.getWorldManager().getContainerForWorld(Bukkit.getWorld(baseName));
        if (!container.equals("root")) {
            // It's a container world, the full name is containers/CON/NAME
            // We already tried the full path via SpyAPI.getWorld, so if it's not loaded, we can't do much
            // unless we want to proactively load it. For now, we only link LOADED worlds.
        }

        return null;
    }
}
