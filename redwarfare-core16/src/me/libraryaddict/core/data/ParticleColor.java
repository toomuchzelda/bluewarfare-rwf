package me.libraryaddict.core.data;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;

public enum ParticleColor {
    AQUA(ChatColor.AQUA),
    BLUE(ChatColor.BLUE),
    DARK_PURPLE(ChatColor.DARK_PURPLE),
    DARK_RED(ChatColor.DARK_RED),
    GOLD(ChatColor.GOLD),
    GREEN(ChatColor.GREEN),
    ORANGE(Color.ORANGE),
    PINK(Color.fromRGB(255, 192, 203)),
    PURPLE(ChatColor.LIGHT_PURPLE),
    RED(Color.RED),
    SOOT(Color.BLACK),
    WHITE(Color.WHITE),
    YELLOW(Color.YELLOW);

    public final Color color;

    ParticleColor(Color color) {
        this.color = color;
    }

    ParticleColor(ChatColor chatColor) {
        //this.color = Color.fromRGB(chatColor.getColor().getRGB());
        this.color = Color.fromRGB(chatColor.getColor().getRed(), chatColor.getColor().getGreen(), chatColor.getColor().getBlue());
    }
}
