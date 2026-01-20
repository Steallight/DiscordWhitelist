package de.steallight.discordWhitelistPort.dcCMD;

import de.steallight.discordWhitelistPort.main.DiscordWhitelistPort;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import net.dv8tion.jda.api.components.buttons.Button;

import java.awt.*;

public class WhitelistAdd extends ListenerAdapter {

    EmbedBuilder eb = new EmbedBuilder();

    String requestChannelID = DiscordWhitelistPort.getPlugin().getConfig().getString("REQUEST_CHANNEL_ID");


    // Handler für den Whitelist Request zum hinzufügen eines Spielers auf die Whitelist
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


                Button acceptButton = Button.success("request-accept[" + minecraftname + "]_" + e.getUser().getId() + ";" + plattform, DiscordWhitelistPort.getPlugin().getMessageFormatter().format( false,"button.accept"));
                Button denyButton = Button.danger("request-deny", DiscordWhitelistPort.getPlugin().getMessageFormatter().format(false, "button.deny"));

                tc.sendMessageEmbeds(eb.build()).addComponents(ActionRow.of(acceptButton, denyButton)).queue();
                e.reply("Deine Request liegt dem Mod-Team vor!").setEphemeral(true).queue();

            }else {
                e.reply("Du bist schon gewhitelisted!").setEphemeral(true).queue();
            }

        }
    }

}
