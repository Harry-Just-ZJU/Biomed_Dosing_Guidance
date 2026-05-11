package cn.edu.zju.bean;

import java.util.Date;

/**
 * Represents a registered user.
 * role: "patient" | "admin"
 */
public class User {
    private int    id;
    private String username;
    private String passwordHash;   // BCrypt hash
    private String role;           // "patient" | "admin"
    private String displayName;
    private Date   createdAt;
    private Date   lastLogin;
    private boolean active;

    public User() {}

    public User(int id, String username, String passwordHash,
                String role, String displayName,
                Date createdAt, Date lastLogin, boolean active) {
        this.id           = id;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.displayName  = displayName;
        this.createdAt    = createdAt;
        this.lastLogin    = lastLogin;
        this.active       = active;
    }

    public int     getId()            { return id; }
    public void    setId(int id)      { this.id = id; }
    public String  getUsername()      { return username; }
    public void    setUsername(String u) { this.username = u; }
    public String  getPasswordHash()  { return passwordHash; }
    public void    setPasswordHash(String h) { this.passwordHash = h; }
    public String  getRole()          { return role; }
    public void    setRole(String r)  { this.role = r; }
    public String  getDisplayName()   { return displayName; }
    public void    setDisplayName(String n) { this.displayName = n; }
    public Date    getCreatedAt()     { return createdAt; }
    public void    setCreatedAt(Date d) { this.createdAt = d; }
    public Date    getLastLogin()     { return lastLogin; }
    public void    setLastLogin(Date d) { this.lastLogin = d; }
    public boolean isActive()         { return active; }
    public void    setActive(boolean a) { this.active = a; }

    public boolean isAdmin() { return "admin".equals(role); }
}
