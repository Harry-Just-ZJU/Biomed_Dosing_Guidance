<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<nav class="col-md-2 d-none d-md-block bg-light sidebar">
    <div class="sidebar-sticky pt-3">
        <h6 class="sidebar-heading px-3 mb-1 text-primary font-weight-bold" style="font-size:15px">
            Patient Portal
        </h6>
        <ul class="nav flex-column mb-4">
            <li class="nav-item">
                <a class='nav-link ${param.active == "dashboard" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/">🏠 Dashboard</a>
            </li>
            <li class="nav-item">
                <a class='nav-link ${param.active == "matching_index" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/matchingIndex">📂 Upload Genomic Data</a>
            </li>
            <li class="nav-item">
                <a class='nav-link ${param.active == "samples" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/samples">📋 My Reports</a>
            </li>
        </ul>
        <h6 class="sidebar-heading px-3 mt-4 mb-1 text-muted" style="font-size:15px">
            Admin Knowledge Base
        </h6>
        <ul class="nav flex-column mb-2">
            <li class="nav-item">
                <a class='nav-link ${param.active == "drugs" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/drugs">💊 Drugs Database</a>
            </li>
            <li class="nav-item">
                <a class='nav-link ${param.active == "drug_labels" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/drugLabels">🏷️ Drug Labels</a>
            </li>
            <li class="nav-item">
                <a class='nav-link ${param.active == "dosing_guideline" ? "active" : ""}'
                   href="<%=request.getContextPath()%>/dosingGuideline">📖 Dosing Guidelines</a>
            </li>
        </ul>
    </div>
</nav>
