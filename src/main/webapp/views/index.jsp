<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="dashboard"/></jsp:include>

<!-- Hero -->
<div class="pm-hero pm-animate mb-4">
  <h1>Genomic insights,<br><em>personalised</em> for you.</h1>
  <p>Upload your VCF or ANNOVAR file and receive evidence-based drug dosing
     recommendations powered by the CPIC pharmacogenomics standard.</p>
  <div class="pm-hero-actions">
    <a href="<%=request.getContextPath()%>/matchingIndex" class="pm-btn-hero-primary">
      Upload Genomic Data &#8594;
    </a>
    <a href="<%=request.getContextPath()%>/static/pdf/user_guide.pdf"
       target="_blank" class="pm-btn-hero-outline">
      &#128196; Download User Guide
    </a>
  </div>
</div>

<!-- How it works -->
<div class="row mb-4">
  <div class="col-md-5 mb-3">
    <div class="pm-card h-100">
      <div class="pm-card-header">How it works</div>
      <div class="pm-card-body" style="padding-top:1rem;">
        <div class="pm-step">
          <div class="pm-step-num">1</div>
          <div class="pm-step-content">
            <h5>Upload your genomic file</h5>
            <p>Submit a standard VCF 4.x file or an ANNOVAR table output (.txt/.tsv). The system automatically detects the format.</p>
          </div>
        </div>
        <div class="pm-step">
          <div class="pm-step-num">2</div>
          <div class="pm-step-content">
            <h5>CPIC haplotype inference</h5>
            <p>Your variants are matched against 60+ pharmacogenomic rsIDs across 12 key genes (CYP2C19, CYP2D6, TPMT, DPYD, and more).</p>
          </div>
        </div>
        <div class="pm-step">
          <div class="pm-step-num">3</div>
          <div class="pm-step-content">
            <h5>Personalised report</h5>
            <p>Receive a risk-stratified report with specific drug dosing recommendations, safety warnings, and CPIC evidence levels.</p>
          </div>
        </div>
        <div class="pm-step">
          <div class="pm-step-num">4</div>
          <div class="pm-step-content">
            <h5>Consult your clinician</h5>
            <p>Share the report with your pharmacist or physician to guide medication decisions.</p>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="col-md-7 mb-3">
    <div class="row">
      <div class="col-6 mb-3">
        <div class="pm-feature-card">
          <div class="pm-feature-icon">&#128274;</div>
          <h5>Secure &amp; Private</h5>
          <p>Your genomic data is processed locally and never shared without explicit consent.</p>
        </div>
      </div>
      <div class="col-6 mb-3">
        <div class="pm-feature-card">
          <div class="pm-feature-icon">&#128200;</div>
          <h5>Dual Format Support</h5>
          <p>Accepts both standard VCF files and pre-annotated ANNOVAR table outputs.</p>
        </div>
      </div>
      <div class="col-6 mb-3">
        <div class="pm-feature-card">
          <div class="pm-feature-icon">&#9889;</div>
          <h5>CPIC Standard</h5>
          <p>Haplotype inference across 12 major pharmacogenes using published CPIC A &amp; B guidelines.</p>
        </div>
      </div>
      <div class="col-6 mb-3">
        <div class="pm-feature-card">
          <div class="pm-feature-icon">&#128218;</div>
          <h5>Evidence-Based</h5>
          <p>Guidelines from CPIC, DPWG, and PharmGKB — the gold standard in pharmacogenomics.</p>
        </div>
      </div>
    </div>

    <!-- Genes covered -->
    <div class="pm-card">
      <div class="pm-card-header">Pharmacogenes Covered</div>
      <div class="pm-card-body" style="padding:1rem 1.5rem;">
        <c:forEach items="${geneList}" var="g">
          <span class="pm-gene-pill mr-1 mb-1">${g}</span>
        </c:forEach>
        <c:if test="${empty geneList}">
          <span class="pm-gene-pill mr-1">CYP2C19</span>
          <span class="pm-gene-pill mr-1">CYP2C9</span>
          <span class="pm-gene-pill mr-1">CYP2D6</span>
          <span class="pm-gene-pill mr-1">TPMT</span>
          <span class="pm-gene-pill mr-1">DPYD</span>
          <span class="pm-gene-pill mr-1">SLCO1B1</span>
          <span class="pm-gene-pill mr-1">VKORC1</span>
          <span class="pm-gene-pill mr-1">UGT1A1</span>
          <span class="pm-gene-pill mr-1">CYP3A5</span>
          <span class="pm-gene-pill mr-1">NUDT15</span>
          <span class="pm-gene-pill mr-1">G6PD</span>
          <span class="pm-gene-pill mr-1">IFNL3</span>
        </c:if>
      </div>
    </div>
  </div>
</div>

<jsp:include page="footer.jsp"/>
