package de.steallight.discordwhitelist;

import de.steallight.discordwhitelist.dcCMD.IdentifyCMD;
import de.steallight.discordwhitelist.dcCMD.WhitelistAdd;
import de.steallight.discordwhitelist.listener.AutoCompleteListener;
import de.steallight.discordwhitelist.listener.ButtonHandler;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class DiscordWhitelist extends JavaPlugin {

    private JDA jda;

    private static DiscordWhitelist INSTANCE;

    public LiteSQL database = new LiteSQL();

    public DiscordWhitelist() throws SQLException, IOException {
    }

    public void config() {
        File c = new File("plugins/DiscordWhitelist", "config.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(c);
        if (!c.exists()) {
            cfg.options().copyDefaults(true);
            cfg.addDefault("BOT_TOKEN", "");
            cfg.addDefault("GUILD_ID", "");
            cfg.addDefault("LOG_CHANNEL_ID", "");
            cfg.addDefault("REQUEST_CHANNEL_ID", "");

        }
    }

    @Override
    public void onEnable() {
        config();
        saveDefaultConfig();
        INSTANCE = this;
        try {
            String guildID = getConfig().getString("GUILD_ID");
            String discordToken = getConfig().getString("BOT_TOKEN");

            Bukkit.getConsoleSender().sendMessage("§8[§c§bDiscordWhitelist§8] §7Plugin erfolgreich §aaktiviert!");

            if (discordToken == null) {
                getServer().shutdown();
                Bukkit.getConsoleSender().sendMessage("§cKein DiscordBot-Token vorhanden!");
            } else {
                this.jda = JDABuilder.createDefault(discordToken,
                                GatewayIntent.GUILD_MEMBERS)
                        .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                        .build();
                Guild server = jda.awaitReady().getGuildById(guildID);
                Bukkit.getConsoleSender().sendMessage("§8[§c§bDiscordWhitelist§8] §7Discord Bot §averbunden!");
                assert server != null;
                this.updateCommands(server);
                this.addEvents();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        String discordToken = getConfig().getString("BOT_TOKEN");
        if (discordToken == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§c§bDiscordWhitelist§8] §7Plugin erfolgreich §cdeaktiviert!");
        } else {
            jda.shutdown();
            Bukkit.getConsoleSender().sendMessage("§8[§c§bDiscordWhitelist§8] §7Plugin erfolgreich §cdeaktiviert!");
        }
    }

    public void addEvents() {
        jda.addEventListener(new AutoCompleteListener());
        jda.addEventListener(new ButtonHandler());
        jda.addEventListener(new WhitelistAdd());
        jda.addEventListener(new IdentifyCMD());

    }

    public void updateCommands(Guild server) {
        server.updateCommands()
                .addCommands(
                        Commands.slash("whitelist", "Frage einen Whitelist-Request an")
                                .addOption(OptionType.STRING, "minecraftname", "Gebe hier deinen Ingame Namen ein", true)
                                .addOption(OptionType.STRING, "platform", "Trage hier bitte deine Minecraft Plattform an", true, true)
                                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                                .setGuildOnly(true),

                        Commands.slash("identify","Frage den User vom Minecraft-Server ab")
                                .addOption(OptionType.STRING, "minecraftname", "Gebe den Ingame-Namen des Users ein", true, true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .setGuildOnly(true)

                ).queue();

    }


    public static DiscordWhitelist getPlugin() {
        return INSTANCE;
    }
}
