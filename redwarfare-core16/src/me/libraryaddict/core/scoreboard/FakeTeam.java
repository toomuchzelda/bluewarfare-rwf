package me.libraryaddict.core.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.data.EnumChatFormatHelper;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class FakeTeam {
    private FakeScoreboard _fakeScoreboard;
    private ScoreboardTeam _nmsTeam;
    private HashMap<Team.Option, Team.OptionStatus> _options = new HashMap<Team.Option, Team.OptionStatus>();
    private ArrayList<String> _players = new ArrayList<String>();
    private String _prefix = "";
    private boolean _seeInvisibles;
    private Field _setPrefix;
    private Field _setSuffix;
    private String _suffix = "";
    private Team _team;
    private String _teamName;
    private PacketContainer _teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

    public FakeTeam(FakeScoreboard fakeScoreboard, String teamName) {
        _teamName = teamName;
        _fakeScoreboard = fakeScoreboard;

        _teamPacket.getStrings().write(0, teamName);
        
        WrappedChatComponent comp = (WrappedChatComponent) WrappedChatComponent
                .fromChatMessage(teamName)[0];
        
        _teamPacket.getChatComponents().write(0, comp);
        //_teamPacket.getStrings().write(1, teamName);
        _teamPacket.getIntegers().write(0, 2);

        writePacketOption();
    }

    public void addPlayer(Player player) {
        addPlayer(player.getName());
    }

    public void addPlayer(String playerName) {
        if (_players.contains(playerName))
            return;

        for (FakeTeam team : _fakeScoreboard.getFakeTeams()) {
            team.removePlayer(playerName);
        }

        for (FakeTeam team : getChildren()) {
            team.addPlayer(playerName);
        }

        _players.add(playerName);

        ((ArrayList<String>) _teamPacket.getModifier().read(7)).add(playerName);

        _team.addEntry(playerName);

        Player p = Bukkit.getPlayerExact(playerName);

        if (p != null) {
            calculateVisibilities(p);
        }
        /*  else
        {
            System.out.println("Can't do " + playerName + " cos no player");
        }*/
    }

    private void calculateVisibilities(Player modified) {
        PacketContainer[] spawn = null;
        PacketContainer delete = CentralManager.getManager().getDelete(modified.getEntityId());

        // If you can't see invisible allies, or he was removed from this team
        // If you cannot see invisible players, or the player is not part of the team
        if (!canSeeInvisiblePlayers() || !_players.contains(modified.getName())) {
            // You can definitely display the packets
            spawn = CentralManager.getManager().getFakePlayerSpawn(modified.getUniqueId(), modified.getEntityId(),
                    modified.getLocation().toVector());
        }

        ArrayList<Player> players = UtilPlayer.getPerverts(modified);

        if (players == null) {
            // System.out.println("Can't do " + modified.getName() + " cos no tracker");
            return;
        } // System.out.println("Now handling " + modified.getName());

        for (Player player : players) {
            if (!_fakeScoreboard.isApplicable(player))
                continue;

            boolean canSeePlayers = spawn != null && !_players.contains(player.getName());

            if (canSeePlayers) {
                if (!CentralManager.getManager().isFakePlayersDisabled()) {
                    UtilPlayer.sendPacket(player, spawn);
                }
                /*    if (player.getName().equals("libraryaddict"))

                    System.out.println("2 Teams showing " + modified.getName() + " " + canSeeInvisiblePlayers() + " "
                            + _players.contains(modified.getName()) + " " + _players.contains(player.getName()) + " "
                            + getTeamName());*/
            } else {
                UtilPlayer.sendPacket(player, delete);

                /*    if (player.getName().equals("libraryaddict"))
                    System.out.println("2 Teams hiding " + modified.getName() + " " + canSeeInvisiblePlayers() + " "
                            + _players.contains(modified.getName()) + " " + _players.contains(player.getName()) + " "
                            + getTeamName());*/
            }
        }
    }

    public boolean canSeeInvisiblePlayers() {
        return _seeInvisibles;
    }

    public FakeTeam clone(FakeScoreboard board) {
        FakeTeam team = new FakeTeam(board, _teamName);

        team._prefix = _prefix;
        team._seeInvisibles = _seeInvisibles;
        team._suffix = _suffix;
        team._players = new ArrayList<String>(_players);
        team._options = new HashMap<Team.Option, Team.OptionStatus>(_options);

        team._teamPacket = _teamPacket.deepClone();

        team.setupTeam(board.getScoreboard());

        return team;
    }

    private ArrayList<FakeTeam> getChildren() {
        return _fakeScoreboard.getTeamChildren(getTeamName());
    }

    public ArrayList<String> getPlayers() {
        return _players;
    }

    public String getPrefix() {
        return _prefix;
    }

    public void setPrefix(String prefix) {
    	
    	//prefix += "asdf";
    	
        for (FakeTeam team : getChildren()) {
            team.setPrefix(prefix);
        }

        if (_prefix.equals(prefix))
            return;

        _prefix = prefix;

        WrappedChatComponent components = WrappedChatComponent.fromText(prefix);

        try {
            _nmsTeam.setPrefix((IChatBaseComponent) components.getHandle());
        } catch (Exception ex) {
            UtilError.handle(ex);
        }

        _teamPacket.getChatComponents().write(1, components);

        
        for (Player player : _fakeScoreboard.getPlayers()) {
            UtilPlayer.sendPacket(player, _teamPacket);
        }
        
        //System.out.println("set Prefix has run with arg: " + prefix + "asdf");
        
        this.setColor(prefix);
    }

    public Scoreboard getScoreboard() {
        return _fakeScoreboard.getScoreboard();
    }

    public String getSuffix() {
        return _suffix;
    }

    public void setSuffix(String suffix) {
        for (FakeTeam team : getChildren()) {
            team.setSuffix(suffix);
        }

        if (_suffix.equals(suffix))
            return;

        _suffix = suffix;

        WrappedChatComponent components = WrappedChatComponent.fromText(suffix);

        try {
            // how convenient
            _nmsTeam.setSuffix((IChatBaseComponent) components.getHandle());
        } catch (Exception ex) {
            UtilError.handle(ex);
        }

        _teamPacket.getChatComponents().write(2, components);

        for (Player player : _fakeScoreboard.getPlayers()) {
            UtilPlayer.sendPacket(player, _teamPacket);
        }
    }

    public String getTeamName() {
        return _teamName;
    }

    public void removePlayer(String playerName) {
        if (!_players.contains(playerName))
            return;

        for (FakeTeam team : getChildren()) {
            team.removePlayer(playerName);
        }

        _players.remove(playerName);

        ((ArrayList<String>) _teamPacket.getModifier().read(7)).remove(playerName);

        _team.removeEntry(playerName);

        Player p = Bukkit.getPlayerExact(playerName);

        if (p != null) {
            calculateVisibilities(p);
        }
    }

    public void setOption(Team.Option option, Team.OptionStatus optionStatus) {
        for (FakeTeam team : getChildren()) {
            team.setOption(option, optionStatus);
        }

        if (_options.containsKey(option) && _options.get(option) == optionStatus)
            return;

        _options.put(option, optionStatus);

        if (option == Option.NAME_TAG_VISIBILITY)
            _teamPacket.getStrings().write(1, optionStatus.name());

        if (option == Option.COLLISION_RULE)
            _teamPacket.getStrings().write(2, optionStatus.name());

        _team.setOption(option, optionStatus);
    }

    public void setSeeInvisiblePlayers(boolean seeInvisibles) {
        for (FakeTeam team : getChildren()) {
            team.setSeeInvisiblePlayers(seeInvisibles);
        }

        if (seeInvisibles == _seeInvisibles)
            return;

        _seeInvisibles = seeInvisibles;

        writePacketOption();

        _team.setAllowFriendlyFire(!seeInvisibles);
        _team.setCanSeeFriendlyInvisibles(seeInvisibles);

        for (String playerName : getPlayers()) {
            Player p = Bukkit.getPlayerExact(playerName);

            if (p != null) {
                calculateVisibilities(p);
            }
        }
    }

    public void setupTeam(Scoreboard board) {
        _team = board.getTeam(_teamName);

        if (_team == null)
            _team = board.registerNewTeam(_teamName);

        _team.setPrefix(_prefix);
        _team.setSuffix(_suffix);
        _team.setAllowFriendlyFire(!_seeInvisibles);
        _team.setCanSeeFriendlyInvisibles(_seeInvisibles);

        for (Option option : _options.keySet()) {
            _team.setOption(option, _options.get(option));
        }

        for (String entry : _players) {
            if (_team.hasEntry(entry))
                continue;

            _team.addEntry(entry);
        }

        try {
            _setPrefix = ScoreboardTeam.class.getDeclaredField("e");
            _setSuffix = ScoreboardTeam.class.getDeclaredField("f");

            _setPrefix.setAccessible(true);
            _setSuffix.setAccessible(true);

            Field field = Class.forName("org.bukkit.craftbukkit.v1_16_R3.scoreboard.CraftTeam").getDeclaredField("team");
            field.setAccessible(true);

            _nmsTeam = (ScoreboardTeam) field.get(_team);
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    private void writePacketOption() {
        int m = 0;

        if (!_seeInvisibles) {
            m |= 0x1;
        }
        if (_seeInvisibles) {
            m |= 0x2;
        }

        _teamPacket.getIntegers().write(1, m);
    }
    
    //sets the NMS team colour and sends packet
    //gets called in setPrefix
    public void setColor(String colorCode)
    {
    	for (FakeTeam team : getChildren())
    	{
    		team.setColor(colorCode);
    	}
    	
    	EnumChatFormat enumChat = EnumChatFormatHelper.enumChatFromString(colorCode);
    	
    	//System.out.println("setColor running, enumChat toString is: " + enumChat.toString());
    	
    	try
    	{
    		_nmsTeam.setColor(enumChat);
    	}
    	catch(Exception ex)
    	{
    		UtilError.handle(ex);
    	}
    	
    	_teamPacket.getModifier().write(6, enumChat);
    	
    	for(Player player : _fakeScoreboard.getPlayers())
    	{
    		UtilPlayer.sendPacket(player, _teamPacket);
    	}
    	//ChatColor chatColor = ChatColor.getByChar(colorCode);
    	
    	//_team.setColor(chatColor);
    	
    	//System.out.println("setColor has run.");
    }

}
