package de.steallight.discordwhitelist.dcCMD;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WhitelistAdd extends ListenerAdapter {


    EmbedBuilder eb = new EmbedBuilder();

    String requestChannelID = DiscordWhitelist.getPlugin().getConfig().getString("REQUEST_CHANNEL_ID");

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent e) {
        if (e.getName().equals("whitelist")) {
            String minecraftname = e.getOption("minecraftname").getAsString();
            String plattform = e.getOption("platform").getAsString();
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftname);
            if (!player.isWhitelisted()) {
                TextChannel tc = e.getGuild().getTextChannelById(requestChannelID);

                eb
                        .setTitle("Neuer Whitelist-Request")
                        .setColor(Color.MAGENTA)
                        .addField("Plattform", plattform, false)
                        .addField("User-ID", e.getUser().getId(), false)
                        .addField("Minecraft-Name", minecraftname, false)
                        .setThumbnail(e.getUser().getAvatarUrl());


                Button acceptButton = Button.success("request-accept[" + minecraftname + "]_" + e.getUser().getId() + ";" + plattform, DiscordWhitelist.getPlugin().getMessageFormatter().format("button.accept"));
                Button denyButton = Button.danger("request-deny", DiscordWhitelist.getPlugin().getMessageFormatter().format("button.deny"));

                tc.sendMessageEmbeds(eb.build()).setActionRow(acceptButton, denyButton).queue();
                e.reply("Deine Request liegt dem Mod-Team vor!").setEphemeral(true).queue();

            }else {
                e.reply("Du bist schon gewhitelisted!").setEphemeral(true).queue();
            }

        }
    }


}
