package cn.edu.zju.dao;

import cn.edu.zju.bean.User;
import cn.edu.zju.dbutils.DBUtils;
import cn.edu.zju.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    /** Find user by username (case-insensitive). Returns null if not found. */
    public User findByUsername(String username) {
        AtomicReference<User> ref = new AtomicReference<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,username,password_hash,role,display_name," +
                    "created_at,last_login,active FROM app_user WHERE LOWER(username)=LOWER(?) AND active=1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ref.set(mapRow(rs));
                }
            } catch (SQLException e) {
                log.error("findByUsername failed: {}", e.getMessage(), e);
            }
        });
        return ref.get();
    }

    /** Verify credentials. Returns User on success, null on failure. */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user == null) return null;
        if (!PasswordUtil.verify(password, user.getPasswordHash())) return null;
        // Update last_login timestamp
        updateLastLogin(user.getId());
        return user;
    }

    /** Register a new user. Returns generated id, or -1 on failure. */
    public int register(String username, String password, String role, String displayName) {
        if (findByUsername(username) != null) return -2; // duplicate
        String hash = PasswordUtil.hash(password);
        AtomicReference<Integer> keyRef = new AtomicReference<>(-1);
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO app_user (username,password_hash,role,display_name,created_at,active) " +
                    "VALUES (?,?,?,?,?,1)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username.trim().toLowerCase());
                ps.setString(2, hash);
                ps.setString(3, role);
                ps.setString(4, displayName);
                ps.setTimestamp(5, new Timestamp(new Date().getTime()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) keyRef.set(rs.getInt(1));
                }
            } catch (SQLException e) {
                log.error("register failed: {}", e.getMessage(), e);
            }
        });
        return keyRef.get();
    }

    private void updateLastLogin(int userId) {
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE app_user SET last_login=? WHERE id=?")) {
                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                ps.setInt(2, userId);
                ps.executeUpdate();
            } catch (SQLException e) {
                log.warn("updateLastLogin failed: {}", e.getMessage());
            }
        });
    }

    private User mapRow(ResultSet rs) throws SQLException {
        Timestamp lastLogin = rs.getTimestamp("last_login");
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("display_name"),
                new Date(rs.getTimestamp("created_at").getTime()),
                lastLogin != null ? new Date(lastLogin.getTime()) : null,
                rs.getBoolean("active"));
    }
}
