<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="samples"/></jsp:include>
<div class="pm-page-header pm-animate d-flex justify-content-between align-items-center flex-wrap">
  <div><h2>My Reports</h2><p>History of genomic analyses.</p></div>
  <a href="<%=request.getContextPath()%>/matchingIndex" class="pm-btn pm-btn-teal pm-btn-sm">+ Upload New</a>
</div>
<div class="pm-card pm-animate pm-animate-d1" style="width:100%;">
  <div style="overflow-x:auto;width:100%;">
    <table class="pm-table" style="width:100%;">
      <thead><tr><th>ID</th><th>Patient</th><th>File</th><th>Date</th><th>Status</th><th>Action</th></tr></thead>
      <tbody>
      <c:forEach items="${samples}" var="item">
        <tr>
          <td><span class="pm-diplotype">#${item.id}</span></td>
          <td style="font-weight:600;">${item.uploadedBy}</td>
          <td style="font-size:.82rem;color:var(--muted);">
            <c:choose><c:when test="${not empty item.vcfFilename}">${item.vcfFilename}</c:when>
            <c:otherwise>—</c:otherwise></c:choose>
          </td>
          <td style="color:var(--muted);font-size:.85rem;">${item.createdAt}</td>
          <td><span class="pm-badge pm-badge-teal">&#10004; Analysed</span></td>
          <td><a href="<%=request.getContextPath()%>/matching?sampleId=${item.id}"
                 class="pm-btn pm-btn-outline pm-btn-sm">View</a></td>
        </tr>
      </c:forEach>
      <c:if test="${empty samples}">
        <tr><td colspan="6" style="text-align:center;padding:3rem;color:var(--muted);">
          No samples yet. <a href="<%=request.getContextPath()%>/matchingIndex"
            style="color:var(--teal);">Upload one now.</a>
        </td></tr>
      </c:if>
      </tbody>
    </table>
  </div>
</div>
<jsp:include page="footer.jsp"/>
