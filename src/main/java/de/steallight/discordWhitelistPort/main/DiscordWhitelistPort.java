package de.steallight.discordWhitelistPort.main;

import de.steallight.discordWhitelistPort.dcCMD.IdentifyCMD;
import de.steallight.discordWhitelistPort.dcCMD.WhitelistAdd;
import de.steallight.discordWhitelistPort.dcCMD.WhitelistRemove;
import de.steallight.discordWhitelistPort.listener.AutoCompleteListener;
import de.steallight.discordWhitelistPort.listener.ButtonHandler;
import de.steallight.discordWhitelistPort.mcCMD.MCWhitelist;
import de.steallight.discordWhitelistPort.utils.LiteSQL;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import de.steallight.discordWhitelistPort.messaging.MessageFormatter;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public final class DiscordWhitelistPort extends JavaPlugin {

    public static JDA jda;
    private MessageFormatter messageFormatter;
    private static DiscordWhitelistPort INSTANCE;
    public LiteSQL database = new LiteSQL();

    public DiscordWhitelistPort() throws SQLException, IOException {

    }

    // Definieren der Konfigurationsdatei für den Plugin Ordner
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

        // Plugin startup logic

        messageFormatter = new de.steallight.discordWhitelistPort.messaging.MessageFormatter();

        INSTANCE = this;

        config();


        saveDefaultConfig();




        getCommand("wl").setExecutor(new MCWhitelist());
        getCommand("wl").setTabCompleter(new MCWhitelist());


        try {
            String guildID = getConfig().getString("GUILD_ID");

            String discordToken = getConfig().getString("BOT_TOKEN").trim();



            Bukkit.getConsoleSender().sendMessage(DiscordWhitelistPort.getPlugin().messageFormatter.format("enable.enabled"));

            if (discordToken.equals("")) {
                getServer().getPluginManager().disablePlugin(this);
                Bukkit.getConsoleSender().sendMessage(messageFormatter.format(false, "error.no-token"));
            } else {
                jda = JDABuilder.create(discordToken,
                                GatewayIntent.GUILD_MEMBERS)
                        .enableCache(CacheFlag.MEMBER_OVERRIDES)
                        .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                        .build();
                Guild server = jda.awaitReady().getGuildById(guildID);
                Bukkit.getConsoleSender().sendMessage(messageFormatter.format("enable.bot-connected"));
                assert server != null;
                this.updateCommands(server);
                this.addEvents();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




    }

    @Override
    public void onDisable() {

        // Plugin Shutdown Logic

        String discordToken = getConfig().getString("BOT_TOKEN");
        if (discordToken.equals("")) {
            Bukkit.getConsoleSender().sendMessage(messageFormatter.format("disable.disabled"));
            getServer().getPluginManager().disablePlugin(this);

        } else {

            Bukkit.getConsoleSender().sendMessage(messageFormatter.format("disable.disabled"));

        }
        messageFormatter = null;
        jda.shutdown();
    }

    // Methode um EventListener zu registrieren
    public void addEvents() {
        jda.addEventListener(new AutoCompleteListener());
        jda.addEventListener(new ButtonHandler());
        jda.addEventListener(new WhitelistAdd());
        jda.addEventListener(new WhitelistRemove());
        jda.addEventListener(new IdentifyCMD());

    }

    // Methode um Discord Commands zu registrieren
    public void updateCommands(Guild server) {
        server.updateCommands()
                .addCommands(
                        Commands.slash("whitelist", "Frage einen Whitelist-Request an")
                                .addOption(OptionType.STRING, "minecraftname", "Gebe hier deinen Ingame Namen ein", true)
                                .addOption(OptionType.STRING, "platform", "Trage hier bitte deine Minecraft Plattform an", true, true)
                                .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                                .setContexts(InteractionContextType.GUILD),

                        Commands.slash("identify", "Frage den User vom Minecraft-Server ab")
                                .addOption(OptionType.STRING, "minecraftname", "Gebe den Ingame-Namen des Users ein", true, true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .setContexts(InteractionContextType.GUILD),

                        Commands.slash("remove", "Entferne einen Spieler aus der Whitelist")
                                .addOption(OptionType.STRING, "minecraftname", "Gebe hier den Ingame-Namen an", true, true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                                .setContexts(InteractionContextType.GUILD)

                ).queue();

    }


    public static DiscordWhitelistPort getPlugin() {
        return INSTANCE;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }




}
