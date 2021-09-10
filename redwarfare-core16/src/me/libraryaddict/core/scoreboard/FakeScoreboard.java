package me.libraryaddict.core.scoreboard;

import me.libraryaddict.core.utils.UtilPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FakeScoreboard {
    private static char[] _chars = "abcdefghijklmno".toCharArray();
    private ArrayList<FakeScoreboard> _children = new ArrayList<>();
    private HashMap<DisplaySlot, String> _displayNames = new HashMap<>();
    private String _name;
    private HashMap<DisplaySlot, HashMap<String, Integer>> _objectives = new HashMap<>();
    private int _previousSidebar;
    private Scoreboard _scoreboard;
    private HashMap<String, FakeTeam> _teams = new HashMap<>();
    private Predicate<Player> _whoCanView;

    public FakeScoreboard(String name) {
        this(name, (Player player) -> true);
    }

    public FakeScoreboard(String name, Player player) {
        this(name, (input) -> input == player);
    }

    public FakeScoreboard(String name, Predicate<Player> predicate) {
        _name = name;
        _whoCanView = predicate;

        _scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        for (char c : _chars) {
            FakeTeam team = createTeam(getTeamName(c));

            team.addPlayer(getTeamName(c));
        }
    }

    public void addChild(FakeScoreboard board) {
        _children.add(board);

        _displayNames.forEach((slot, value) -> board.setDisplayName(slot, value));

        _objectives.forEach((slot, value) -> value.forEach((name, score) -> board.makeScore(slot, name, score)));

        for (FakeTeam fakeTeam : _teams.values()) {
            board.addTeam(fakeTeam.clone(board));
        }

        getPlayers().stream().filter((player) -> player.getScoreboard() == getScoreboard() && board.isApplicable(player))
                .forEach((player) -> board.setScoreboard(player));
    }
    
    public void addTeam(FakeTeam team) {
        _teams.put(team.getTeamName(), team);
    }

    public FakeTeam createTeam(String name) {
        getChildren().forEach((child) -> child.createTeam(name));

        FakeTeam team = new FakeTeam(this, name);

        _teams.put(name, team);

        team.setupTeam(getScoreboard());

        team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);

        return team;
    }

    public ArrayList<Player> getApplicable() {
//        ArrayList<Player> boards = new ArrayList<>();

        ArrayList<Player> players = UtilPlayer.getPlayers();

//        players.stream().filter(_whoCanView).forEach((player) -> boards.add(player));

        return players.stream().filter(_whoCanView).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<FakeScoreboard> getChildren() {
        return _children;
    }
    
    public FakeScoreboard getChild(String name)
    {
    	for(FakeScoreboard board : _children)
    	{
    		if(board.getName().equals(name))
    			return board;
    	}
    	return null;
    }

    public Collection<FakeTeam> getFakeTeams() {
        return _teams.values();
    }

    public String getName() {
        return _name;
    }

    private Objective getObjective(DisplaySlot slot) {
        Objective objective;
        Scoreboard board = getScoreboard();

        if (board.getObjective(slot) == null) {
            objective = board.registerNewObjective(slot.name(), slot.name());
            objective.setDisplaySlot(slot);
        } else {
            objective = board.getObjective(slot);
        }

        return objective;
    }

    public ArrayList<Player> getPlayers() {
        ArrayList<Player> players = UtilPlayer.getPlayers();

        return players.stream().filter(_whoCanView).collect(Collectors.toCollection(ArrayList::new));
    }

    public int getScore(DisplaySlot slot, String name) {
        if (!_objectives.containsKey(slot) || !_objectives.get(slot).containsKey(name))
            return 0;

        return _objectives.get(slot).get(name);
    }

    public Scoreboard getScoreboard() {
        return _scoreboard;
    }

    public void setScoreboard(Player player) {
        for (FakeScoreboard fakeBoard : _children) {
            if (!fakeBoard.isApplicable(player))
                continue;

            fakeBoard.setScoreboard(player);
            return;
        }

        player.setScoreboard(getScoreboard());
    }

    public FakeScoreboard getSubScoreboard(Player player) {
        Optional<FakeScoreboard> opt = getChildren().stream().filter(board -> board._whoCanView.test(player)).findAny();

        return opt.orElse(this);
    }

    public FakeTeam getTeam(String name) {
        return _teams.get(name);
    }

    protected ArrayList<FakeTeam> getTeamChildren(String name) {
        return (ArrayList<FakeTeam>) getChildren().stream().map((board) -> board.getTeam(name)).filter((team) -> team != null)
                .collect(Collectors.toList());
    }

    private String getTeamName(char c) {
        return "" + ChatColor.COLOR_CHAR + c + ChatColor.RESET;
    }

    public void hideScore(DisplaySlot slot, String name) {
        getChildren().forEach((child) -> child.hideScore(slot, name));

        if (!_objectives.containsKey(slot)) {
            return;
        }

        if (!_objectives.get(slot).containsKey(name)) {
            return;
        }

        _objectives.get(slot).remove(name);

        getScoreboard().resetScores(name);

        if (_objectives.get(slot).isEmpty()) {
            _objectives.remove(slot);
        }
    }

    public boolean isApplicable(Player player) {
        return _whoCanView.test(player);
    }

    public void makeScore(DisplaySlot slot, String name, int score) {
        makeScore(slot, name, null, score);
    }

    public void makeScore(DisplaySlot slot, String name, String displayName, int score) {
        for (FakeScoreboard child : _children) {
            child.makeScore(slot, name, displayName, score);
        }

        if (_objectives.containsKey(slot) && _objectives.get(slot).containsKey(name) && _objectives.get(slot).get(name) == score)
            return;

        // cringe
//        if (!_objectives.containsKey(slot)) {
//            _objectives.put(slot, new HashMap<>());
//        }
//        _objectives.get(slot).put(name, score);
        _objectives.computeIfAbsent(slot, ignored -> new HashMap<>()).put(name, score);
        
        Objective obj = getObjective(slot);

        obj.getScore(name).setScore(score);

        if (displayName != null) {
            obj.setDisplayName(displayName);
        }
    }

    private String[] prune(String string) {
        String[] lines = new String[]
                {
                        string.substring(0, Math.min(16, string.length())), ""
                };

        if (string.length() > 16) {
            if (lines[0].endsWith("" + ChatColor.COLOR_CHAR))
                lines[0] = lines[0].substring(0, 15);

            lines[1] = ChatColor.getLastColors(lines[0]) + string.substring(lines[0].length());

            if (lines[1].length() > 16)
                lines[1] = lines[1].substring(0, 16);
        }

        return lines;
    }

    public void setDisplayName(DisplaySlot slot, String name) {
        for (FakeScoreboard child : _children) {
            child.setDisplayName(slot, name);
        }

        if (name.length() > 16)
            name = name.substring(0, 16);

        if (_displayNames.containsKey(slot) && Objects.equals(_displayNames.get(slot), name))
            return;

        _displayNames.put(slot, name);

        getObjective(slot).setDisplayName(name);
    }

    public void setSidebar(Collection<String> lines) {
        setSidebar(lines.toArray(new String[0]));
    }

    public void setSidebar(String[] lines) {
        for (int i = 0; i < Math.max(lines.length, _previousSidebar); i++) {
            if (_chars.length <= i)
                break;

            String teamName = getTeamName(_chars[i]);

            FakeTeam team = getTeam(teamName);

            if (lines.length > i) {
                String[] text = prune(lines[i]);

                team.setPrefix(text[0]);
                team.setSuffix(text[1]);

                makeScore(DisplaySlot.SIDEBAR, teamName, 0);
            } else {
                hideScore(DisplaySlot.SIDEBAR, teamName);
            }
        }

        _previousSidebar = lines.length;
    }

    public void setSidebarTitle(String title) {
        setDisplayName(DisplaySlot.SIDEBAR, title);
    }
}
