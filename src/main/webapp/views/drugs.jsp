<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="drugs"/></jsp:include>

<div class="pm-page-header pm-animate d-flex justify-content-between align-items-end flex-wrap">
  <div><h2>Drugs Database</h2><p>Pharmacogenomic drugs from PharmGKB.</p></div>
  <c:if test="${not empty q}">
    <a href="<%=request.getContextPath()%>/drugs" class="pm-btn pm-btn-outline pm-btn-sm">Clear</a>
  </c:if>
</div>

<form method="get" action="<%=request.getContextPath()%>/drugs" class="pm-search-bar pm-animate pm-animate-d1">
  <input class="pm-search-input" type="text" name="q" value="${fn:escapeXml(q)}"
         placeholder="Search by drug name or ID…">
  <button type="submit" class="pm-btn pm-btn-teal pm-btn-sm">Search</button>
</form>

<c:if test="${not empty q}">
  <div class="pm-alert pm-alert-info mb-3">
    <span>&#128269;</span>
    <span>${fn:length(drugs)} result(s) for "<strong>${fn:escapeXml(q)}</strong>"</span>
  </div>
</c:if>

<div class="pm-card pm-animate pm-animate-d2" style="width:100%;">
  <div style="overflow-x:auto;width:100%;">
    <table class="pm-table" style="width:100%;table-layout:fixed;">
      <colgroup>
        <col style="width:20%;"><col style="width:35%;">
        <col style="width:12%;"><col style="width:33%;">
      </colgroup>
      <thead><tr><th>Drug ID</th><th>Drug Name</th><th>Biomarker</th><th>Actions</th></tr></thead>
      <tbody>
      <c:forEach items="${drugs}" var="item">
        <tr>
          <td style="font-size:.78rem;color:var(--muted);word-break:break-all;">${item.id}</td>
          <td style="font-weight:600;">
            <a href="<%=request.getContextPath()%>/drugDetail?id=${item.id}"
               style="color:var(--navy);">${item.name}</a>
          </td>
          <td>
            <c:choose>
              <c:when test="${item.biomarker}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
              <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise>
            </c:choose>
          </td>
          <td>
            <div style="display:flex;gap:.4rem;flex-wrap:wrap;">
              <a href="<%=request.getContextPath()%>/drugDetail?id=${item.id}"
                 class="pm-btn pm-btn-primary pm-btn-sm">Details</a>
              <a href="<%=request.getContextPath()%>/dosingGuideline?drugId=${item.id}"
                 class="pm-btn pm-btn-outline pm-btn-sm">Guidelines</a>
              <a href="<%=request.getContextPath()%>/drugLabels?drugId=${item.id}"
                 class="pm-btn pm-btn-outline pm-btn-sm">Labels</a>
            </div>
          </td>
        </tr>
      </c:forEach>
      <c:if test="${empty drugs}">
        <tr><td colspan="4" style="text-align:center;padding:3rem;color:var(--muted);">
          No drugs found. Run the crawler to populate the database.
        </td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</div>
<jsp:include page="footer.jsp"/>
