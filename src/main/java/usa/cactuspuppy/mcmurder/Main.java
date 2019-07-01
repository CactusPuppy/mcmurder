package usa.cactuspuppy.mcmurder;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Getter
    private static Main instance;

    @Override
    public void onEnable() {
        long start = System.nanoTime();
        instance = this;
    }

    private boolean baseSetup() {
        //TODO
        return true;
    }
}
