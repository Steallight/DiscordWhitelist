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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
                            .addField("Plattform", plattform, false)
                            .addField("MC-Name", minecraftname, false)
                            .addBlankField(false)
                            .addField("User", addedMember.getAsMention(), false)
                            .addField("User-ID", UserId, false)
                            .setFooter("genehmigt von " + e.getMember().getEffectiveName());

                    new WhitelistPlayer().runTask(DiscordWhitelist.getPlugin());


                    insertMcBinder(DiscordWhitelist.getPlugin().database, UserId, minecraftname.toLowerCase());

                System.out.println("Eingetragen");

                    tc.sendMessageEmbeds(eb.build()).queue();
                    e.reply("Der User wurde gewhitelisted!").setEphemeral(true).queue();
                    e.getMessage().delete().queue();


                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        }
    }

    public class WhitelistPlayer extends BukkitRunnable {
        @Override
        public void run() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);

            player.setWhitelisted(true);
        }
    }

    public void insertMcBinder(LiteSQL sql, String UserID, String minecraftusername) throws SQLException {
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtInsertMcBinder = con.prepareStatement("INSERT INTO Whitelist(MCUsername, DCUserID) VALUES ('" + minecraftusername.toLowerCase() + "','" + UserID + "')");

        stmtInsertMcBinder.executeUpdate();
        stmtInsertMcBinder.close();
        con.close();
    }


}
