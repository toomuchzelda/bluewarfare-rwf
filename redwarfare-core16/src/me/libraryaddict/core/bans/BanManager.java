package me.libraryaddict.core.bans;

import me.libraryaddict.core.bans.commands.*;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BanManager extends MiniPlugin {
    public BanManager(JavaPlugin plugin, CommandManager command) {
        super(plugin, "Ban Manager");

        command.registerCommand(new CommandBan(plugin));
        command.registerCommand(new CommandBanInfo(plugin));
        command.registerCommand(new CommandUnban(plugin));
        command.registerCommand(new CommandMute(plugin));
        command.registerCommand(new CommandUnmute(plugin));
        command.registerCommand(new CommandFindAlts());
    }

}
