package de.steallight.discordwhitelist.listener;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class ButtonHandler extends ListenerAdapter {

    public static String minecraftname;


    String logChannelID = DiscordWhitelist.getPlugin().getConfig().getString("LOG_CHANNEL_ID");

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent e) {
        if (e.getComponentId().startsWith("request-accept")) {
            String outputData = e.getComponentId();
            TextChannel tc = e.getGuild().getTextChannelById(logChannelID);
            minecraftname = "";
            String seperator = "-";
            String seperatorPlatform = ";";
            int startIndex = outputData.indexOf("[");
            int endIndex = outputData.indexOf("]");
            int startIndex2 = outputData.indexOf("]_");
            int endIndex2 = outputData.indexOf(";");

            int sepPlatformPos = outputData.indexOf(seperatorPlatform);
            String plattform = outputData.substring(sepPlatformPos + seperatorPlatform.length()).trim();
            String UserId = outputData.substring(startIndex2 + 2, endIndex2);
            minecraftname = e.getComponentId().substring(startIndex + 1, endIndex);

            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);
            Member addedMember = e.getGuild().retrieveMemberById(UserId).complete();

            if (!player.isWhitelisted()) {
                try {

                    EmbedBuilder eb = new EmbedBuilder();

                    eb
                            .setTitle("Whitelist-Log")
                            .setColor(Color.GREEN)
                            .setThumbnail(addedMember.getAvatarUrl())
                            .addField("Plattform", plattform, false)
                            .addField("MC-Name", minecraftname, false)
                            .addBlankField(false)
                            .addField("User", addedMember.getAsMention(), false)
                            .addField("User-ID", UserId, false)
                            .setFooter("genehmigt von " + e.getMember().getEffectiveName());

                    new WhitelistPlayer().runTask(DiscordWhitelist.getPlugin());


                    insertMcBinder(DiscordWhitelist.getPlugin().database, UserId, minecraftname);



                    tc.sendMessageEmbeds(eb.build()).queue();
                    e.reply("Der User wurde gewhitelisted!").setEphemeral(true).queue();
                    e.getMessage().delete().queue();


                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
            //Identify Kick Button Handling
        } else if (e.getComponentId().startsWith("identifyKick")) {
            String userId = e.getComponentId().substring(13);
            e.getGuild().kick(e.getGuild().getMemberById(userId)).queue();

            //Identify Ban Button Handling
        } else if (e.getComponentId().startsWith("identifyBan")) {
            String userId = e.getComponentId().substring(12);
            String ouputData = e.getComponentId();
            int startIndex = ouputData.indexOf("[");
            int endIndex = ouputData.indexOf("]");
            minecraftname = e.getComponentId().substring(startIndex + 1, endIndex);

            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);
            e.getGuild().ban(e.getGuild().getMemberById(userId), 10, TimeUnit.DAYS).queue();
            player.ban("", Instant.ofEpochSecond(0),"").save();


        } else if (e.getComponentId().startsWith("identifyDewhitelist")) {

            minecraftname = e.getComponentId().substring(20);

            new DeWhitlistPlayer().runTask(DiscordWhitelist.getPlugin());
            e.reply("Der User wurde von der Whitelist entfernt").setEphemeral(true).queue();
        }
    }

    public class WhitelistPlayer extends BukkitRunnable {
        @Override
        public void run() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);

            player.setWhitelisted(true);
        }
    }

    public class DeWhitlistPlayer extends BukkitRunnable{
        @Override
        public void run() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);

            player.setWhitelisted(false);
        }
    }



    public void insertMcBinder(LiteSQL sql, String UserID, String minecraftusername) throws SQLException {
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtInsertMcBinder = con.prepareStatement("INSERT INTO Whitelist(MCUsername, DCUserID) VALUES ('" + minecraftusername + "','" + UserID + "')");

        stmtInsertMcBinder.executeUpdate();
        stmtInsertMcBinder.close();
        con.close();
    }


}
