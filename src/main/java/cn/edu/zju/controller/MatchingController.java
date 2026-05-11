package cn.edu.zju.controller;

import cn.edu.zju.bean.*;
import cn.edu.zju.dao.*;
import cn.edu.zju.servlet.DispatchServlet;
import cn.edu.zju.vcf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);

    private final SampleDao     sampleDao     = new SampleDao();
    private final VcfVariantDao vcfVariantDao = new VcfVariantDao();

    private final VcfParser                vcfParser       = new VcfParser();
    private final AnnovarParser            annovarParser   = new AnnovarParser();
    private final HaplotypeEngine          haplotypeEngine = new HaplotypeEngine();
    private final CpicRecommendationEngine cpicEngine      = new CpicRecommendationEngine();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/matchingIndex", this::matchingIndex);
        dispatcher.registerGetMapping("/samples",       this::samples);
        dispatcher.registerGetMapping("/matching",      this::matching);
        dispatcher.registerPostMapping("/upload",       this::uploadFile);
    }

    public void matchingIndex(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (AuthController.requireLogin(req, resp)) return;
        req.getRequestDispatcher("/views/matching_index.jsp").forward(req, resp);
    }

    public void samples(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (AuthController.requireLogin(req, resp)) return;
        req.setAttribute("samples", sampleDao.findAll());
        req.getRequestDispatcher("/views/samples.jsp").forward(req, resp);
    }

    public void matching(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (AuthController.requireLogin(req, resp)) return;
        String param = req.getParameter("sampleId");
        if (param == null) { resp.sendRedirect("samples"); return; }
        int sampleId;
        try { sampleId = Integer.parseInt(param.trim()); }
        catch (NumberFormatException e) { resp.sendRedirect("samples"); return; }
        Sample sample = sampleDao.findById(sampleId);
        if (sample == null) { resp.sendRedirect("samples"); return; }
        List<String> genes = vcfVariantDao.findGenesBySampleId(sampleId);
        req.setAttribute("sample",          sample);
        req.setAttribute("genes",           genes);
        req.setAttribute("genoCalls",       new ArrayList<>());
        req.setAttribute("recommendations", new ArrayList<>());
        req.setAttribute("annovarRecords",  new ArrayList<>());
        req.setAttribute("rerunMode",       Boolean.TRUE);
        req.getRequestDispatcher("/views/matching_index_search.jsp").forward(req, resp);
    }

    public void uploadFile(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        if (AuthController.requireLogin(req, resp)) return;

        String uploadedBy = req.getParameter("uploaded_by");
        if (uploadedBy == null || uploadedBy.isBlank()) {
            forwardError(req, resp, "Patient name / ID cannot be blank."); return;
        }

        Part part = null;
        try { part = req.getPart("vcfFile"); } catch (Exception e) {
            forwardError(req, resp, "File upload failed: " + e.getMessage()); return;
        }
        if (part == null || part.getSize() == 0) {
            forwardError(req, resp, "Please select a file to upload."); return;
        }

        String filename = getFilename(part);
        if (filename == null || filename.isBlank()) filename = "upload.txt";
        String fnl = filename.toLowerCase();
        if (!fnl.endsWith(".vcf") && !fnl.endsWith(".vcf.gz")
                && !fnl.endsWith(".txt") && !fnl.endsWith(".tsv") && !fnl.endsWith(".csv")) {
            forwardError(req, resp, "Accepted: .vcf / .vcf.gz (VCF) or .txt / .tsv (ANNOVAR table)");
            return;
        }

        byte[] bytes;
        try (InputStream in = part.getInputStream()) { bytes = in.readAllBytes(); }
        if (bytes == null || bytes.length == 0) {
            forwardError(req, resp, "Uploaded file is empty."); return;
        }
        String content = new String(bytes, StandardCharsets.UTF_8);

        FileTypeDetector.FileType fileType = FileTypeDetector.detect(content);
        log.info("Detected: {} for {}", fileType, filename);

        int sampleId;
        try { sampleId = sampleDao.save(uploadedBy.trim(), filename); }
        catch (Exception e) {
            forwardError(req, resp, "Database error: " + e.getMessage()); return;
        }
        if (sampleId < 0) {
            forwardError(req, resp,
                    "Could not create sample record. Check MySQL and run migrate.sql."); return;
        }

        if (fileType == FileTypeDetector.FileType.ANNOVAR_TABLE) {
            handleAnnovar(req, resp, content, sampleId);
        } else {
            handleVcf(req, resp, content, sampleId);
        }
    }

    private void handleVcf(HttpServletRequest req, HttpServletResponse resp,
                            String content, int sampleId)
            throws ServletException, IOException {
        long dataLines = Arrays.stream(content.split("\\r?\\n"))
                .filter(l -> !l.trim().isEmpty() && !l.trim().startsWith("#")).count();
        if (dataLines == 0) {
            forwardError(req, resp, "No variant data lines in VCF."); return;
        }
        List<VcfVariant> variants;
        try { variants = vcfParser.parseVariants(sampleId, content); }
        catch (Exception e) { forwardError(req, resp, "VCF parsing failed: " + e.getMessage()); return; }
        if (variants.isEmpty()) {
            forwardError(req, resp, "No variants parsed. Check VCF 4.x format."); return;
        }
        Map<String, String>      genotypeMap = vcfParser.buildGenotypeMap(variants);
        List<GenotypeCall>       genoCalls   = haplotypeEngine.inferGenotypes(genotypeMap);
        List<DrugRecommendation> recs        = cpicEngine.getRecommendations(genoCalls);
        List<String>             genes       = vcfParser.extractGenes(variants);
        for (GenotypeCall gc : genoCalls) if (!genes.contains(gc.getGene())) genes.add(gc.getGene());
        try { vcfVariantDao.saveVariants(sampleId, variants); vcfVariantDao.saveGenes(sampleId, genes); }
        catch (Exception e) { log.warn("Persistence: {}", e.getMessage()); }
        forwardReport(req, resp, sampleId, genoCalls, recs, genes, "VCF", new ArrayList<>());
    }

    private void handleAnnovar(HttpServletRequest req, HttpServletResponse resp,
                                String content, int sampleId)
            throws ServletException, IOException {
        List<AnnovarParser.AnnovarRecord> records;
        try { records = annovarParser.parse(content); }
        catch (Exception e) { forwardError(req, resp, "ANNOVAR parsing failed: " + e.getMessage()); return; }
        if (records.isEmpty()) {
            forwardError(req, resp,
                    "No actionable variants in ANNOVAR file. All may be synonymous/intronic."); return;
        }
        Map<String, String>      genotypeMap = annovarParser.buildGenotypeMap(records);
        List<GenotypeCall>       genoCalls   = haplotypeEngine.inferGenotypes(genotypeMap);
        List<DrugRecommendation> recs        = cpicEngine.getRecommendations(genoCalls);
        List<String>             genes       = annovarParser.extractGenes(records);
        try { vcfVariantDao.saveGenes(sampleId, genes); }
        catch (Exception e) { log.warn("saveGenes: {}", e.getMessage()); }
        forwardReport(req, resp, sampleId, genoCalls, recs, genes, "ANNOVAR", records);
    }

    private void forwardReport(HttpServletRequest req, HttpServletResponse resp,
                                int sampleId,
                                List<GenotypeCall> genoCalls,
                                List<DrugRecommendation> recs,
                                List<String> genes,
                                String fileType,
                                List<AnnovarParser.AnnovarRecord> annovarRecords)
            throws ServletException, IOException {
        Sample sample = sampleDao.findById(sampleId);
        req.setAttribute("sample",          sample);
        req.setAttribute("genoCalls",       genoCalls);
        req.setAttribute("recommendations", recs);
        req.setAttribute("genes",           genes);
        req.setAttribute("fileType",        fileType);
        req.setAttribute("annovarRecords",  annovarRecords);
        req.getRequestDispatcher("/views/matching_index_search.jsp").forward(req, resp);
    }

    private void forwardError(HttpServletRequest req, HttpServletResponse resp, String msg)
            throws ServletException, IOException {
        log.warn("Error: {}", msg);
        req.setAttribute("validateError", msg);
        req.getRequestDispatcher("/views/matching_index_error.jsp").forward(req, resp);
    }

    private String getFilename(Part part) {
        String cd = part.getHeader("content-disposition");
        if (cd == null) return null;
        for (String token : cd.split(";")) {
            token = token.trim();
            if (token.startsWith("filename")) {
                String name = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                return slash >= 0 ? name.substring(slash + 1) : name;
            }
        }
        return null;
    }
}
