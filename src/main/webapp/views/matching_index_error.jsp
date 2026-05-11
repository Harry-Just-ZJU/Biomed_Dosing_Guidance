<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="matching_index"/></jsp:include>
<div class="pm-page-header pm-animate"><h2 style="color:var(--red);">Analysis Failed</h2></div>
<div class="row"><div class="col-lg-6 col-md-8">
  <div class="pm-card pm-animate" style="border-color:var(--red);">
    <div class="pm-card-body text-center" style="padding:2.5rem;">
      <div style="width:64px;height:64px;background:var(--red-light);border-radius:50%;
          display:flex;align-items:center;justify-content:center;margin:0 auto 1.25rem;">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--red)"
             stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <h4 style="font-family:var(--font-sans);font-weight:700;color:var(--red);margin-bottom:1rem;">
        We couldn't process your file
      </h4>
      <div class="text-left">
        <c:choose>
          <c:when test="${fn:contains(validateError,'Database') or fn:contains(validateError,'database')}">
            <div class="pm-alert pm-alert-danger mb-3">
              <span>&#9940;</span>
              <div><strong>Database Error</strong><br><small>${validateError}</small></div>
            </div>
            <div class="pm-alert pm-alert-warning text-left" style="font-size:.83rem;">
              <span>&#128161;</span>
              <div><strong>Fix:</strong> Ensure MySQL is running, database <code>biomed</code> exists,
              and run <code>migrate.sql</code> + <code>migrate_users.sql</code>.</div>
            </div>
          </c:when>
          <c:otherwise>
            <div class="pm-alert pm-alert-danger mb-3">
              <span>&#9940;</span>
              <div><strong>Processing Error</strong><br><small>${validateError}</small></div>
            </div>
          </c:otherwise>
        </c:choose>
      </div>
      <div class="d-flex justify-content-center mt-4" style="gap:.75rem;">
        <a href="<%=request.getContextPath()%>/matchingIndex" class="pm-btn pm-btn-primary">Try Again</a>
        <a href="<%=request.getContextPath()%>/" class="pm-btn pm-btn-outline">Dashboard</a>
      </div>
    </div>
  </div>
</div></div>
<jsp:include page="footer.jsp"/>
