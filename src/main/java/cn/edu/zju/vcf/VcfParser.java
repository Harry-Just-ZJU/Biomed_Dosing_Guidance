package cn.edu.zju.vcf;

import cn.edu.zju.bean.VcfVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parses a standard VCF 4.x file into:
 *   1. List of VcfVariant records (for persistence)
 *   2. Map of rsID -> GT string (for haplotype inference)
 */
public class VcfParser {

    private static final Logger log = LoggerFactory.getLogger(VcfParser.class);

    /**
     * Parse VCF content and return all variant records.
     */
    public List<VcfVariant> parseVariants(int sampleId, String vcfContent) {
        List<VcfVariant> variants = new ArrayList<>();
        if (vcfContent == null || vcfContent.isBlank()) return variants;

        String[] lines = vcfContent.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] cols = line.split("\t", -1);
            if (cols.length < 5) continue;

            try {
                String chrom    = normaliseChrom(cols[0]);
                long   pos      = Long.parseLong(cols[1].trim());
                String varId    = cols.length > 2 ? cols[2].trim() : ".";
                String ref      = cols.length > 3 ? cols[3].trim() : ".";
                String alt      = cols.length > 4 ? cols[4].trim() : ".";
                String qual     = cols.length > 5 ? cols[5].trim() : ".";
                String filter   = cols.length > 6 ? cols[6].trim() : ".";
                String info     = cols.length > 7 ? cols[7].trim() : ".";
                String genotype = extractGT(cols);

                variants.add(new VcfVariant(sampleId, chrom, pos, varId,
                        ref, alt, qual, filter, info, genotype));
            } catch (NumberFormatException e) {
                log.debug("Skipping non-data line: {}", line);
            }
        }
        log.info("Parsed {} variants for sampleId={}", variants.size(), sampleId);
        return variants;
    }

    /**
     * Build a map of rsID (lowercase) -> GT string from parsed variants.
     * Only includes variants with a known rsID (starting with "rs").
     * Skips reference-only calls (GT = 0/0).
     */
    public Map<String, String> buildGenotypeMap(List<VcfVariant> variants) {
        Map<String, String> map = new LinkedHashMap<>();
        for (VcfVariant v : variants) {
            String rsid = v.getVarId();
            if (rsid == null || !rsid.toLowerCase().startsWith("rs")) continue;

            String gt = v.getGenotype();
            if (gt == null || gt.equals(".") || gt.equals("./.") || gt.equals(".|.")) continue;

            // Skip homozygous reference (0/0)
            if (gt.equals("0/0") || gt.equals("0|0")) continue;

            map.put(rsid.toLowerCase(), gt);
        }
        log.info("Built genotype map with {} rsIDs", map.size());
        return map;
    }

    /** Extract gene symbols (for the legacy gene-list storage in vcf_gene table) */
    public List<String> extractGenes(List<VcfVariant> variants) {
        Set<String> genes = new LinkedHashSet<>();
        for (VcfVariant v : variants) {
            String info = v.getInfo();
            if (info != null && !info.equals(".")) {
                genes.addAll(extractFromAnn(info));
                genes.addAll(extractFromGeneInfo(info));
            }
        }
        List<String> result = new ArrayList<>(genes);
        Collections.sort(result);
        return result;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String extractGT(String[] cols) {
        // FORMAT = cols[8], SAMPLE = cols[9]
        if (cols.length < 10) return ".";
        String format = cols[8].trim();
        String sample = cols[9].trim();
        if (format.isEmpty() || sample.isEmpty()) return ".";

        String[] fmtFields = format.split(":");
        String[] smpFields = sample.split(":");
        for (int i = 0; i < fmtFields.length && i < smpFields.length; i++) {
            if ("GT".equals(fmtFields[i])) return smpFields[i];
        }
        // If no FORMAT, treat first token as GT
        return smpFields[0];
    }

    private List<String> extractFromAnn(String info) {
        List<String> genes = new ArrayList<>();
        if (!info.contains("ANN=")) return genes;
        String annBlock = info.replaceFirst(".*ANN=", "").replaceFirst(";.*", "");
        for (String entry : annBlock.split(",")) {
            String[] parts = entry.split("\\|", -1);
            if (parts.length >= 4) {
                String g = parts[3].trim();
                if (isValidGene(g)) genes.add(g);
            }
        }
        return genes;
    }

    private List<String> extractFromGeneInfo(String info) {
        List<String> genes = new ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("GENEINFO=([A-Za-z][A-Za-z0-9_\\-\\.]+):")
                .matcher(info);
        while (m.find()) {
            String g = m.group(1).trim();
            if (isValidGene(g)) genes.add(g);
        }
        return genes;
    }

    private String normaliseChrom(String raw) {
        String c = raw.trim();
        if (c.toLowerCase().startsWith("chr")) c = c.substring(3);
        return c;
    }

    private boolean isValidGene(String s) {
        if (s == null || s.length() < 2 || s.length() > 30) return false;
        if (!s.matches("[A-Za-z][A-Za-z0-9_\\-\\.]+"))      return false;
        Set<String> skip = new HashSet<>(Arrays.asList(
                "PASS","FAIL","GENE","INFO","FORMAT","REF","ALT","HIGH","LOW",
                "MODERATE","MODIFIER","SNP","SNV","INDEL","CHROM","POS","ID"));
        return !skip.contains(s.toUpperCase());
    }
}
