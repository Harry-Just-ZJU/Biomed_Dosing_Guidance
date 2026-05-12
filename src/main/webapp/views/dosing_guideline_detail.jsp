<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="dosing_guideline"/></jsp:include>
<div class="pm-page-header pm-animate">
  <div style="display:flex;align-items:center;gap:.75rem;flex-wrap:wrap;">
    <a href="<%=request.getContextPath()%>/dosingGuideline" class="pm-btn pm-btn-outline pm-btn-sm">
      <svg class="pm-icon"><use href="#pm-icon-arrow-left"/></svg> Back
    </a>
    <h2 style="margin:0;">${guideline.name}</h2>
    <span class="pm-badge pm-badge-teal">${guideline.source}</span>
    <c:if test="${guideline.recommendation}">
      <span class="pm-badge pm-badge-teal">Has Recommendation</span>
    </c:if>
  </div>
  <p style="margin-top:.5rem;">Drug ID: ${guideline.drugId}</p>
</div>
<div class="row">
  <div class="col-md-8">
    <c:if test="${not empty guideline.summaryMarkdown}">
      <div class="pm-card mb-4 pm-animate">
        <div class="pm-card-header">Summary</div>
        <div class="pm-card-body pm-prose">${guideline.summaryMarkdown}</div>
      </div>
    </c:if>
    <c:if test="${not empty guideline.textMarkdown}">
      <div class="pm-card mb-4 pm-animate pm-animate-d1">
        <div class="pm-card-header">Full Guideline Text</div>
        <div class="pm-card-body pm-prose">${guideline.textMarkdown}</div>
      </div>
    </c:if>
  </div>
  <div class="col-md-4 pm-animate pm-animate-d1">
    <div class="pm-card">
      <div class="pm-card-header">Guideline Info</div>
      <div class="pm-card-body" style="font-size:.88rem;">
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Source</div>
          <span class="pm-badge pm-badge-teal">${guideline.source}</span></div>
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Drug ID</div>
          <a href="<%=request.getContextPath()%>/drugDetail?id=${guideline.drugId}"
             style="color:var(--teal);">${guideline.drugId}</a></div>
        <div class="mb-3"><div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Recommendation</div>
          <c:choose><c:when test="${guideline.recommendation}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
          <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise></c:choose></div>
      </div>
    </div>
  </div>
</div>
<jsp:include page="footer.jsp"/>
