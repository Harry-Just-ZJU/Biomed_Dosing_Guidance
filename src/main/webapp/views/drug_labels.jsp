<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="drug_labels"/></jsp:include>

<div class="pm-page-header pm-animate d-flex justify-content-between align-items-end flex-wrap">
  <div><h2>Drug Labels</h2><p>FDA / EMA / PMDA regulatory labels.</p></div>
  <c:if test="${not empty q or not empty param.drugId}">
    <a href="<%=request.getContextPath()%>/drugLabels" class="pm-btn pm-btn-outline pm-btn-sm">Clear</a>
  </c:if>
</div>

<form method="get" action="<%=request.getContextPath()%>/drugLabels" class="pm-search-bar pm-animate pm-animate-d1">
  <input class="pm-search-input" type="text" name="q" value="${fn:escapeXml(q)}"
         placeholder="Search labels by name, source, or summary…">
  <button type="submit" class="pm-btn pm-btn-teal pm-btn-sm">Search</button>
</form>

  <c:if test="${not empty param.drugId}">
    <div class="pm-alert pm-alert-info mb-3">
      <span><svg class="pm-icon"><use href="#pm-icon-search"/></svg></span>
      <span>Filtered for Drug ID: <strong>${param.drugId}</strong></span>
    </div>
  </c:if>

<div class="pm-card pm-animate pm-animate-d2" style="width:100%;">
  <div style="overflow-x:auto;width:100%;">
    <table class="pm-table" style="width:100%;table-layout:fixed;">
      <colgroup>
        <col style="width:20%;"><col style="width:12%;">
        <col style="width:10%;"><col style="width:10%;"><col style="width:40%;"><col style="width:8%;">
      </colgroup>
      <thead>
        <tr><th>Label ID</th><th>Drug ID</th><th>Source</th><th>Dosing</th><th>Summary</th><th></th></tr>
      </thead>
      <tbody>
      <c:forEach items="${drugLabels}" var="item">
        <c:if test="${empty param.drugId or param.drugId == item.drugId}">
          <tr>
            <td style="font-size:.78rem;color:var(--muted);word-break:break-all;">${item.id}</td>
            <td><span class="pm-badge pm-badge-navy">${item.drugId}</span></td>
            <td><span class="pm-badge pm-badge-yellow">${item.source}</span></td>
            <td>
              <c:choose>
                <c:when test="${item.dosingInformation}"><span class="pm-badge pm-badge-teal">Yes</span></c:when>
                <c:otherwise><span class="pm-badge pm-badge-muted">No</span></c:otherwise>
              </c:choose>
            </td>
            <td style="font-size:.82rem;color:var(--muted);word-break:break-word;">
              ${fn:substring(item.summaryMarkdown,0,160)}
              <c:if test="${fn:length(item.summaryMarkdown)>160}">&hellip;</c:if>
            </td>
            <td>
              <a href="<%=request.getContextPath()%>/drugLabelDetail?id=${item.id}"
                 class="pm-btn pm-btn-outline pm-btn-sm">View</a>
            </td>
          </tr>
        </c:if>
      </c:forEach>
      <c:if test="${empty drugLabels}">
        <tr><td colspan="6" style="text-align:center;padding:3rem;color:var(--muted);">No labels found.</td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</div>
<jsp:include page="footer.jsp"/>
