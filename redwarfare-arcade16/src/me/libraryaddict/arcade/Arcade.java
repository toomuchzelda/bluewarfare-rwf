package me.libraryaddict.arcade;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.utils.UtilFile;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Arcade extends JavaPlugin {
    private ArcadeManager _arcadeManager;

    @Override
    public void onDisable() {
        WorldData data;
        
        //if loaded world is a game world (snd/hg map)
        if ((data = _arcadeManager.getWorld().getData()) != null)
        {
        	Bukkit.unloadWorld(_arcadeManager.getWorld().getGameWorld(), false);
        	String name = data.getWorldFolder().getAbsolutePath();
            UtilFile.delete(data.getWorldFolder());
            this.getLogger().info("Deleted file " + name);
        }
    }

    @Override
    public void onEnable() {
    	//1.16 worlds couldn't have '_' in the name iirc so i filtered those out.
    	// now use a different identifier so they will be detected and deleted by this
    	// this is assuming this was only made for cleaning up temporary maps and not any other files
    	
        for (File file : new File("Test").getAbsoluteFile().getParentFile().listFiles()) {
            String name = file.getName();
            //if (!name.contains("_"))
            //   continue;
            if(!name.startsWith("CACHEDMAP"))
            	continue;

            /*
            try {
                Long.parseLong(name.split("_")[1]);
            } catch (Exception ex) {
                continue;
            }
            */
            
            UtilFile.delete(file);
            this.getLogger().info("Deleted file " + name);
        }

        _arcadeManager = new ArcadeManager(this);
    }
}
