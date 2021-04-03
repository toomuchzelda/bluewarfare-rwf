package me.libraryaddict.arcade.forcekit;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;

import java.util.ArrayList;

public interface ForceKit {
    public ArrayList<Kit> getKits(GameTeam team);

    public String parse(String kits);
}
