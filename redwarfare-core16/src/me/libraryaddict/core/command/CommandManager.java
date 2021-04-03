package me.libraryaddict.core.command;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import me.libraryaddict.core.C;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.command.commands.*;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class CommandManager extends MiniPlugin {
    private ArrayList<String> _bypassCommands = new ArrayList<String>();
    private ArrayList<SimpleCommand> _commands = new ArrayList<SimpleCommand>();
    private ProtocolManager _protocolManager;
    private RankManager _rankManager;

    public CommandManager(JavaPlugin plugin) {
        super(plugin, "Command Manager");

        _protocolManager = ProtocolLibrary.getProtocolManager();

        _protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.TAB_COMPLETE) {
            private PacketConstructor _constructor = _protocolManager.createPacketConstructor(PacketType.Play.Server.TAB_COMPLETE,
                    Integer.class, Suggestions.class);

            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    event.setCancelled(true);

                    //Bukkit.broadcastMessage("Packet event called");
                    
                    //event.getPlayer().updateCommands();
                    
                    //get what the player has written so far
                    String commandTyped = event.getPacket().getStrings().read(0);
                    ArrayList<String> returns = onTabComplete(event.getPlayer(), commandTyped);
                    //Bukkit.broadcastMessage(event.getPacket().getStrings().read(0));
                    
                    
                    Collections.sort(returns, String.CASE_INSENSITIVE_ORDER);

                    if (returns.isEmpty())
                    {
                    	//Bukkit.broadcastMessage("returns is empty");
                        return;
                    }

                    //String[] arg = returns.toArray(new String[0]);
                    
                    //https://github.com/Mojang/brigadier/blob/master/src/main/java/com/mojang/brigadier/suggestion/Suggestion.java
                    int id = event.getPacket().getIntegers().read(0);
                    
                    //StringRange: where the suggestion starts (and ends?)
                    //start value is the index the suggestion starts.
                    //so for typing "game forcestop" and so far have typed: "game for"
                    //start value is 5 (i think) including the /
                    //end value is 9, its all of what's been typed so far i think im too tired to remember
                    int stringIndex = commandTyped.length();
                    
                    //get the last space in the command as the marker of where to start the next suggestion
                    for(int i = commandTyped.length() - 1; i >= 0; i--)
                    {
                    	if(commandTyped.charAt(i) == ' ')
                    	{
                    		stringIndex = i + 1;
                    		break;
                    	}
                    }
                    
                    StringRange stringRange = new StringRange(stringIndex, commandTyped.length());
                    //Bukkit.broadcastMessage("StringRange: " + stringRange.toString());
                    
                    List<Suggestion> suggestionList = new ArrayList<>();
                    
                    for(String arg : returns)
                    {
                    	//Bukkit.broadcastMessage("number of args: " + returns.size());
                    	Suggestion toAdd = new Suggestion(stringRange, arg);
                    	suggestionList.add(toAdd);
                    }
                    
                    //construct a bunch of individual Suggestion(s) for the command and then make a Suggestions
                    //(with an s on the end) to send to the player
                    Suggestions suggestionsToSend = new Suggestions(stringRange, suggestionList);
                    
                    PacketContainer tabComplete = _constructor.createPacket(id, suggestionsToSend);
                    
                    //_protocolManager.sendServerPacket(event.getPlayer(), _constructor.createPacket((Object) arg));
                    _protocolManager.sendServerPacket(event.getPlayer(), tabComplete);
                }
                catch (Exception e) {
                    UtilError.handle(e);
                }
            }
        }	);

        registerCommand(new CommandGamemode());
        registerCommand(new CommandTeleport());
        registerCommand(new CommandTeleportAll());
        registerCommand(new CommandTeleportHere());
        registerCommand(new CommandTop());
        registerCommand(new CommandBungeeSettings(plugin));
        registerCommand(new CommandKick(plugin));
        registerCommand(new CommandGiveItem());
        registerCommand(new CommandStuck());
        registerCommand(new CommandSudo(this));
        registerCommand(new CommandClearInventory());
        registerCommand(new CommandRefundMe());
    }
    
    //thank the heavens for this guy
    //https://bukkit.org/threads/tutorial-registering-commands-at-runtime.158461/
    private static PluginCommand getPluginCommand(String name, Plugin plugin) {
    	PluginCommand command = null;
     
    	try {
    		Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
    		c.setAccessible(true);
     
    		command = c.newInstance(name, plugin);
    	} catch (SecurityException e) {
    		UtilError.handle(e);
    	} catch (IllegalArgumentException e) {
    		UtilError.handle(e);
    	} catch (IllegalAccessException e) {
    		UtilError.handle(e);
    	} catch (InstantiationException e) {
    		UtilError.handle(e);
    	} catch (InvocationTargetException e) {
    		UtilError.handle(e);
    	} catch (NoSuchMethodException e) {
    		UtilError.handle(e);
    	}
     
    	return command;
    }
    
    //register commands for tab-complete list
    //if its not in the list of commands when you do /[tab] the above packet wont be sent by client
    public void registerCommand(String... aliases) {
    	JavaPlugin plugin = CentralManager.getManager().getPlugin();
    	PluginCommand command = getPluginCommand(aliases[0], plugin);
     
    	command.setAliases(Arrays.asList(aliases));
    	getCommandMap().register(plugin.getDescription().getName(), command);
    }
    
    private static CommandMap getCommandMap() {
    	CommandMap commandMap = null;
     
    	try {
    		if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
    			Field f = SimplePluginManager.class.getDeclaredField("commandMap");
    			f.setAccessible(true);
     
    			commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
    		}
    	} catch (NoSuchFieldException e) {
    		e.printStackTrace();
    	} catch (SecurityException e) {
    		e.printStackTrace();
    	} catch (IllegalArgumentException e) {
    		e.printStackTrace();
    	} catch (IllegalAccessException e) {
    		e.printStackTrace();
    	}
     
    	return commandMap;
    }
    
    //event for when the server sends a list of available commands to the player
    //viewable by /[tab] 
    //this normally includes every command from every plugin but i removed everything not-rwf
    @EventHandler
    public void onSendCommandList(PlayerCommandSendEvent event)
    {
    	//Bukkit.broadcastMessage("command send event called");

    	Iterator<String> collection = event.getCommands().iterator();
    	
    	while(collection.hasNext())
    	{
    		String s = collection.next();
    		boolean remove = true;
    		for(SimpleCommand command : _commands)
    		{
    			for(String alias : command.getAliases())
    			{
    				//if it's a rwf command, dont remove it
    				if(s.equals(alias))
    				{
    					//if they dont have perms for it then remove it (dont show it to them)
    					if(command.canUse(event.getPlayer(), _rankManager.getRank(event.getPlayer())))
    					{
    						remove = false;
    					} else {
    						remove = true;
    					}
    				}
    			}
    		}
    		
    		if(remove)
    		{
    			collection.remove();
    			//Bukkit.broadcastMessage("removed " + s);
    		}
    	}
    }	
    
    public void addBypasses(ArrayList<String> bypasses) {
        _bypassCommands.addAll(bypasses);
    }

    public void addBypasses(String... bypasses) {
        _bypassCommands.addAll(Arrays.asList(bypasses));
    }

    public SimpleCommand getCommand(Class<? extends SimpleCommand> classFile) {
        for (SimpleCommand command : _commands) {
            if (command.getClass().isAssignableFrom(classFile))
                return command;
        }

        return null;
    }

    public SimpleCommand getCommand(String commandAlias) {
        for (SimpleCommand command : _commands) {
            if (command.isAlias(commandAlias))
                return command;
        }

        return null;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        String alias = command.split(" ")[0].substring(1);

        if (_bypassCommands.contains(alias.toLowerCase())) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        String arg = command.substring(command.contains(" ") ? command.indexOf(" ") : command.length()).trim();
        String[] args = arg.isEmpty() ? new String[0] : arg.split(" ");

        for (SimpleCommand simpleCommand : _commands) {
            if (!simpleCommand.isAlias(alias))
                continue;

            PlayerRank rank = _rankManager.getRank(player);

            if (!simpleCommand.canUse(player, rank)) {
                player.sendMessage(C.DRed + "You do not have permission to use this command");
                return;
            }

            try {
                if (simpleCommand.isAdminCommand())
                    simpleCommand.log(player, args);

                simpleCommand.runCommand(player, rank, alias, args);
            } catch (Exception ex) {
                player.sendMessage(UtilError.format("There was an error while running the command"));
                UtilError.handle(ex);
            }

            return;
        }

        player.sendMessage(C.DRed + "Command not found");
    }

    private ArrayList<String> onTabComplete(Player player, String message) {
        String token = message.substring(message.lastIndexOf(" ") + 1);

        ArrayList<String> completions = new ArrayList<String>();

        if (!message.startsWith("/")) {
            for (Player p : UtilPlayer.getPlayers()) {
                if (p.getName().toLowerCase().startsWith(token.toLowerCase())) {
                    completions.add(p.getName());
                }
            }

            return completions;
        }

        String alias = message.split(" ")[0].substring(1);

        PlayerRank rank = _rankManager.getRank(player);

        for (SimpleCommand simpleCommand : _commands) {
            if (!simpleCommand.canUse(player, rank)) {
                continue;
            }

            if (!token.equals(message)) {
                if (!simpleCommand.isAlias(alias)) {
                    continue;
                }

                String arg = message.substring(message.split(" ")[0].length() + 1, message.length() - token.length()).trim();

                String[] args = arg.isEmpty() ? new String[0] : arg.split(" ");

                simpleCommand.onTab(player, rank, args, token, completions);
            } else {
                for (String s : simpleCommand.getAliasesStarting(alias)) {
                    completions.add("/" + s);
                }
            }
        }

        return completions;
    }

    public void registerCommand(SimpleCommand command) {
        for (String commandAlias : command.getAliases()) {
            if (getCommand(commandAlias) != null) {
                throw new IllegalArgumentException(
                        "The command '" + commandAlias + "' is already registered to " + getCommand(commandAlias));
            }
        }

        _commands.add(command);
        command.setPlugin(getPlugin());
        
        //register them for 1.13 tabcomplete list thing
        for(String alias : command.getAliases())
        {
        	//so it doesnt override the vanilla /stop
        	//rwf commands cant be used from console
        	if(!alias.equals("stop")) {
        		registerCommand(alias);
        	}
        }
    }

    public void setRankManager(RankManager rankManager) {
        _rankManager = rankManager;
    }

    public void unregisterCommand(SimpleCommand command) {
        _commands.remove(command);
        command.setPlugin(null);
    }
}
