package cn.edu.zju.controller;

import cn.edu.zju.bean.User;
import cn.edu.zju.dao.UserDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int SESSION_TIMEOUT_SECONDS = 60 * 60; // 1 hour

    private final UserDao userDao = new UserDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/login",    this::loginPage);
        dispatcher.registerPostMapping("/login",   this::loginSubmit);
        dispatcher.registerGetMapping("/logout",   this::logout);
        dispatcher.registerGetMapping("/register", this::registerPage);
        dispatcher.registerPostMapping("/register",this::registerSubmit);
    }

    // ── GET /login ──────────────────────────────────────────────────────────
    public void loginPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Already logged in → redirect to home
        if (currentUser(req) != null) { resp.sendRedirect(base(req) + "/"); return; }
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    // ── POST /login ─────────────────────────────────────────────────────────
    public void loginSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = trim(req.getParameter("username"));
        String password = req.getParameter("password");

        if (username.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("loginError", "Username and password are required.");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        User user = userDao.authenticate(username, password);
        if (user == null) {
            // Generic message — don't reveal whether username exists
            req.setAttribute("loginError", "Invalid username or password.");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
            return;
        }

        // Invalidate old session (session fixation protection)
        req.getSession(false); // get existing without creating
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();

        HttpSession session = req.getSession(true);
        session.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        session.setAttribute("currentUser", user);

        log.info("User logged in: {} ({})", user.getUsername(), user.getRole());
        resp.sendRedirect(base(req) + "/");
    }

    // ── GET /logout ─────────────────────────────────────────────────────────
    public void logout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            log.info("User logged out: {}", session.getAttribute("currentUser") != null
                    ? ((User) session.getAttribute("currentUser")).getUsername() : "unknown");
            session.invalidate();
        }
        resp.sendRedirect(base(req) + "/login");
    }

    // ── GET /register ────────────────────────────────────────────────────────
    public void registerPage(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (currentUser(req) != null) { resp.sendRedirect(base(req) + "/"); return; }
        req.getRequestDispatcher("/views/register.jsp").forward(req, resp);
    }

    // ── POST /register ───────────────────────────────────────────────────────
    public void registerSubmit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username    = trim(req.getParameter("username"));
        String password    = req.getParameter("password");
        String confirm     = req.getParameter("confirmPassword");
        String role        = req.getParameter("role");
        String displayName = trim(req.getParameter("displayName"));

        // Validation
        if (username.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("regError", "Username and password are required.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (username.length() < 3 || username.length() > 50) {
            req.setAttribute("regError", "Username must be 3–50 characters.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (!username.matches("[a-zA-Z0-9_\\-\\.]+")) {
            req.setAttribute("regError", "Username may only contain letters, digits, _ - .");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (password.length() < 8) {
            req.setAttribute("regError", "Password must be at least 8 characters.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (!password.equals(confirm)) {
            req.setAttribute("regError", "Passwords do not match.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (!"patient".equals(role) && !"admin".equals(role)) {
            role = "patient"; // safe default
        }

        int result = userDao.register(username, password, role,
                displayName.isEmpty() ? username : displayName);

        if (result == -2) {
            req.setAttribute("regError", "Username already taken. Please choose another.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }
        if (result < 0) {
            req.setAttribute("regError", "Registration failed due to a database error.");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp); return;
        }

        req.setAttribute("regSuccess", "Account created! You can now log in.");
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public static User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return null;
        return (User) s.getAttribute("currentUser");
    }

    /** Redirect to login if not authenticated. Returns true if redirected. */
    public static boolean requireLogin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (currentUser(req) != null) return false;
        resp.sendRedirect(base(req) + "/login");
        return true;
    }

    /** Require admin role. Returns true if access denied. */
    public static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        User u = currentUser(req);
        if (u == null) { resp.sendRedirect(base(req) + "/login"); return true; }
        if (!u.isAdmin()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            req.setAttribute("errorMsg", "Admin access required.");
            req.getRequestDispatcher("/views/error.jsp").forward(req, resp);
            return true;
        }
        return false;
    }

    private static String base(HttpServletRequest req) {
        return req.getContextPath();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
