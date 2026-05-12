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

<svg xmlns="http://www.w3.org/2000/svg" style="position:absolute;width:0;height:0;overflow:hidden" aria-hidden="true" focusable="false">
  <symbol id="pm-icon-chevron" viewBox="0 0 24 24"><path d="M6 9l6 6 6-6"/></symbol>
  <symbol id="pm-icon-upload" viewBox="0 0 24 24">
    <path d="M12 16V4"/><path d="M8 8l4-4 4 4"/><path d="M4 16v4h16v-4"/>
  </symbol>
  <symbol id="pm-icon-report" viewBox="0 0 24 24">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
    <path d="M14 2v6h6"/><path d="M8 13h8"/><path d="M8 17h5"/>
  </symbol>
  <symbol id="pm-icon-guide" viewBox="0 0 24 24">
    <path d="M2 4h6a2 2 0 0 1 2 2v14a2 2 0 0 0-2-2H2z"/>
    <path d="M22 4h-6a2 2 0 0 0-2 2v14a2 2 0 0 1 2-2h6z"/>
  </symbol>
  <symbol id="pm-icon-pill" viewBox="0 0 24 24">
    <path d="M4.5 19.5a5 5 0 0 1 0-7l8-8a5 5 0 0 1 7 7l-8 8a5 5 0 0 1-7 0z"/>
    <path d="M8.5 8.5l7 7"/>
  </symbol>
  <symbol id="pm-icon-tag" viewBox="0 0 24 24">
    <path d="M20.59 13.41L12 4.83V2H4v8l8.59 8.59a2 2 0 0 0 2.82 0l5.18-5.18a2 2 0 0 0 0-2.82z"/>
    <circle cx="7.5" cy="7.5" r="1.5"/>
  </symbol>
  <symbol id="pm-icon-book" viewBox="0 0 24 24">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 0 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </symbol>
  <symbol id="pm-icon-logout" viewBox="0 0 24 24">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
    <path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>
  </symbol>
  <symbol id="pm-icon-search" viewBox="0 0 24 24">
    <circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/>
  </symbol>
  <symbol id="pm-icon-lock" viewBox="0 0 24 24">
    <rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </symbol>
  <symbol id="pm-icon-chart" viewBox="0 0 24 24">
    <path d="M3 3v18h18"/><rect x="7" y="12" width="3" height="6"/><rect x="12" y="9" width="3" height="9"/><rect x="17" y="5" width="3" height="13"/>
  </symbol>
  <symbol id="pm-icon-spark" viewBox="0 0 24 24"><path d="M13 2L3 14h7l-1 8 10-12h-7z"/></symbol>
  <symbol id="pm-icon-layers" viewBox="0 0 24 24">
    <path d="M12 2l9 5-9 5-9-5 9-5z"/><path d="M3 12l9 5 9-5"/><path d="M3 17l9 5 9-5"/>
  </symbol>
  <symbol id="pm-icon-arrow-right" viewBox="0 0 24 24">
    <path d="M5 12h14"/><path d="M13 6l6 6-6 6"/>
  </symbol>
  <symbol id="pm-icon-arrow-left" viewBox="0 0 24 24">
    <path d="M19 12H5"/><path d="M11 6l-6 6 6 6"/>
  </symbol>
  <symbol id="pm-icon-info" viewBox="0 0 24 24">
    <circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>
  </symbol>
  <symbol id="pm-icon-alert" viewBox="0 0 24 24">
    <path d="M10.3 3.3L1.7 18a2 2 0 0 0 1.7 3h17.2a2 2 0 0 0 1.7-3L13.7 3.3a2 2 0 0 0-3.4 0z"/>
    <path d="M12 9v4"/><path d="M12 17h.01"/>
  </symbol>
  <symbol id="pm-icon-check" viewBox="0 0 24 24"><path d="M20 6L9 17l-5-5"/></symbol>
  <symbol id="pm-icon-ban" viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M5 5l14 14"/></symbol>
  <symbol id="pm-icon-lightbulb" viewBox="0 0 24 24">
    <path d="M9 18h6"/><path d="M10 22h4"/>
    <path d="M12 2a7 7 0 0 0-4 12c.7.6 1 1.2 1 2h6c0-.8.3-1.4 1-2a7 7 0 0 0-4-12z"/>
  </symbol>
  <symbol id="pm-icon-dna" viewBox="0 0 24 24">
    <path d="M4 3c7 6 9 12 16 18"/><path d="M20 3c-7 6-9 12-16 18"/>
  </symbol>
</svg>

<nav class="pm-topnav">
  <a class="pm-topnav-brand" href="<%=request.getContextPath()%>/">
    <span class="brand-dot"></span>
    Nebula Dosing
  </a>

  <div class="pm-topnav-links">

    <!-- Patient Portal dropdown -->
    <div class="pm-nav-dropdown">
      <button class="pm-nav-btn">Patient Portal <svg class="pm-icon pm-nav-chevron"><use href="#pm-icon-chevron"/></svg></button>
      <div class="pm-nav-panel">
        <div class="pm-nav-panel-section">My Journey</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/matchingIndex">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-upload"/></svg></span>
          <div>
            <div class="pm-nav-item-title">Upload Genomic Data</div>
            <div class="pm-nav-item-sub">VCF or ANNOVAR file analysis</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/samples">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-report"/></svg></span>
          <div>
            <div class="pm-nav-item-title">My Reports</div>
            <div class="pm-nav-item-sub">View previous analyses</div>
          </div>
        </a>
        <div class="pm-nav-panel-section" style="margin-top:.75rem;">Resources</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/static/pdf/user_guide.pdf" target="_blank">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-guide"/></svg></span>
          <div>
            <div class="pm-nav-item-title">User Guide (PDF)</div>
            <div class="pm-nav-item-sub">Download getting-started guide</div>
          </div>
        </a>
      </div>
    </div>

    <!-- Knowledge Base dropdown -->
    <div class="pm-nav-dropdown">
      <button class="pm-nav-btn">Knowledge Base <svg class="pm-icon pm-nav-chevron"><use href="#pm-icon-chevron"/></svg></button>
      <div class="pm-nav-panel">
        <div class="pm-nav-panel-section">Drug Information</div>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/drugs">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-pill"/></svg></span>
          <div>
            <div class="pm-nav-item-title">Drugs Database</div>
            <div class="pm-nav-item-sub">Search pharmacogenomic drugs</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/drugLabels">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-tag"/></svg></span>
          <div>
            <div class="pm-nav-item-title">Drug Labels</div>
            <div class="pm-nav-item-sub">FDA / EMA / PMDA regulatory labels</div>
          </div>
        </a>
        <a class="pm-nav-item" href="<%=request.getContextPath()%>/dosingGuideline">
          <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-book"/></svg></span>
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
          <svg class="pm-icon pm-nav-chevron"><use href="#pm-icon-chevron"/></svg>
        </button>
        <div class="pm-nav-panel" style="right:0;left:auto;min-width:180px;">
          <div class="pm-nav-panel-section">
            <c:choose>
              <c:when test="${__cu.role == 'admin'}">Admin / Researcher</c:when>
              <c:otherwise>Patient</c:otherwise>
            </c:choose>
          </div>
          <a class="pm-nav-item" href="<%=request.getContextPath()%>/logout">
            <span class="pm-nav-item-icon"><svg class="pm-icon"><use href="#pm-icon-logout"/></svg></span>
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
