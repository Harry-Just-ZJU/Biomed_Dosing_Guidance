<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="drug_labels"/></jsp:include>
<div class="pm-page-header pm-animate">
  <div style="display:flex;align-items:center;gap:.75rem;">
    <a href="<%=request.getContextPath()%>/drugLabels" class="pm-btn pm-btn-outline pm-btn-sm">
      <svg class="pm-icon"><use href="#pm-icon-arrow-left"/></svg> Back
    </a>
    <h2 style="margin:0;">Drug Label Detail</h2>
    <span class="pm-badge pm-badge-yellow">${label.source}</span>
  </div>
  <p style="margin-top:.5rem;">Label ID: ${label.id} &middot; Drug: ${label.drugId}</p>
</div>
<div class="row">
  <div class="col-md-8">
    <c:if test="${not empty label.summaryMarkdown}">
      <div class="pm-card mb-4 pm-animate">
        <div class="pm-card-header">Summary</div>
        <div class="pm-card-body pm-prose">${label.summaryMarkdown}</div>
      </div>
    </c:if>
    <c:if test="${not empty label.prescribingMarkdown}">
      <div class="pm-card mb-4 pm-animate pm-animate-d1">
        <div class="pm-card-header">Prescribing Information</div>
        <div class="pm-card-body pm-prose">${label.prescribingMarkdown}</div>
      </div>
    </c:if>
    <c:if test="${not empty label.textMarkdown}">
      <div class="pm-card mb-4 pm-animate pm-animate-d2">
        <div class="pm-card-header">Full Text</div>
        <div class="pm-card-body pm-prose">${label.textMarkdown}</div>
      </div>
    </c:if>
  </div>
  <div class="col-md-4 pm-animate pm-animate-d1">
    <div class="pm-card">
      <div class="pm-card-header">Label Info</div>
      <div class="pm-card-body" style="font-size:.88rem;">
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Source</div>
          <span class="pm-badge pm-badge-yellow">${label.source}</span></div>
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Dosing Information</div>
          <c:choose><c:when test="${label.dosingInformation}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
          <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise></c:choose></div>
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Alternate Drug Available</div>
          <c:choose><c:when test="${label.alternateDrugAvailable}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
          <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise></c:choose></div>
      </div>
    </div>
  </div>
</div>
<jsp:include page="footer.jsp"/>
