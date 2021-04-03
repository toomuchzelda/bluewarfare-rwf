package me.libraryaddict.core.fakeentity;

import org.bukkit.entity.Player;

public interface EntityInteract {
    public void onInteract(Player player, InteractType interactType);

    public enum InteractType {
        ATTACK, MAIN_INTERACT, OFFHAND_INTERACT;
    }
}
