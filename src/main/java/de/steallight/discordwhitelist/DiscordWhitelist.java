package de.steallight.discordwhitelist;

import net.dv8tion.jda.api.JDA;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DiscordWhitelist extends JavaPlugin {

    private JDA jda;

    private static DiscordWhitelist INSTANCE;

    public void config(){
        File c = new File("plugins/DiscordWhitelist", "config.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(c);
        if (!c.exists()){
            cfg.options().copyDefaults(true);
            cfg.addDefault("BOT_TOKEN", "");

        }
    }

    @Override
    public void onEnable() {
        config();
        saveDefaultConfig();
    INSTANCE = this;
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static DiscordWhitelist getPlugin(){
        return INSTANCE;
    }
}
