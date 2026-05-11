<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Sign In — Precision Medicine</title>
  <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
  <style>
    body { background:var(--bg); display:flex; align-items:center; justify-content:center;
           min-height:100vh; padding-top:0; }
    .auth-wrap { width:100%; max-width:420px; padding:1rem; }
    .auth-logo { text-align:center; margin-bottom:2rem; }
    .auth-logo .brand-dot { display:inline-block; width:12px; height:12px;
      background:var(--teal); border-radius:50%; margin-right:8px; }
    .auth-logo span { font-family:var(--font-sans); font-size:1.15rem;
      font-weight:700; color:var(--navy); }
    .auth-card { background:var(--surface); border:1px solid var(--border);
      border-radius:var(--radius-lg); padding:2.5rem; box-shadow:var(--shadow-md); }
    .auth-card h2 { font-size:1.5rem; margin-bottom:.25rem; }
    .auth-card p.sub { color:var(--muted); font-size:.88rem; margin-bottom:1.75rem; }
  </style>
</head>
<body>
<div class="auth-wrap pm-animate">
  <div class="auth-logo">
    <span class="brand-dot"></span>
    <span>Precision Medicine</span>
  </div>

  <div class="auth-card">
    <h2>Welcome back</h2>
    <p class="sub">Sign in to access your pharmacogenomic reports.</p>

    <c:if test="${not empty loginError}" xmlns:c="http://java.sun.com/jsp/jstl/core">
      <div class="pm-alert pm-alert-danger mb-3">
        <span>⚠️</span><span>${loginError}</span>
      </div>
    </c:if>
    <c:if test="${not empty regSuccess}" xmlns:c="http://java.sun.com/jsp/jstl/core">
      <div class="pm-alert pm-alert-success mb-3">
        <span>✔</span><span>${regSuccess}</span>
      </div>
    </c:if>

    <form method="post" action="<%=request.getContextPath()%>/login" autocomplete="on">
      <div class="pm-form-group">
        <label class="pm-label" for="username">Username</label>
        <input class="pm-input" type="text" id="username" name="username"
               placeholder="your username" autocomplete="username" required>
      </div>
      <div class="pm-form-group">
        <label class="pm-label" for="password">Password</label>
        <input class="pm-input" type="password" id="password" name="password"
               placeholder="••••••••" autocomplete="current-password" required>
      </div>
      <button type="submit" class="pm-btn pm-btn-teal pm-btn-lg w-100 mt-2"
              style="justify-content:center;">
        Sign In
      </button>
    </form>

    <p style="text-align:center;margin-top:1.5rem;font-size:.85rem;color:var(--muted);">
      Don't have an account?
      <a href="<%=request.getContextPath()%>/register"
         style="color:var(--teal);font-weight:600;">Create one</a>
    </p>
  </div>
</div>
</body>
</html>
