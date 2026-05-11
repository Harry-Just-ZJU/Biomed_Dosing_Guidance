<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Create Account — Precision Medicine</title>
  <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
  <style>
    body { background:var(--bg); display:flex; align-items:center; justify-content:center;
           min-height:100vh; padding-top:0; }
    .auth-wrap { width:100%; max-width:460px; padding:1rem; }
    .auth-logo { text-align:center; margin-bottom:2rem; }
    .auth-logo .brand-dot { display:inline-block; width:12px; height:12px;
      background:var(--teal); border-radius:50%; margin-right:8px; }
    .auth-logo span { font-family:var(--font-sans); font-size:1.15rem;
      font-weight:700; color:var(--navy); }
    .auth-card { background:var(--surface); border:1px solid var(--border);
      border-radius:var(--radius-lg); padding:2.5rem; box-shadow:var(--shadow-md); }
    .auth-card h2 { font-size:1.5rem; margin-bottom:.25rem; }
    .auth-card p.sub { color:var(--muted); font-size:.88rem; margin-bottom:1.75rem; }
    .role-btn { flex:1; padding:.6rem; border:1.5px solid var(--border);
      border-radius:var(--radius-sm); text-align:center; cursor:pointer;
      font-size:.88rem; font-weight:600; color:var(--muted);
      transition:var(--transition); }
    .role-btn.selected { border-color:var(--teal); color:var(--teal);
      background:var(--teal-light); }
    .pw-hint { font-size:.75rem; color:var(--muted); margin-top:.3rem; }
  </style>
</head>
<body>
<div class="auth-wrap pm-animate">
  <div class="auth-logo">
    <span class="brand-dot"></span>
    <span>Precision Medicine</span>
  </div>

  <div class="auth-card">
    <h2>Create account</h2>
    <p class="sub">Join to access personalised pharmacogenomic insights.</p>

    <c:if test="${not empty regError}">
      <div class="pm-alert pm-alert-danger mb-3">
        <span>⚠️</span><span>${regError}</span>
      </div>
    </c:if>

    <form method="post" action="<%=request.getContextPath()%>/register" id="regForm">

      <div class="pm-form-group">
        <label class="pm-label">I am a</label>
        <div class="d-flex" style="gap:.75rem;">
          <div class="role-btn selected" id="btn-patient" onclick="setRole('patient')">
            🧬 Patient
          </div>
          <div class="role-btn" id="btn-admin" onclick="setRole('admin')">
            🔬 Admin / Researcher
          </div>
        </div>
        <input type="hidden" id="roleInput" name="role" value="patient">
      </div>

      <div class="pm-form-group">
        <label class="pm-label" for="displayName">Full Name</label>
        <input class="pm-input" type="text" id="displayName" name="displayName"
               placeholder="Jane Smith" value="${param.displayName}">
      </div>

      <div class="pm-form-group">
        <label class="pm-label" for="username">
          Username <span style="color:var(--red)">*</span>
        </label>
        <input class="pm-input" type="text" id="username" name="username"
               placeholder="letters, digits, _ - ." required
               value="${param.username}" autocomplete="username">
      </div>

      <div class="pm-form-group">
        <label class="pm-label" for="password">
          Password <span style="color:var(--red)">*</span>
        </label>
        <input class="pm-input" type="password" id="password" name="password"
               placeholder="min 8 characters" required autocomplete="new-password">
        <div class="pw-hint">At least 8 characters.</div>
      </div>

      <div class="pm-form-group">
        <label class="pm-label" for="confirmPassword">Confirm Password <span style="color:var(--red)">*</span></label>
        <input class="pm-input" type="password" id="confirmPassword" name="confirmPassword"
               placeholder="repeat password" required>
      </div>

      <button type="submit" class="pm-btn pm-btn-teal pm-btn-lg w-100 mt-2"
              style="justify-content:center;">
        Create Account
      </button>
    </form>

    <p style="text-align:center;margin-top:1.5rem;font-size:.85rem;color:var(--muted);">
      Already have an account?
      <a href="<%=request.getContextPath()%>/login"
         style="color:var(--teal);font-weight:600;">Sign in</a>
    </p>
  </div>
</div>

<script>
function setRole(r) {
  document.getElementById('roleInput').value = r;
  document.getElementById('btn-patient').className = 'role-btn' + (r==='patient'?' selected':'');
  document.getElementById('btn-admin').className   = 'role-btn' + (r==='admin'  ?' selected':'');
}
document.getElementById('regForm').addEventListener('submit', function(e) {
  const p = document.getElementById('password').value;
  const c = document.getElementById('confirmPassword').value;
  if (p !== c) { e.preventDefault(); alert('Passwords do not match.'); }
});
</script>
</body>
</html>
