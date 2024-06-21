package de.steallight.discordwhitelist.mcCMD;

import de.steallight.discordwhitelist.DiscordWhitelist;
import de.steallight.discordwhitelist.messaging.MessageFormatter;
import de.steallight.discordwhitelist.utils.LiteSQL;
import net.dv8tion.jda.api.entities.Message;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MCWhitelist implements CommandExecutor, TabCompleter {


    MessageFormatter msgFormat = DiscordWhitelist.getPlugin().getMessageFormatter();


    // Ingame Command für das Whitelisten eines Spielers
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase("wl")) {
                Player p = (Player) sender;
                if (sender.isOp()) {

                    String subcommand = args[0].toLowerCase();
                    String minecraftUsername = args[1];
                        OfflinePlayer whitelistPlayer = Bukkit.getOfflinePlayer(minecraftUsername);

try {


    switch (subcommand) {
        case "add" -> {
            if (!whitelistPlayer.isWhitelisted()) {


                String dcUserID = args[2];
                Bukkit.getOfflinePlayer(minecraftUsername).setWhitelisted(true);
                addWhitelist(DiscordWhitelist.getPlugin().database, dcUserID, minecraftUsername);
                p.sendMessage(msgFormat.format(true, "success.whitelist-added"));
            }else {
                p.sendMessage(DiscordWhitelist.getPlugin().getMessageFormatter().format(true, "whitelist.already-whitelisted"));
            }
        }

        case "remove" -> {
            if (whitelistPlayer.isWhitelisted()) {

                Bukkit.getOfflinePlayer(minecraftUsername).setWhitelisted(false);
                removeWhitelist(DiscordWhitelist.getPlugin().database, minecraftUsername);
                p.sendMessage(msgFormat.format(true, "success.whitelist-removed"));
            }else {
                p.sendMessage(msgFormat.format(true, "whitelist.not-whitelisted"));
            }
        }
    }
}catch (SQLException ex){
    throw new RuntimeException(ex);
}

                }else {
                    p.sendMessage(DiscordWhitelist.getPlugin().getMessageFormatter().format(true, "error.no-permissions"));
                }
            }
        }
        return false;
    }


    // Methode um einen Spieler in die Whitelist und in die Datenbank aufzunehmen
    public void addWhitelist(LiteSQL sql, String UserID, String minecraftusername) throws SQLException {
        sql.getConnection().close();
        Connection con = sql.getConnection();
        PreparedStatement stmtInsertWhitelist = con.prepareStatement("INSERT INTO Whitelist(MCUsername, DCUserID) VALUES ('" + minecraftusername + "','" + UserID + "')");

        stmtInsertWhitelist.executeUpdate();
        stmtInsertWhitelist.close();
        con.close();

    }

    // Methode um einen Spieler von der Whitelist und der Datebank zu entfernen
    public void removeWhitelist(LiteSQL sql, String minecraftusername) throws SQLException{
            sql.getConnection().close();
            Connection con = sql.getConnection();
            PreparedStatement stmtRemoveWhitelist = con.prepareStatement("DELETE FROM Whitelist WHERE MCUsername = '" + minecraftusername + "'");

            stmtRemoveWhitelist.executeUpdate();
            stmtRemoveWhitelist.close();
            con.close();
    }


    @Nullable
    @Override
    // AutoComplete Handler
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.isOp()){
            String[] subcmds = {"add", "remove"};
            ArrayList<String> tabComplete = new ArrayList<>();
            if (args.length == 0) return tabComplete;
            if (args.length == 1){
                return Arrays.asList(subcmds);
            }
        }
        return null;
    }
}
