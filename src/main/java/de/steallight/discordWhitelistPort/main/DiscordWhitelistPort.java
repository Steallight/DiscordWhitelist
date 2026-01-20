package de.steallight.discordWhitelistPort.main;

import de.steallight.discordWhitelistPort.dcCMD.IdentifyCMD;
import de.steallight.discordWhitelistPort.dcCMD.WhitelistAdd;
import de.steallight.discordWhitelistPort.dcCMD.WhitelistRemove;
import de.steallight.discordWhitelistPort.listener.AutoCompleteListener;
import de.steallight.discordWhitelistPort.listener.ButtonHandler;
import de.steallight.discordWhitelistPort.mcCMD.MCWhitelist;
import de.steallight.discordWhitelistPort.messaging.MessageFormatter;
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

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class DiscordWhitelistPort extends JavaPlugin {

    public static JDA jda;
    private MessageFormatter messageFormatter;
    private static DiscordWhitelistPort INSTANCE;
    public LiteSQL database = null;

    public DiscordWhitelistPort(){

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

        try{
            messageFormatter = new MessageFormatter();
            getLogger().info("MessageFormatter geladen.");
        }catch (Exception e){
            getLogger().severe("MessageLogger fehlgeschlagen: " + e.getMessage());
            disablePluginGracefully("MessageFormatter Fehler");
            return;
        }

        if (getCommand("wl") != null) {
            getCommand("wl").setExecutor(new MCWhitelist());
            getCommand("wl").setTabCompleter(new MCWhitelist());
        }



        String guildID = getConfig().getString("GUILD_ID");

        String discordToken = getConfig().getString("BOT_TOKEN").trim();


        if (discordToken.isEmpty() || guildID.isEmpty()){
            getLogger().severe("BOT_TOKEN oder GUILD_ID fehlen in der config.yml!");
            disablePluginGracefully(messageFormatter.format(false, "error.no-token"));
            return;
        }

        try {
            jda = JDABuilder.create(discordToken,
                            GatewayIntent.GUILD_MEMBERS)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .build();
            Guild server = jda.awaitReady().getGuildById(guildID);


            if (server == null){
                throw new IllegalStateException("Guild ID '" + guildID + "' nicht gefunden!");
            }
            Bukkit.getConsoleSender().sendMessage(messageFormatter.format(true, "enable.enabled"));
            Bukkit.getConsoleSender().sendMessage(messageFormatter.format(true, "enable.bot-connected"));


            this.updateCommands(server);
            this.addEvents();

            try{
                database = new LiteSQL();
            }catch (Exception e){
                getLogger().warning("Datenbank nicht geladen: " + e.getMessage());
            }
        } catch (Exception e) {
            getLogger().severe("Discord-Init fehlgeschlagen: " + e.getMessage());
            disablePluginGracefully("Discord Fehler: " + e.getMessage());
            return;
        }
    }

    @Override
    public void onDisable() {

        // Plugin Shutdown Logic
        if (messageFormatter != null){
            messageFormatter = null;
        }

        if (jda != null){
            try {
                jda.shutdown();
                if (!jda.awaitShutdown(5, TimeUnit.SECONDS)){
                    jda.shutdownNow();
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            jda = null;
        }

        if (database != null){
            try {
                database.getConnection().close();
            }catch (Exception ignored){}
            database = null;
        }
        getLogger().info("DiscordWhitelistPort deaktiviert.");

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

    // Hilfsmethode für graceful Disable
    private void disablePluginGracefully(String reason) {
        getLogger().warning("Plugin deaktiviert: " + reason);
        Bukkit.getScheduler().runTask(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled(this)) {
                Bukkit.getPluginManager().disablePlugin(this);
            }
        });
    }

    public static DiscordWhitelistPort getPlugin() {
        return INSTANCE;
    }

    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }




}
