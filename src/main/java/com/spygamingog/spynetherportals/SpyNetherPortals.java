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

import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.logging.Level;

public class SpyNetherPortals extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        
        // Initial scan for already loaded worlds
        for (World world : Bukkit.getWorlds()) {
            checkAndLinkWorld(world);
        }
        
        getLogger().info("SpyNetherPortals v1.0.2 enabled. Linker and Chat/Tablist Sync active.");
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        checkAndLinkWorld(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        SpyAPI.unlinkWorlds(event.getWorld());
    }

    private void checkAndLinkWorld(World world) {
        String name = world.getName();
        String baseName = null;

        if (name.endsWith("_nether")) {
            baseName = name.substring(0, name.length() - 7);
        } else if (name.endsWith("_the_end")) {
            baseName = name.substring(0, name.length() - 8);
        }

        if (baseName != null) {
            World overworld = SpyAPI.getWorld(baseName, false);
            if (overworld != null) {
                SpyAPI.linkWorlds(world, overworld);
                getLogger().info("[Linker] Linked " + name + " to " + overworld.getName() + " for Chat/Tablist Sync.");
            } else {
                getLogger().info("[Linker] Found dimension " + name + " but overworld " + baseName + " is not loaded.");
            }
        } else {
            // Check if this is an overworld that has nether/end already loaded
            World nether = SpyAPI.getWorld(name + "_nether", false);
            if (nether != null) {
                SpyAPI.linkWorlds(world, nether);
                getLogger().info("[Linker] Linked " + name + " to " + nether.getName() + " for Chat/Tablist Sync.");
            }
            World end = SpyAPI.getWorld(name + "_the_end", false);
            if (end != null) {
                SpyAPI.linkWorlds(world, end);
                getLogger().info("[Linker] Linked " + name + " to " + end.getName() + " for Chat/Tablist Sync.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();
        World fromWorld = from.getWorld();
        if (fromWorld == null) return;

        String fromName = fromWorld.getName();
        World targetWorld = null;

        getLogger().info("[Portal] Player " + event.getPlayer().getName() + " entered portal in " + fromName + 
            " (Env: " + fromWorld.getEnvironment() + ", Cause: " + event.getCause() + ")");

        // Handle Nether Portals
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // Overworld -> Nether
                targetWorld = findLinkedWorld(fromName, "_nether");
                getLogger().info("[Portal] Looking for nether for " + fromName + "... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
            } else if (fromWorld.getEnvironment() == World.Environment.NETHER) {
                // Nether -> Overworld
                String baseName = fromName.endsWith("_nether") ? fromName.substring(0, fromName.length() - 7) : fromName;
                targetWorld = SpyAPI.getWorld(baseName);
                getLogger().info("[Portal] Looking for overworld for " + fromName + " (base: " + baseName + ")... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
            }
        } 
        // Handle End Portals
        else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (fromWorld.getEnvironment() == World.Environment.NORMAL) {
                // Overworld -> End
                targetWorld = findLinkedWorld(fromName, "_the_end");
                getLogger().info("[Portal] Looking for end for " + fromName + "... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
            } else if (fromWorld.getEnvironment() == World.Environment.THE_END) {
                // End -> Overworld
                String baseName = fromName.endsWith("_the_end") ? fromName.substring(0, fromName.length() - 8) : fromName;
                targetWorld = SpyAPI.getWorld(baseName);
                getLogger().info("[Portal] Looking for overworld for " + fromName + " (base: " + baseName + ")... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
            }
        }

        if (targetWorld != null) {
            Location to = event.getTo();
            to.setWorld(targetWorld);
            
            // Handle Nether Portal coordinate scaling
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                double scale = (fromWorld.getEnvironment() == World.Environment.NORMAL) ? 0.125 : 8.0;
                to.setX(from.getX() * scale);
                to.setZ(from.getZ() * scale);
                // Keep Y the same, but ensure it's within bounds
                to.setY(Math.max(0, Math.min(targetWorld.getMaxHeight() - 1, from.getY())));
            } 
            // Handle End Portal destinations
            else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                if (targetWorld.getEnvironment() == World.Environment.THE_END) {
                    // Standard End platform location
                    to.setX(100.5);
                    to.setY(49);
                    to.setZ(0.5);
                } else {
                    // Returning to overworld from end
                    Location spawn = targetWorld.getSpawnLocation();
                    to.setX(spawn.getX());
                    to.setY(spawn.getY());
                    to.setZ(spawn.getZ());
                }
            }
            
            // NOTE: We no longer force a "safe location" here. 
            // By setting the destination world and coordinates, we allow Bukkit's 
            // internal portal logic to search for an existing portal or create a new one,
            // which preserves the portal linking the user requested.
            
            getLogger().info("Redirecting " + event.getPlayer().getName() + " to " + targetWorld.getName() + 
                " at [" + to.getBlockX() + ", " + to.getBlockY() + ", " + to.getBlockZ() + "]");
        } else {
            // No linked world found, block the portal and notify
            event.setCancelled(true);
            String dimension = event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? "Nether" : "The End";
            event.getPlayer().sendMessage("§cThis world does not have a linked " + dimension + ".");
        }
    }

    private World findLinkedWorld(String baseName, String suffix) {
        // Try direct name + suffix
        // We use true here because if a player is entering a portal, 
        // we WANT the destination world to load if it's hibernating.
        World target = SpyAPI.getWorld(baseName + suffix, true);
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
