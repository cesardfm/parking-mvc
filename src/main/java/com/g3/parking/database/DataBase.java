package com.g3.parking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;

public class DataBase {
    private static DataBase instance;
    private Connection driverManager;

    // Inyectar propiedades desde Spring
    @Value("${spring.datasource.url}")
    private String url;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver}")
    private String driver;

    public Connection getConnection() throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found.", e);
        }
        
        try {
            if (driverManager == null || driverManager.isClosed()) {
                // Usar las propiedades inyectadas
                driverManager = DriverManager.getConnection(url, username, password);
            }
            return driverManager;
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
}
