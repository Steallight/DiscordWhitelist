package de.steallight.discordWhitelistPort.messaging;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.Objects;

public class MessageFile {

    private final FileConfiguration config;

    public MessageFile(String name){
        this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream(name))));
    }

    public FileConfiguration getConfig(){
        return config;
    }

    public String get(String key){
        return config.getString(key);
    }

}
