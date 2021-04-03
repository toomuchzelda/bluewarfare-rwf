package me.libraryaddict.arcade.events;

import me.libraryaddict.core.combat.CombatLog;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DeathEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();
    private boolean _cancelled;
    private CombatLog _combatLog;
    private String _killedPrefix = "";
    private String _killerPrefix = "";
    private Player _player;

    public DeathEvent(Player player, CombatLog combatLog) {
        _player = player;
        _combatLog = combatLog;
    }

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    public AttackType getAttackType() {
        return getDamageEvent().getAttackType();
    }

    public CombatLog getCombatLog() {
        return _combatLog;
    }

    public CustomDamageEvent getDamageEvent() {
        return getCombatLog().getLastEvent().getEvent();
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public String getKilledPrefix() {
        return _killedPrefix;
    }

    public void setKilledPrefix(String prefix) {
        _killedPrefix = prefix;
    }

    public String getKillerPrefix() {
        return _killerPrefix;
    }

    public void setKillerPrefix(String prefix) {
        _killerPrefix = prefix;
    }

    public Entity getLastAttacker() {
        return _combatLog.getKiller();
    }

    public Player getPlayer() {
        return _player;
    }

    @Override
    public boolean isCancelled() {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        _cancelled = cancel;
    }

}
