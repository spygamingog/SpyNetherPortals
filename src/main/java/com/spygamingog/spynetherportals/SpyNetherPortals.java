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
        Bukkit.getPluginManager().registerEvents(new GroupMessagingListener(this), this);
        
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
                targetWorld = findOverworld(fromWorld, "_nether");
                getLogger().info("[Portal] Looking for overworld for " + fromName + "... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
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
                targetWorld = findOverworld(fromWorld, "_the_end");
                getLogger().info("[Portal] Looking for overworld for " + fromName + "... Found: " + (targetWorld != null ? targetWorld.getName() : "null"));
            }
        }

        if (targetWorld != null) {
            Location to = event.getTo();
            if (to == null) {
                to = new Location(targetWorld, from.getX(), from.getY(), from.getZ(), from.getYaw(), from.getPitch());
            } else {
                to.setWorld(targetWorld);
            }
            
            // Handle Nether Portal coordinate scaling
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                double scale = (fromWorld.getEnvironment() == World.Environment.NORMAL) ? 0.125 : 8.0;
                to.setX(from.getX() * scale);
                to.setZ(from.getZ() * scale);
                // Keep Y within world height bounds
                to.setY(Math.max(targetWorld.getMinHeight() + 1, Math.min(targetWorld.getMaxHeight() - 1, from.getY())));
                event.setCanCreatePortal(true);
                event.setSearchRadius(128);
                event.setCreationRadius(16);
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
            
            event.setTo(to);
            
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
        // 1. Try direct name + suffix (e.g. survival_nether)
        World target = SpyAPI.getWorld(baseName + suffix, true);
        if (target != null) return target;

        // 2. Try with clean alias
        World fromWorld = Bukkit.getWorld(baseName);
        if (fromWorld != null) {
            String alias = SpyAPI.getAliasForWorld(fromWorld);
            if (alias != null && !alias.equalsIgnoreCase(baseName)) {
                target = SpyAPI.getWorld(alias + suffix, true);
                if (target != null) return target;
            }

            // 3. Try containerized path
            String container = SpyAPI.getContainerForWorld(fromWorld);
            if (container != null && !container.equalsIgnoreCase("root")) {
                target = SpyAPI.getWorld(container + "/" + (alias != null ? alias : baseName) + suffix, true);
                if (target != null) return target;
            }
        }

        return null;
    }

    private World findOverworld(World fromWorld, String suffix) {
        String fromName = fromWorld.getName();
        String baseName = fromName.toLowerCase().endsWith(suffix.toLowerCase())
                ? fromName.substring(0, fromName.length() - suffix.length())
                : fromName;

        World target = SpyAPI.getWorld(baseName, true);
        if (target != null) return target;

        String alias = SpyAPI.getAliasForWorld(fromWorld);
        if (alias != null) {
            String baseAlias = alias.toLowerCase().endsWith(suffix.toLowerCase())
                    ? alias.substring(0, alias.length() - suffix.length())
                    : alias;
            target = SpyAPI.getWorld(baseAlias, true);
            if (target != null) return target;

            String container = SpyAPI.getContainerForWorld(fromWorld);
            if (container != null && !container.equalsIgnoreCase("root")) {
                target = SpyAPI.getWorld(container + "/" + baseAlias, true);
                if (target != null) return target;
            }
        }

        return null;
    }
}
