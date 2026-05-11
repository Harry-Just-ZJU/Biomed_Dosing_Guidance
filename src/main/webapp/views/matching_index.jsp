<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<jsp:include page="header.jsp"><jsp:param name="activeMenu" value="matching_index"/></jsp:include>

<div class="pm-page-header pm-animate">
  <h2>Upload Genomic Data</h2>
  <p>Submit a VCF file or ANNOVAR table output. The system detects the format automatically.</p>
</div>

<div class="row">
  <div class="col-lg-6 col-md-8">
    <div class="pm-card pm-animate pm-animate-d1">
      <div class="pm-card-body">
        <form id="upload-form" method="post"
              action="<%=request.getContextPath()%>/upload"
              enctype="multipart/form-data">

          <div class="pm-form-group">
            <label class="pm-label" for="uploaded_by">
              Patient Name or ID <span style="color:var(--red)">*</span>
            </label>
            <input class="pm-input" type="text" id="uploaded_by" name="uploaded_by"
                   placeholder="e.g. Jane Smith or P-2024-001" required>
          </div>

          <hr class="pm-divider">
          <h6 style="font-family:var(--font-sans);font-weight:700;margin-bottom:1rem;">
            Select File
          </h6>

          <div class="pm-dropzone" id="drop-zone">
            <div class="pm-dropzone-icon">
              <svg width="22" height="22" fill="none" stroke="var(--teal)"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
              </svg>
            </div>
            <h6>Drag &amp; drop your file here</h6>
            <p>.vcf / .vcf.gz &nbsp;&bull;&nbsp; .txt / .tsv (ANNOVAR table) &nbsp;&bull;&nbsp; max 200 MB</p>
            <input type="file" id="file-input" name="vcfFile" style="display:none"
                   accept=".vcf,.vcf.gz,.txt,.tsv,.csv">
          </div>

          <div id="file-info" class="pm-alert pm-alert-success mt-3" style="display:none;"></div>
          <div id="error-msg" class="pm-alert pm-alert-danger  mt-3" style="display:none;"></div>
          <div id="type-hint" class="pm-alert pm-alert-info mt-3"    style="display:none;"></div>

          <div class="d-flex align-items-start mt-3 mb-4" style="gap:.6rem;">
            <input type="checkbox" id="privacyCheck" required style="margin-top:.2rem;flex-shrink:0;">
            <label for="privacyCheck" style="font-size:.82rem;color:var(--muted);margin:0;cursor:pointer;">
              This file is anonymised and I consent to the pharmacogenomic matching process.
            </label>
          </div>

          <button type="submit" id="submit-btn" class="pm-btn pm-btn-teal pm-btn-lg w-100"
                  style="justify-content:center;">
            Upload &amp; Analyse
          </button>
        </form>
      </div>
    </div>
  </div>

  <div class="col-lg-4 col-md-4 pm-animate pm-animate-d2">
    <div class="pm-card mb-3">
      <div class="pm-card-header">Supported Formats</div>
      <div class="pm-card-body" style="padding:1rem 1.25rem;">
        <div style="margin-bottom:.75rem;">
          <div style="font-weight:700;font-size:.88rem;">Standard VCF</div>
          <div style="font-size:.8rem;color:var(--muted);">
            VCF 4.x with rsID annotations in column 3.
            ANN=, CSQ=, GENEINFO= fields parsed automatically.
          </div>
        </div>
        <div>
          <div style="font-weight:700;font-size:.88rem;">ANNOVAR Table Output</div>
          <div style="font-size:.8rem;color:var(--muted);">
            table_annovar .hg19_multianno.txt files. Requires
            Gene.refGene and ExonicFunc.refGene columns.
          </div>
        </div>
      </div>
    </div>
    <div style="font-size:.78rem;color:var(--muted);padding:.25rem;">
      Need help? <a href="<%=request.getContextPath()%>/static/pdf/user_guide.pdf"
        target="_blank" style="color:var(--teal);">Download the user guide</a>.
    </div>
  </div>
</div>

<jsp:include page="footer.jsp"/>
<script>
const drop=document.getElementById('drop-zone'),fi=document.getElementById('file-input'),
      form=document.getElementById('upload-form'),err=document.getElementById('error-msg'),
      info=document.getElementById('file-info'),hint=document.getElementById('type-hint'),
      btn=document.getElementById('submit-btn');

drop.addEventListener('click',()=>fi.click());
drop.addEventListener('dragover',e=>{e.preventDefault();drop.classList.add('drag-over');});
drop.addEventListener('dragleave',()=>drop.classList.remove('drag-over'));
drop.addEventListener('drop',e=>{e.preventDefault();drop.classList.remove('drag-over');
  if(e.dataTransfer.files.length>0){fi.files=e.dataTransfer.files;validate();}});
fi.addEventListener('change',validate);

function validate(){
  [err,info,hint].forEach(el=>el.style.display='none');
  btn.disabled=false;
  const f=fi.files[0]; if(!f) return false;
  const n=f.name.toLowerCase();
  const validExt=['.vcf','.vcf.gz','.txt','.tsv','.csv'];
  if(!validExt.some(e=>n.endsWith(e))){showErr('Accepted: .vcf, .vcf.gz, .txt, .tsv');return false;}
  if(f.size>200*1024*1024){showErr('File exceeds 200 MB.');return false;}
  info.textContent='Ready: '+f.name+' ('+(f.size/1024).toFixed(1)+' KB)';
  info.style.display='flex';
  // Show format hint
  if(n.endsWith('.vcf')||n.endsWith('.vcf.gz')){
    hint.textContent='Detected format: Standard VCF — will use CPIC haplotype pipeline.';
    hint.style.display='flex';
  } else if(n.endsWith('.txt')||n.endsWith('.tsv')){
    hint.textContent='Detected format: ANNOVAR table — will parse Gene.refGene and ExonicFunc columns.';
    hint.style.display='flex';
  }
  return true;
}
function showErr(m){err.textContent=m;err.style.display='flex';fi.value='';btn.disabled=true;}
form.addEventListener('submit',e=>{
  if(!fi.files||!fi.files.length){e.preventDefault();showErr('Please select a file.');return;}
  if(!validate()){e.preventDefault();return;}
  btn.innerHTML='<span class="spinner-border spinner-border-sm mr-1"></span>Analysing\u2026';
  btn.classList.add('disabled');
});
</script>
