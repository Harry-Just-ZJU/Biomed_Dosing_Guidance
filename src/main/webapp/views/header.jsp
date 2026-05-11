<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%
  cn.edu.zju.bean.User __cu =
    (cn.edu.zju.bean.User) session.getAttribute("currentUser");
  request.setAttribute("__cu", __cu);
%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Nebula Dosing — Precision Pharmacogenomics</title>
  <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
  <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
  <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
  <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
</head>
<body>

<nav class="pm-topnav">
  <a class="pm-topnav-brand" href="<%=request.getContextPath()%>/">
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" style="-webkit-text-fill-color:initial;flex-shrink:0;">
      <circle cx="12" cy="12" r="3" fill="url(#ng)"/>
      <circle cx="12" cy="12" r="7" stroke="url(#ng)" stroke-width="1.5" fill="none" opacity=".4"/>
      <circle cx="12" cy="12" r="11" stroke="url(#ng)" stroke-width="1" fill="none" opacity=".2"/>
      <defs>
        <linearGradient id="ng" x1="0" y1="0" x2="24" y2="24" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stop-color="#A78BFA"/><stop offset="100%" stop-color="#60A5FA"/>
        </linearGradient>
      </defs>
    </svg>
    Nebula Dosing
  </a>

  <div class="pm-topnav-links">

    <!-- Patient Portal dropdown -->
    <div class="pm-nav-dropdown">
      <button class="pm-nav-btn">Patient Portal <span class="pm-nav-chevron">&#9660;</span></button>
      <div class="pm-nav-panel">
        <div class="pm-nav-panel-section">My Journey</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/matchingIndex">
          <span class="pm-nav-item-icon">&#128194;</span>
          <div>
            <div class="pm-nav-item-title">Upload Genomic Data</div>
            <div class="pm-nav-item-sub">VCF or ANNOVAR file analysis</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/samples">
          <span class="pm-nav-item-icon">&#128203;</span>
          <div>
            <div class="pm-nav-item-title">My Reports</div>
            <div class="pm-nav-item-sub">View previous analyses</div>
          </div>
        </a>
        <div class="pm-nav-panel-section" style="margin-top:.75rem;">Resources</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/static/pdf/user_guide.pdf" target="_blank">
          <span class="pm-nav-item-icon">&#128196;</span>
          <div>
            <div class="pm-nav-item-title">User Guide (PDF)</div>
            <div class="pm-nav-item-sub">Download getting-started guide</div>
          </div>
        </a>
      </div>
    </div>

    <!-- Knowledge Base dropdown -->
    <div class="pm-nav-dropdown">
      <button class="pm-nav-btn">Knowledge Base <span class="pm-nav-chevron">&#9660;</span></button>
      <div class="pm-nav-panel">
        <div class="pm-nav-panel-section">Drug Information</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/drugs">
          <span class="pm-nav-item-icon">&#128138;</span>
          <div>
            <div class="pm-nav-item-title">Drugs Database</div>
            <div class="pm-nav-item-sub">Search pharmacogenomic drugs</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/drugLabels">
          <span class="pm-nav-item-icon">&#127991;&#65039;</span>
          <div>
            <div class="pm-nav-item-title">Drug Labels</div>
            <div class="pm-nav-item-sub">FDA / EMA / PMDA regulatory labels</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/dosingGuideline">
          <span class="pm-nav-item-icon">&#128218;</span>
          <div>
            <div class="pm-nav-item-title">Dosing Guidelines</div>
            <div class="pm-nav-item-sub">CPIC / DPWG evidence-based guidance</div>
          </div>
        </a>
      </div>
    </div>

  </div>

  <!-- Right: user menu -->
  <div class="pm-topnav-right">
    <c:if test="${not empty __cu}">
      <div class="pm-nav-dropdown">
        <button class="pm-nav-btn" style="display:flex;align-items:center;gap:.5rem;">
          <div class="pm-avatar" style="width:28px;height:28px;font-size:.75rem;">
            ${fn:substring(__cu.displayName,0,1)}
          </div>
          <span style="font-size:.85rem;font-weight:600;">${__cu.displayName}</span>
          <span class="pm-nav-chevron">&#9660;</span>
        </button>
        <div class="pm-nav-panel" style="right:0;left:auto;min-width:180px;">
          <div class="pm-nav-panel-section">
            <c:choose>
              <c:when test="${__cu.role == 'admin'}">Admin / Researcher</c:when>
              <c:otherwise>Patient</c:otherwise>
            </c:choose>
          </div>
          <a class="pm-nav-item" href="<%=request.getContextPath()%>/logout">
            <span class="pm-nav-item-icon">&#128682;</span>
            <div><div class="pm-nav-item-title">Sign Out</div></div>
          </a>
        </div>
      </div>
    </c:if>
  </div>
</nav>


<script>
// Dropdown: keep open for 200ms after mouse leaves so user can move to panel
document.querySelectorAll('.pm-nav-dropdown').forEach(function(dd) {
  var timer;
  var panel = dd.querySelector('.pm-nav-panel');

  function show() {
    clearTimeout(timer);
    panel.style.display = 'block';
  }
  function hide() {
    timer = setTimeout(function() {
      panel.style.display = '';
    }, 220);  // 220ms grace period
  }

  dd.addEventListener('mouseenter', show);
  dd.addEventListener('mouseleave', hide);
  if (panel) {
    panel.addEventListener('mouseenter', show);
    panel.addEventListener('mouseleave', hide);
  }
});
</script>

<div class="pm-page-wrap">
