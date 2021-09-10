package me.libraryaddict.core.scoreboard;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.data.EnumChatFormatHelper;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
//import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.ChatFormatting;
//import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.network.chat.Component;
//import net.minecraft.server.v1_16_R3.ScoreboardTeam;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FakeTeam {
    private FakeScoreboard _fakeScoreboard;
    private PlayerTeam _nmsTeam;
    private HashMap<Team.Option, Team.OptionStatus> _options = new HashMap<Team.Option, Team.OptionStatus>();
    private ArrayList<String> _players = new ArrayList<String>();
    private String _prefix = "";
    private boolean _seeInvisibles;
    private Field _setPrefix;
    private Field _setSuffix;
    private String _suffix = "";
    private Team _team;
    private String _teamName;
    
    //the team itself and team packet used to be stored separately. the packet:
    //private PacketContainer _teamPacket = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
    // however in 1.17 the structure of this packet changed a lot so it's no longer feasible
    // to store a separate packet and modify it. but the NMS constructor for the packet is pretty simple,
    // so i'm gonna store another team to represent what this fake packet was, and use that to construct
    // packets as needed.
    //tbh, I'm not sure that a packet needs to be stored separately, the team and packet fields might be
    // synced but i havent checked every usage.
    //put it on a separate scoreboard so can have same team names and stuff
    private Scoreboard _packetScoreboard;
    private Team _packetTeam;
    private PlayerTeam _packetNmsTeam;
    
    public FakeTeam(FakeScoreboard fakeScoreboard, String teamName) {
        _teamName = teamName;
        _fakeScoreboard = fakeScoreboard;
        
        //need the nms team to be setup to construct packet, so i'm going to move packet construction
        // setupTeam()
        /*
         
         */
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
        
        //((ArrayList<String>) _teamPacket.getModifier().read(7)).add(playerName);
        _packetTeam.addEntry(playerName);
        
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
        
        //team._teamPacket = _teamPacket.deepClone();
        
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
        
        //test for redundant packet sending
        //prefix += "asdf";
        
        for (FakeTeam team : getChildren()) {
            team.setPrefix(prefix);
        }
        
        if (_prefix.equals(prefix))
            return;
        
        _prefix = prefix;
        
        WrappedChatComponent components = WrappedChatComponent.fromText(prefix);
        
        try {
            //_nmsTeam.setPrefix((IChatBaseComponent) components.getHandle());
            _nmsTeam.setPlayerPrefix((Component) components.getHandle());
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
        
        _packetNmsTeam.setPlayerPrefix((Component) components.getHandle());
        //_teamPacket.getChatComponents().write(1, components);
        
        //might be redundant since packet sent again in setColor
        /*for (Player player : _fakeScoreboard.getPlayers()) {
            UtilPlayer.sendPacket(player, getPacket());
        }*/
        
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
            //_nmsTeam.setSuffix((IChatBaseComponent) components.getHandle());
            _nmsTeam.setPlayerSuffix((Component) components.getHandle());
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
        
        _packetNmsTeam.setPlayerSuffix((Component) components.getHandle());
        //_teamPacket.getChatComponents().write(2, components);
        
        for (Player player : _fakeScoreboard.getPlayers()) {
            UtilPlayer.sendPacket(player, getPacket());
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
        
        //((ArrayList<String>) _teamPacket.getModifier().read(7)).remove(playerName);
        _packetTeam.removeEntry(playerName);
        
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
            _packetTeam.setOption(Option.NAME_TAG_VISIBILITY, optionStatus);
            //_teamPacket.getStrings().write(1, optionStatus.name());
        
        if (option == Option.COLLISION_RULE)
            _packetTeam.setOption(Option.COLLISION_RULE, optionStatus);
            //_teamPacket.getStrings().write(2, optionStatus.name());
        
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
        
        //maybe redundant
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
        
        //do the same thing for packet team
        _packetScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        _packetTeam = _packetScoreboard.getTeam(_teamName);
        if(_packetTeam == null)
            _packetTeam = _packetScoreboard.registerNewTeam(_teamName);
        
        _packetTeam.setPrefix(_prefix);
        _packetTeam.setSuffix(_suffix);
        _packetTeam.setAllowFriendlyFire(!_seeInvisibles);
        _packetTeam.setCanSeeFriendlyInvisibles(_seeInvisibles);
        
        for(Option option : _options.keySet()) {
            _packetTeam.setOption(option, _options.get(option));
        }
        
        for (String entry : _players) {
            if (_packetTeam.hasEntry(entry))
                continue;
            
            _packetTeam.addEntry(entry);
        }
        
        try {
            _setPrefix = PlayerTeam.class.getDeclaredField("h");
            _setSuffix = PlayerTeam.class.getDeclaredField("i");
            
            _setPrefix.setAccessible(true);
            _setSuffix.setAccessible(true);
            
            Field field = Class.forName("org.bukkit.craftbukkit.v1_17_R1.scoreboard.CraftTeam")
                    .getDeclaredField("team");
            field.setAccessible(true);
            
            _nmsTeam = (PlayerTeam) field.get(_team);
            _packetNmsTeam = (PlayerTeam) field.get(_packetTeam);
            
            writePacketOption();
        } catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
    
    private void writePacketOption()
    {
        int m = 0;
        
        //Bit mask. 0x01: Allow friendly fire, 0x02: can see invisible players on same team.
        //if can't see invisibles, enable friendly fire
        /*
        if (!_seeInvisibles)
            m |= 0x1;
         */
        _packetTeam.setAllowFriendlyFire(!_seeInvisibles);
        //if can see invisibles, enable seeing invisibles on same team.
        /*
        if (_seeInvisibles)
            m |= 0x2;
         */
        _packetTeam.setCanSeeFriendlyInvisibles(_seeInvisibles);
        
        //_teamPacket.getIntegers().write(1, m);
    }
    
    //sets the NMS team colour and sends packet
    //gets called in setPrefix
    public void setColor(String colorCode)
    {
        for (FakeTeam team : getChildren())
        {
            team.setColor(colorCode);
        }
        
        ChatFormatting enumChat = EnumChatFormatHelper.enumChatFromString(colorCode);
        
        _nmsTeam.setColor(enumChat);
        _packetNmsTeam.setColor(enumChat);
        
        //write/construct the packet and send it
        //_teamPacket.getModifier().write(6, enumChat);
        
        ClientboundSetPlayerTeamPacket teamPacket = getPacket();
        
        for(Player player : _fakeScoreboard.getPlayers())
        {
            UtilPlayer.sendPacket(player, teamPacket);
        }
        
        
        //ChatColor chatColor = ChatColor.getByChar(colorCode);
        
        //_team.setColor(chatColor);
        
        //System.out.println("setColor has run.");
    }
    
    public ClientboundSetPlayerTeamPacket getPacket()
    {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(_packetNmsTeam, false);
    }
    
    //send team packet to related players
    public void sendPacket()
    {
    	ClientboundSetPlayerTeamPacket teamPacket = getPacket();
        
        for(Player player : _fakeScoreboard.getPlayers())
        {
            UtilPlayer.sendPacket(player, teamPacket);
        }
    }
}
