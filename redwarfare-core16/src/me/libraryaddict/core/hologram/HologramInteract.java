package me.libraryaddict.core.hologram;

import org.bukkit.entity.Player;

public interface HologramInteract {
    public void onInteract(Player player, InteractType interactType);

    public enum InteractType {
        ATTACK, MAIN_INTERACT, OFFHAND_INTERACT;
    }
}
