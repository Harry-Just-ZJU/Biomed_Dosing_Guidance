<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="drugs"/></jsp:include>

<div class="pm-page-header pm-animate">
  <div style="display:flex;align-items:center;gap:.75rem;">
    <a href="<%=request.getContextPath()%>/drugs" class="pm-btn pm-btn-outline pm-btn-sm">
      &#8592; Back
    </a>
    <h2 style="margin:0;">${drug.name}</h2>
    <c:if test="${drug.biomarker}">
      <span class="pm-badge pm-badge-teal">Biomarker</span>
    </c:if>
  </div>
  <p style="margin-top:.5rem;">PharmGKB ID: ${drug.id}</p>
</div>

<div class="row">
  <div class="col-md-8">
    <!-- Drug Labels -->
    <div class="pm-detail-section pm-animate pm-animate-d1">
      <h3>&#127991;&#65039; Drug Labels (${fn:length(labels)})</h3>
      <c:if test="${empty labels}">
        <p style="color:var(--muted);">No labels found for this drug.</p>
      </c:if>
      <c:forEach items="${labels}" var="lbl">
        <div class="pm-card mb-3">
          <div class="pm-card-header">
            <span>${lbl.source}</span>
            <c:if test="${lbl.dosingInformation}">
              <span class="pm-badge pm-badge-teal">Has Dosing Info</span>
            </c:if>
          </div>
          <div class="pm-card-body">
            <p class="pm-prose">${fn:substring(lbl.summaryMarkdown,0,400)}
              <c:if test="${fn:length(lbl.summaryMarkdown)>400}">&hellip;</c:if>
            </p>
            <a href="<%=request.getContextPath()%>/drugLabelDetail?id=${lbl.id}"
               class="pm-btn pm-btn-outline pm-btn-sm">Read Full Label</a>
          </div>
        </div>
      </c:forEach>
    </div>

    <!-- Dosing Guidelines -->
    <div class="pm-detail-section pm-animate pm-animate-d2">
      <h3>&#128218; Dosing Guidelines (${fn:length(guidelines)})</h3>
      <c:if test="${empty guidelines}">
        <p style="color:var(--muted);">No guidelines found for this drug.</p>
      </c:if>
      <c:forEach items="${guidelines}" var="g">
        <div class="pm-card mb-3">
          <div class="pm-card-header">
            <span>${g.name}</span>
            <span class="pm-badge pm-badge-teal">${g.source}</span>
          </div>
          <div class="pm-card-body">
            <p class="pm-prose">${fn:substring(g.summaryMarkdown,0,400)}
              <c:if test="${fn:length(g.summaryMarkdown)>400}">&hellip;</c:if>
            </p>
            <a href="<%=request.getContextPath()%>/dosingGuidelineDetail?id=${g.id}"
               class="pm-btn pm-btn-outline pm-btn-sm">Read Full Guideline</a>
          </div>
        </div>
      </c:forEach>
    </div>
  </div>

  <div class="col-md-4 pm-animate pm-animate-d1">
    <div class="pm-card">
      <div class="pm-card-header">Drug Info</div>
      <div class="pm-card-body" style="font-size:.88rem;">
        <div style="margin-bottom:.75rem;">
          <div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">ID</div>
          <div style="font-family:'Courier New',monospace;">${drug.id}</div>
        </div>
        <div style="margin-bottom:.75rem;">
          <div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">Biomarker</div>
          <c:choose>
            <c:when test="${drug.biomarker}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
            <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise>
          </c:choose>
        </div>
        <c:if test="${not empty drug.drugUrl}">
          <div>
            <div style="color:var(--muted);font-size:.75rem;text-transform:uppercase;font-weight:600;">PharmGKB Link</div>
            <a href="https://www.pharmgkb.org${drug.drugUrl}" target="_blank"
               style="color:var(--teal);font-size:.82rem;">View on PharmGKB &#8594;</a>
          </div>
        </c:if>
      </div>
    </div>
  </div>
</div>
<jsp:include page="footer.jsp"/>
