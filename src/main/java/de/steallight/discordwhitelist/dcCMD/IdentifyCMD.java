package de.steallight.discordwhitelist.dcCMD;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class IdentifyCMD extends ListenerAdapter {

    // Identifiziert einen Ingame-User und gibt den zugehörigen Discord User an
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {

        if (e.getName().equals("identify")) {
            try {


                String minecraftname = e.getOption("minecraftname").getAsString();
                String UserId = getUserID(DiscordWhitelist.getPlugin().database, minecraftname);


                Member taggedUser = e.getGuild().getMemberById(UserId);
                Collection<Button> identifyButtons = new ArrayList<>();
                identifyButtons.add(Button.primary("identifyDewhitelist_" + minecraftname, "DeWhitelist"));
                identifyButtons.add(Button.secondary("identifyKick_" + taggedUser.getId(), "Kick"));
                identifyButtons.add(Button.danger("identifyBan_" + taggedUser.getId() + "[" + minecraftname + "]","Ban"));

                if (taggedUser != null) {

                    //int UserId = getUserID(DiscordWhitelist.getPlugin().database, minecraftname.toLowerCase());

                    EmbedBuilder eb = new EmbedBuilder();
                    eb
                            .setTitle("User-Abfrage")
                            .setColor(Color.CYAN)
                            .setThumbnail(taggedUser.getEffectiveAvatarUrl())
                            .addField("Minecraft-Username", minecraftname, true)
                            .addField("Discord-ID", taggedUser.getId(), true)
                            .setFooter("abgefragt von " + e.getUser().getName(), e.getUser().getAvatarUrl());

                    e.replyEmbeds(eb.build()).addActionRow(identifyButtons).queue();

                }else {
                    e.reply("Der User steht nicht in der Datenbank!").setEphemeral(true).queue();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

    }


    public String getUserID(LiteSQL sql, String minecraftname) throws SQLException {
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtGetUserID = con.prepareStatement("SELECT DCUserID FROM Whitelist WHERE MCUsername = '" + minecraftname + "'");

        ResultSet resultSetUserID = stmtGetUserID.executeQuery();



        if (!resultSetUserID.next()) {
            return null;
        } else {

            String UserID = resultSetUserID.getString(1);


            stmtGetUserID.close();
            con.close();
            return UserID.trim();
        }

    }


}
