<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="matching_index"/></jsp:include>

<div class="pm-page-header pm-animate d-flex justify-content-between align-items-end flex-wrap">
  <div>
    <h2>Pharmacogenomic Report</h2>
    <p>Generated via CPIC haplotype inference pipeline</p>
  </div>
  <div style="display:flex;gap:.5rem;flex-wrap:wrap;">
    <span class="pm-chip">Sample #${sample.id}</span>
    <span class="pm-chip">${sample.createdAt}</span>
    <c:if test="${not empty fileType}">
      <span class="pm-chip" style="color:var(--teal);border-color:var(--teal);">
        ${fileType} input
      </span>
    </c:if>
  </div>
</div>

<!-- Patient card -->
<div class="pm-patient-card pm-animate pm-animate-d1 mb-4">
  <div>
    <p class="pm-patient-name">${sample.uploadedBy}</p>
    <p class="pm-patient-meta">${sample.vcfFilename} &middot; CPIC analysis complete</p>
  </div>
  <a href="<%=request.getContextPath()%>/matchingIndex"
     class="pm-btn pm-btn-outline pm-btn-sm"
     style="color:#fff;border-color:rgba(255,255,255,.35);">Upload Another</a>
</div>

<!-- History rerun notice -->
<c:if test="${rerunMode}">
  <div class="pm-alert pm-alert-info pm-animate mb-4">
    <span>&#8505;&#65039;</span>
    <div>
      <strong>History View</strong> — Full haplotype report available immediately after upload.
      Re-upload the VCF to regenerate.
      <div class="mt-2">
        <c:forEach items="${genes}" var="g">
          <span class="pm-gene-pill mr-1">${g}</span>
        </c:forEach>
      </div>
    </div>
  </div>
</c:if>

<c:if test="${not rerunMode}">

  <!-- ── ANNOVAR variant table (shown only for ANNOVAR input) ── -->
  <c:if test="${fileType == 'ANNOVAR' and not empty annovarRecords}">
    <div class="pm-animate pm-animate-d1 mb-5">
      <div class="pm-step-header">
        <div class="pm-step-num">0</div>
        <div>
          <p class="pm-step-title">ANNOVAR Variant Summary</p>
          <p class="pm-step-sub">${fn:length(annovarRecords)} non-synonymous / actionable variants detected</p>
        </div>
      </div>
      <div class="pm-card">
        <div style="overflow-x:auto;width:100%;">
          <table class="pm-table pm-annovar-table" style="width:100%;table-layout:auto;">
            <thead>
              <tr>
                <th>Gene</th><th>Chr:Pos</th><th>Ref→Alt</th>
                <th>Exonic Function</th><th>AA Change</th><th>rsID</th><th>ClinSig</th>
              </tr>
            </thead>
            <tbody>
            <c:forEach items="${annovarRecords}" var="rec" end="49">
              <tr>
                <td><span class="pm-gene-pill">${rec.gene}</span></td>
                <td style="color:var(--muted);">${rec.chr}:${rec.start}</td>
                <td style="font-family:'Courier New',monospace;font-size:.8rem;">
                  ${rec.ref}&#8594;${rec.alt}
                </td>
                <td>
                  <c:choose>
                    <c:when test="${fn:containsIgnoreCase(rec.exonicFunc,'stop')}">
                      <span class="pm-exonic-badge pm-exonic-stop">${rec.exonicFunc}</span>
                    </c:when>
                    <c:when test="${fn:containsIgnoreCase(rec.exonicFunc,'missense')}">
                      <span class="pm-exonic-badge pm-exonic-missense">${rec.exonicFunc}</span>
                    </c:when>
                    <c:when test="${fn:containsIgnoreCase(rec.exonicFunc,'frameshift')}">
                      <span class="pm-exonic-badge pm-exonic-frameshift">${rec.exonicFunc}</span>
                    </c:when>
                    <c:otherwise>
                      <span class="pm-exonic-badge pm-exonic-other">${rec.exonicFunc}</span>
                    </c:otherwise>
                  </c:choose>
                </td>
                <td style="font-size:.78rem;color:var(--muted);">${fn:substring(rec.aaChange,0,40)}</td>
                <td style="font-size:.78rem;">${rec.rsId}</td>
                <td style="font-size:.78rem;color:var(--muted);">${rec.clnSig}</td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
        <c:if test="${fn:length(annovarRecords) > 50}">
          <div style="padding:.75rem 1.5rem;font-size:.8rem;color:var(--muted);border-top:1px solid var(--border);">
            Showing first 50 of ${fn:length(annovarRecords)} variants.
          </div>
        </c:if>
      </div>
    </div>
  </c:if>

  <!-- ── No haplotype calls ── -->
  <c:if test="${empty genoCalls}">
    <div class="pm-card pm-animate mb-4">
      <div class="pm-card-body text-center" style="padding:3rem;">
        <div style="font-size:2.5rem;margin-bottom:1rem;">&#129516;</div>
        <h4 style="font-family:var(--font-sans);font-weight:700;">No Pharmacogenomic Variants Detected</h4>
        <p style="color:var(--muted);max-width:480px;margin:.75rem auto 1.5rem;">
          None of the variants matched known pharmacogenomic rsIDs in the CPIC star-allele database.
          All variants may be homozygous reference, or rsID annotations may be missing.
        </p>
        <a href="<%=request.getContextPath()%>/matchingIndex" class="pm-btn pm-btn-primary">
          Upload Another File
        </a>
      </div>
    </div>
  </c:if>

  <c:if test="${not empty genoCalls}">

    <!-- ── Step 1: Phenotype table ── -->
    <div class="pm-step-header pm-animate pm-animate-d1">
      <div class="pm-step-num">1</div>
      <div>
        <p class="pm-step-title">Haplotype &amp; Phenotype Calls</p>
        <p class="pm-step-sub">${fn:length(genoCalls)} gene(s) with non-reference calls</p>
      </div>
    </div>

    <div class="pm-card mb-5 pm-animate pm-animate-d2">
      <div style="overflow-x:auto;width:100%;">
        <table class="pm-table" style="width:100%;table-layout:auto;">
          <thead>
            <tr>
              <th>Gene</th><th>Diplotype</th><th>Phenotype</th>
              <th style="text-align:center;">Activity Score</th><th>Matched rsIDs</th>
            </tr>
          </thead>
          <tbody>
          <c:forEach items="${genoCalls}" var="gc">
            <tr>
              <td><span class="pm-gene-pill">${gc.gene}</span></td>
              <td><span class="pm-diplotype">${gc.diplotype}</span></td>
              <td>
                <span class="pm-risk-tag ${gc.riskLevel}">
                  <c:choose>
                    <c:when test="${gc.riskLevel == 'danger'}">&#9940;</c:when>
                    <c:when test="${gc.riskLevel == 'warning'}">&#9888;&#65039;</c:when>
                    <c:otherwise>&#10004;</c:otherwise>
                  </c:choose>
                  ${gc.phenotype}
                </span>
              </td>
              <td style="text-align:center;"><span class="pm-chip">${gc.activityScore}</span></td>
              <td style="font-size:.8rem;color:var(--muted);">
                <c:forEach items="${gc.matchedRsids}" var="rs" varStatus="s">
                  ${rs}<c:if test="${!s.last}">, </c:if>
                </c:forEach>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ── Step 2: Drug recommendations ── -->
    <div class="pm-step-header pm-animate pm-animate-d2">
      <div class="pm-step-num">2</div>
      <div>
        <p class="pm-step-title">CPIC Drug Recommendations</p>
        <p class="pm-step-sub">${fn:length(recommendations)} recommendation(s) — sorted by risk</p>
      </div>
    </div>

    <!-- Legend -->
    <div class="d-flex flex-wrap mb-4 pm-animate pm-animate-d2" style="gap:.5rem;">
      <span class="pm-risk-tag danger">&#9940; Contraindicated / Avoid</span>
      <span class="pm-risk-tag warning">&#9888;&#65039; Dose Adjustment Required</span>
      <span class="pm-risk-tag success">&#10004; Standard Guidance</span>
    </div>

    <!-- Summary chart -->
    <c:if test="${not empty recommendations}">
      <div class="pm-card mb-4 pm-animate pm-animate-d2">
        <div class="pm-card-header">Risk Summary</div>
        <div class="pm-card-body" style="padding:1.25rem 1.5rem;">
          <c:set var="dCount" value="0"/>
          <c:set var="wCount" value="0"/>
          <c:set var="sCount" value="0"/>
          <c:forEach items="${recommendations}" var="r">
            <c:if test="${r.riskLevel == 'danger'}"><c:set var="dCount" value="${dCount + 1}"/></c:if>
            <c:if test="${r.riskLevel == 'warning'}"><c:set var="wCount" value="${wCount + 1}"/></c:if>
            <c:if test="${r.riskLevel == 'success'}"><c:set var="sCount" value="${sCount + 1}"/></c:if>
          </c:forEach>
          <div style="display:flex;gap:1.5rem;flex-wrap:wrap;">
            <div style="text-align:center;">
              <div style="font-size:2rem;font-weight:700;color:var(--red);">${dCount}</div>
              <div style="font-size:.78rem;color:var(--muted);">Contraindicated</div>
            </div>
            <div style="text-align:center;">
              <div style="font-size:2rem;font-weight:700;color:var(--yellow);">${wCount}</div>
              <div style="font-size:.78rem;color:var(--muted);">Dose Adjustment</div>
            </div>
            <div style="text-align:center;">
              <div style="font-size:2rem;font-weight:700;color:var(--green);">${sCount}</div>
              <div style="font-size:.78rem;color:var(--muted);">Standard</div>
            </div>
            <div style="text-align:center;">
              <div style="font-size:2rem;font-weight:700;color:var(--navy);">${fn:length(genoCalls)}</div>
              <div style="font-size:.78rem;color:var(--muted);">Genes Tested</div>
            </div>
          </div>
        </div>
      </div>
    </c:if>

    <c:if test="${empty recommendations}">
      <div class="pm-alert pm-alert-info pm-animate">
        <span>&#8505;&#65039;</span>
        <span>Phenotypes were called but no CPIC drug recommendations apply to these phenotypes.</span>
      </div>
    </c:if>

    <div class="row pm-animate pm-animate-d3">
    <c:forEach items="${recommendations}" var="rec">
      <div class="col-lg-4 col-md-6 mb-4">
        <div class="pm-drug-card">
          <div class="pm-drug-card-top ${rec.riskLevel}"></div>
          <div class="pm-drug-card-body">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <p class="pm-drug-name">&#128138; ${rec.drugName}</p>
              <span class="pm-cpic-badge">CPIC ${rec.cpicLevel}</span>
            </div>
            <div class="mb-2" style="font-size:.8rem;">
              <span class="pm-gene-pill">${rec.gene}</span>
              <span style="color:var(--muted);margin-left:.4rem;">${rec.phenotype}</span>
            </div>
            <div class="pm-drug-implication">${rec.implication}</div>
            <div class="pm-drug-rec">${rec.recommendation}</div>
          </div>
        </div>
      </div>
    </c:forEach>
    </div>

    <!-- Safety disclaimer -->
    <div class="pm-alert pm-alert-warning mt-2" style="font-size:.82rem;">
      <span>&#9888;&#65039;</span>
      <span>
        <strong>Clinical Disclaimer:</strong> This report is for research and educational purposes only.
        It does not constitute medical advice. All dosing decisions must be made by a qualified clinician.
        CPIC guidelines: <a href="https://cpicpgx.org" target="_blank">cpicpgx.org</a>.
      </span>
    </div>

  </c:if>
</c:if>

<jsp:include page="footer.jsp"/>
