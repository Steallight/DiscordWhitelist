package de.steallight.discordwhitelist.dcCMD;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WhitelistRemove extends ListenerAdapter {

    public static String minecraftname;
    EmbedBuilder eb = new EmbedBuilder();
    String logChannelID = DiscordWhitelist.getPlugin().getConfig().getString("LOG_CHANNEL_ID");

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (e.getName().equals("remove")) {
            minecraftname = e.getOption("minecraftname").getAsString();
            TextChannel tc = e.getGuild().getTextChannelById(logChannelID);
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);

            if (player.isWhitelisted()) {
                try {
                    removeUser(DiscordWhitelist.getPlugin().database, minecraftname);
                    new DeWhitelistPlayer().runTask(DiscordWhitelist.getPlugin());

                    eb
                            .setTitle("Der User wurde aus der Whitelist entfernt")
                            .setColor(Color.GREEN);

                    e.replyEmbeds(eb.build()).setEphemeral(true).queue();
                    writeLog(eb,tc,minecraftname,e.getMember());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            } else {
                e.reply("Der Spieler ist nicht gewhitelisted!").setEphemeral(true).queue();
            }
        }
    }


    public void removeUser(LiteSQL sql, String minecraftname) throws SQLException {

        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtRemoveUser = con.prepareStatement("DELETE FROM Whitelist WHERE MCUsername = '" + minecraftname + "'");
        stmtRemoveUser.executeUpdate();
        stmtRemoveUser.close();
        con.close();

    }

    public static class DeWhitelistPlayer extends BukkitRunnable {
        @Override
        public void run() {
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);

            player.setWhitelisted(false);
        }
    }

    public void writeLog(EmbedBuilder eb, TextChannel tc, String minecraftname, Member modUser) {
        eb
                .setTitle("User entfernt")
                .setColor(Color.RED)
                .addField("MC-Name", minecraftname, false)
                .setFooter("entfernt von " + modUser.getEffectiveName());


        tc.sendMessageEmbeds(eb.build()).queue();
    }


}
