package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class DatabaseConnection {
    public static Connection getConnection() throws SQLException{
        Properties properties = new Properties();

        try(InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("config.properties")){
            if(input==null){
                throw new SQLException("I didn't find config.properties");
            }
            properties.load(input);
        }catch(IOException e){
            throw new SQLException("Error reading the configuration", e);
        }

        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url,user,password);
    }
}

