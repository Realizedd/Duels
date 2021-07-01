package me.realized.duels.player;

import lombok.Getter;
import me.realized.duels.DuelsPlugin;
import me.realized.duels.config.Config;
import me.realized.duels.data.LocationData;
import me.realized.duels.util.Loadable;
import me.realized.duels.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfoManager implements Loadable {

    private final DuelsPlugin plugin;
    private final Config config;
    private final File file;
    private final Map<UUID, PlayerInfo> cache = new HashMap<>();

    @Getter
    private Location lobby;

    public PlayerInfoManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfiguration();
        this.file = new File(plugin.getDataFolder(), "lobby.json");
    }

    @Override
    public void handleLoad() {
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file))) {
                this.lobby = plugin.getGson().fromJson(reader, LocationData.class).toLocation();
            } catch (IOException ex) {
                Log.error(this, "Could not load lobby location!", ex);
            }
        }

        if (lobby == null || lobby.getWorld() == null) {
            final World world = Bukkit.getWorlds().get(0);
            this.lobby = world.getSpawnLocation();
            Log.info(this, "Lobby location was not set, using " + world.getName()
                    + "'s spawn location as default. Use the command /duels setlobby in-game to set the lobby location.");
        }
    }

    // PlayerInfo should remain even on reloads
    @Override
    public void handleUnload() {
    }

    public boolean setLobby(final Player player) {
        final Location lobby = player.getLocation().clone();

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file))) {
                plugin.getGson().toJson(new LocationData(lobby), writer);
                writer.flush();
            }

            this.lobby = lobby;
            return true;
        } catch (IOException ex) {
            Log.error(this, "Could not save lobby location!", ex);
            return false;
        }
    }

    public PlayerInfo get(final Player player) {
        return cache.get(player.getUniqueId());
    }

    public void put(final Player player, final PlayerInfo info) {
        cache.put(player.getUniqueId(), info);

        if (!config.isTeleportToLastLocation()) {
            info.setLocation(lobby.clone());
        }
    }

    public void remove(final Player player) {
        cache.remove(player.getUniqueId());
    }

    public PlayerInfo removeAndGet(final Player player) {
        final PlayerInfo info = get(player);
        remove(player);
        return info;
    }
}
