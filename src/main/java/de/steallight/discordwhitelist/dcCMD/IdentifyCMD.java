package de.steallight.discordwhitelist.dcCMD;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdentifyCMD extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {

        if (e.getName().equals("identify")) {
            try {


                String minecraftname = e.getOption("minecraftname").getAsString();
                int UserId = getUserID(DiscordWhitelist.getPlugin().database, minecraftname.toLowerCase());
           //     Member taggedUser = e.getGuild().retrieveMemberById(UserId).complete();
                EmbedBuilder eb = new EmbedBuilder();
                eb
                        .setTitle("User-Abfrage")
                        .setColor(Color.CYAN)
                       // .setThumbnail(taggedUser.getAvatarUrl())
                        .addField("Minecraft-Username", minecraftname, true)
                       // .addField("Discord-ID", taggedUser.getId() ,true)
                        .setFooter("abgefragt von " + e.getUser().getName(), e.getUser().getAvatarUrl());

                e.replyEmbeds(eb.build()).queue();


            }catch (SQLException ex){
                ex.printStackTrace();
            }
        }

    }


    public int getUserID(LiteSQL sql, String minecraftname)throws SQLException {
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtGetUserID = con.prepareStatement("SELECT * FROM Whitelist WHERE MCUsername='" + minecraftname + "'");

        ResultSet resultSetUserID = stmtGetUserID.executeQuery();

        int UserID = resultSetUserID.getInt(1);

        stmtGetUserID.close();
        con.close();
        return UserID;

    }
}
