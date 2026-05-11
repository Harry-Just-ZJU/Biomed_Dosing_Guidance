package cn.edu.zju.dbutils;

import cn.edu.zju.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.function.Consumer;

public class DBUtils {
    private static final Logger log = LoggerFactory.getLogger(DBUtils.class);

    public static Connection getConnection() {
        AppConfig cfg = AppConfig.getInstance();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(cfg.getJdbcUrl(), cfg.getJdbcUsername(), cfg.getJdbcPassword());
        } catch (ClassNotFoundException | SQLException e) {
            log.error("DB connection failed: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to database: " + e.getMessage(), e);
        }
    }

    public static void execSQL(Consumer<Connection> consumer) {
        Connection conn = null;
        try {
            conn = getConnection();
            consumer.accept(conn);
        } catch (Exception e) {
            log.error("execSQL failed: {}", e.getMessage(), e);
            if (e instanceof RuntimeException) throw (RuntimeException) e;
            throw new RuntimeException(e);
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
