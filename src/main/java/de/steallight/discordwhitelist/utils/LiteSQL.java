package de.steallight.discordwhitelist.utils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LiteSQL {

    private final String url;

    public LiteSQL() throws IOException, SQLException {
        final File file = new File( "datenbank.db");
        if (!file.exists()){
            file.createNewFile();
        }

        this.url = "jdbc:sqlite:" + file.getPath();
        createTables();

    }


    public Connection getConnection() throws SQLException{
        Connection connection = DriverManager.getConnection(url);
        System.out.println("Verbindung wurde hergestellt!");
        return connection;
    }

    private void createTables() throws SQLException{
        Connection con = getConnection();

        PreparedStatement mcBinder = con.prepareStatement("CREATE TABLE IF NOT EXISTS Whitelist(MCUsername TEXT NOT NULL, DCUserID INTEGER NOT NULL)");

        mcBinder.execute();
        mcBinder.close();
        con.close();
    }
}
