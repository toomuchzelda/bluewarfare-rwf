package me.libraryaddict.core.player.listener;

import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListenerSavePlayer extends JedisPubSub {
    private PlayerDataManager _playerManager;

    public RedisListenerSavePlayer(PlayerDataManager manager, String serverName) {
        _playerManager = manager;

        RedisManager.addListener(this, RedisKey.NOTIFY_SAVE_PLAYER.fromParams(serverName));
    }

    @Override
    public void onMessage(String channel, String message) {
        new BukkitRunnable() {
            public void run() {
                _playerManager.saveData(UUID.fromString(message));
            }
        }.runTask(_playerManager.getPlugin());
    }

}
