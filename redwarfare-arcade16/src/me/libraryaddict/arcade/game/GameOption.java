package me.libraryaddict.arcade.game;

import me.libraryaddict.core.Pair;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;

public class GameOption<Y> {
    public static GameOption<Boolean> ALLOW_ANVIL = new GameOption<>(false);
    public static GameOption<Boolean> ALLOW_CRAFTING = new GameOption<>(false);
    public static GameOption<Boolean> ATTACK_NON_TEAM = new GameOption<>(true);
    public static GameOption<Boolean> ATTACK_TEAM = new GameOption<>(true);
    public static GameOption<Boolean> BLOCK_BURN = new GameOption<>(false);
    public static GameOption<Boolean> BLOCK_IGNITE = new GameOption<>(false);
    public static GameOption<Boolean> BORDER_BLOCKS = new GameOption<>(false);
    public static GameOption<Boolean> BREAK_BLOCK = new GameOption<>(false);
    public static GameOption<Boolean> BREAK_GRASS = new GameOption<>(false);
    public static GameOption<Boolean> CHEST_LOOT = new GameOption<>(false);
    public static GameOption<Boolean> COLOR_CHAT_NAMES = new GameOption<>(false);
    public static GameOption<Boolean> DAMAGE_NON_LIVING = new GameOption<>(false);
    public static GameOption<Boolean> DEAD_BODIES = new GameOption<>(false);
    public static GameOption<Boolean> DEATH_ITEMS = new GameOption<>(false);
    public static GameOption<Boolean> DEATH_MESSAGES = new GameOption<>(true);
    public static GameOption<Boolean> DEATH_OUT = new GameOption<>(true);
    public static GameOption<Boolean> EXPLORE_PREGAME = new GameOption<>(true);
    public static GameOption<Boolean> FLYING_PREGAME = new GameOption<>(true);
    public static GameOption<Boolean> FORCE_RESOURCE_PACK = new GameOption<>(false);
    public static GameOption<Pair<Sound, Float>> GAME_START_SOUND = new GameOption<>(
            Pair.of(Sound.BLOCK_NOTE_BLOCK_HARP, 0F));
    public static GameOption<Boolean> HATS = new GameOption<>(false);
    public static GameOption<Boolean> HAY_BALE_BREAKS_FALL = new GameOption<>(false);
    public static GameOption<Boolean> HUNGER = new GameOption<>(false);
    public static GameOption<Boolean> INFORM_KILL_ASSIST = new GameOption<>(false);
    public static GameOption<Boolean> INTERACT_DECORATIONS = new GameOption<>(false);
    public static GameOption<Boolean> ITEMS_SPAWN = new GameOption<>(false);
    public static GameOption<Boolean> KILLER_HEALTH = new GameOption<>(true);
    public static GameOption<Boolean> KILLS_IN_TAB = new GameOption<>(false);
    public static GameOption<Boolean> LOCK_TO_SPAWN = new GameOption<>(true);
    public static GameOption<Pair<Integer, Integer>> LOOT_AMOUNT = new GameOption<>(Pair.of(4, 8));
    public static GameOption<Boolean> MAP_VOTE = new GameOption<>(true);
    public static GameOption<Boolean> NATURAL_MOBS = new GameOption<>(false);
    public static GameOption<Boolean> OPEN_CHEST = new GameOption<>(false);
    public static GameOption<Boolean> PICKUP_ITEM = new GameOption<>(false);
    public static GameOption<Material[]> PLACABLE_BLOCKS = new GameOption<>(new Material[0]);
    public static GameOption<Boolean> PLACE_BLOCK = new GameOption<>(false);
    public static GameOption<Pair<Sound, Float>> PLAYER_DEATH_SOUND = new GameOption<>(null);
    public static GameOption<Boolean> PLAYER_DROP_ITEM = new GameOption<>(false);
    public static GameOption<Boolean> PUSH = new GameOption<>(false);
    public static GameOption<Boolean> REMOVE_SEEDS_DROP = new GameOption<>(false);
    public static GameOption<String> RESOURCE_PACK = new GameOption<>(null);
    public static GameOption<Boolean> SERVER_HANDLES_WORLDS = new GameOption<>(true);
    public static GameOption<Boolean> SPEC_CLICK_INFO = new GameOption<>(false);
    public static GameOption<Double> STEAK_HEALTH = new GameOption<>(0D);
    public static GameOption<Boolean> TABLIST_KILLS = new GameOption<>(false);
    public static GameOption<Boolean> TEAM_HOTBAR = new GameOption<>(false);
    public static GameOption<Integer> TIME_CYCLE = new GameOption<>(0);
    public static GameOption<Long> TIME_OF_WORLD = new GameOption<>(0L);
    public static GameOption<Boolean> UNBREAKABLE = new GameOption<>(true);
    public static GameOption<Boolean> WEATHER = new GameOption<>(false);
    public static GameOption<Boolean> REGENERATION = new GameOption<>(true);
    private static ArrayList<GameOption<?>> values;
    private final Y _default;

    private GameOption(Y defaultValue) {
        _default = defaultValue;

        if (values == null) values = new ArrayList<>();
        values.add(this);
    }

    public static GameOption<?>[] values() {
        return values.toArray(new GameOption[0]);
    }

    public Y getDefault() {
        return _default;
    }

}
