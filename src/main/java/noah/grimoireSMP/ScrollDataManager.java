package noah.grimoireSMP;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import noah.grimoireSMP.scroll.Scroll;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ScrollDataManager {
    private static ScrollDataManager instance;
    private final File file;
    private FileConfiguration config;

    private ScrollDataManager(JavaPlugin plugin) {
        file = new File(plugin.getDataFolder(), "scrolls.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reloadConfig();
    }

    public static ScrollDataManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ScrollDataManager(plugin);
        }
        return instance;
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getScrollType(UUID uuid) {
        return config.getString("players." + uuid.toString() + ".scrollType", null);
    }

    public int getRuneEssence(UUID uuid) {
        return config.getInt("players." + uuid.toString() + ".runeEssence", Scroll.START_RUNE_ESSENCE);
    }

    public boolean getGrimoire(UUID uuid) {
        return config.getBoolean("players." + uuid.toString() + ".grimoire", false);
    }

    public void setPlayerData(UUID uuid, String scrollType, int runeEssence, boolean grimoire) {
        config.set("players." + uuid.toString() + ".scrollType", scrollType);
        config.set("players." + uuid.toString() + ".runeEssence", runeEssence);
        config.set("players." + uuid.toString() + ".grimoire", grimoire);
    }
}
