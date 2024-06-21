package de.steallight.discordwhitelist.listener;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoCompleteListener extends ListenerAdapter {

    private final String[] platform = new String[]{
            "Java",
            "Bedrock"
    };

    // AutoComplete Listener | Autovervollständigung für Ingame Commands
    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent e) {
        if (e.getName().equals("whitelist") && e.getFocusedOption().getName().equals("platform")) {
            List<Command.Choice> platformOptions = Stream.of(platform)
                    .filter(plattform -> plattform.startsWith(e.getFocusedOption().getValue()))
                    .map(plattform -> new Command.Choice(plattform, plattform))
                    .collect(Collectors.toList());
            e.replyChoices(platformOptions).queue();
        } else if (e.getName().equals("identify") && e.getFocusedOption().getName().equals("minecraftname")) {
            try {
                List<Command.Choice> whitelistPlayersOptions = getWhitelistedPlayers(DiscordWhitelist.getPlugin().database)
                        .stream().filter(players -> players.startsWith(e.getFocusedOption().getValue()))
                        .map(players -> new Command.Choice(players, players))
                        .collect(Collectors.toList());

                e.replyChoices(whitelistPlayersOptions).queue();


            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }


        } else if (e.getName().equals("remove") && e.getFocusedOption().getName().equals("minecraftname")) {
            try {
                List<Command.Choice> whitelistPlayerOptions = getWhitelistedPlayers(DiscordWhitelist.getPlugin().database)
                        .stream().filter(players -> players.startsWith(e.getFocusedOption().getValue()))
                        .map(players -> new Command.Choice(players,players))
                        .collect(Collectors.toList());

                e.replyChoices(whitelistPlayerOptions).queue();
            }catch (SQLException error){
                throw new RuntimeException(error);
            }


    }

}


    public List<String> getWhitelistedPlayers(LiteSQL sql) throws SQLException {
        ArrayList<String> tabComplete = new ArrayList<>();
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtWhitelistedPlayers = con.prepareStatement("SELECT MCUsername FROM Whitelist");

        ResultSet resultSetWhitelistPlayer = stmtWhitelistedPlayers.executeQuery();
        if (resultSetWhitelistPlayer != null) {
            int indexCount = 1;
            while (resultSetWhitelistPlayer.next()) {

                String entry = resultSetWhitelistPlayer.getString(indexCount);


                tabComplete.add(entry);


            }


        }

        stmtWhitelistedPlayers.close();
        con.close();

        return tabComplete;
    }
}
