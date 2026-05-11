package cn.edu.zju.vcf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parses ANNOVAR table_annovar output files (.hg19_multianno.txt or similar).
 *
 * Expected header columns (subset that we care about):
 *   Chr, Start, End, Ref, Alt,
 *   Func.refGene, Gene.refGene, ExonicFunc.refGene, AAChange.refGene,
 *   avsnp150 (or avsnp147), CLNSIG, ...
 *
 * Strategy:
 *  1. Parse header to find column indices dynamically.
 *  2. For each data row, extract gene + rsID + ExonicFunc.
 *  3. Filter out synonymous SNVs and intronic variants (same as old AnnovarDao).
 *  4. Build genotype map: rsID → "0/1" (heterozygous, since ANNOVAR output
 *     doesn't always carry GT — we assume het for any listed variant).
 */
public class AnnovarParser {

    private static final Logger log = LoggerFactory.getLogger(AnnovarParser.class);

    public static class AnnovarRecord {
        public String chr, ref, alt, gene, exonicFunc, aaChange, rsId, clnSig;
        public long   start;
    }

    /**
     * Parse ANNOVAR table content.
     * Returns list of records; only non-synonymous / actionable variants included.
     */
    public List<AnnovarRecord> parse(String content) {
        List<AnnovarRecord> records = new ArrayList<>();
        if (content == null || content.isBlank()) return records;

        String[] lines = content.split("\\r?\\n");
        if (lines.length < 2) return records;

        // Parse header
        String[] headers = lines[0].split("\t", -1);
        Map<String, Integer> colIdx = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            colIdx.put(headers[i].trim(), i);
        }

        // Required columns
        int iChr         = col(colIdx, "Chr");
        int iStart       = col(colIdx, "Start");
        int iRef         = col(colIdx, "Ref");
        int iAlt         = col(colIdx, "Alt");
        int iGene        = col(colIdx, "Gene.refGene");
        int iExonic      = col(colIdx, "ExonicFunc.refGene");
        int iFunc        = col(colIdx, "Func.refGene");
        int iAA          = col(colIdx, "AAChange.refGene");
        // rsID column — try multiple names
        int iRsId        = firstCol(colIdx, "avsnp150", "avsnp147", "snp138", "ID");
        int iClnSig      = firstCol(colIdx, "CLNSIG", "clinvar_clnsig");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split("\t", -1);

            // Filter: skip synonymous and purely intronic variants
            String exonicFunc = safe(cols, iExonic);
            String func       = safe(cols, iFunc);
            if ("synonymous SNV".equalsIgnoreCase(exonicFunc)) continue;
            if ("intronic".equalsIgnoreCase(func)) continue;
            if ("ncRNA_intronic".equalsIgnoreCase(func)) continue;

            AnnovarRecord r = new AnnovarRecord();
            r.chr        = safe(cols, iChr);
            r.ref        = safe(cols, iRef);
            r.alt        = safe(cols, iAlt);
            r.gene       = safe(cols, iGene);
            r.exonicFunc = exonicFunc;
            r.aaChange   = safe(cols, iAA);
            r.clnSig     = safe(cols, iClnSig);
            r.rsId       = cleanRsId(safe(cols, iRsId));

            try { r.start = Long.parseLong(safe(cols, iStart)); }
            catch (NumberFormatException ignored) { r.start = 0; }

            // Skip empty genes
            if (r.gene.isEmpty() || ".".equals(r.gene)) continue;

            records.add(r);
        }

        log.info("ANNOVAR: parsed {} actionable records from {} lines",
                records.size(), lines.length - 1);
        return records;
    }

    /**
     * Build rsID → genotype map from ANNOVAR records.
     * Since ANNOVAR table output doesn't carry GT, we use "0/1" (heterozygous)
     * for any variant present in the table.
     */
    public Map<String, String> buildGenotypeMap(List<AnnovarRecord> records) {
        Map<String, String> map = new LinkedHashMap<>();
        for (AnnovarRecord r : records) {
            if (r.rsId != null && r.rsId.startsWith("rs")) {
                map.put(r.rsId.toLowerCase(), "0/1"); // assume heterozygous
            }
        }
        log.info("ANNOVAR genotype map: {} rsIDs", map.size());
        return map;
    }

    /**
     * Extract unique gene symbols from ANNOVAR records.
     * Gene.refGene may be "GENE1;GENE2" — split on semicolons.
     */
    public List<String> extractGenes(List<AnnovarRecord> records) {
        Set<String> genes = new LinkedHashSet<>();
        for (AnnovarRecord r : records) {
            if (r.gene == null || r.gene.isBlank() || ".".equals(r.gene)) continue;
            for (String g : r.gene.split(";")) {
                String trimmed = g.trim();
                if (!trimmed.isEmpty() && !".".equals(trimmed)) genes.add(trimmed);
            }
        }
        return new ArrayList<>(genes);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int col(Map<String, Integer> m, String name) {
        return m.getOrDefault(name, -1);
    }

    private int firstCol(Map<String, Integer> m, String... names) {
        for (String n : names) { if (m.containsKey(n)) return m.get(n); }
        return -1;
    }

    private String safe(String[] cols, int idx) {
        if (idx < 0 || idx >= cols.length) return "";
        String v = cols[idx].trim();
        return ".".equals(v) ? "" : v;
    }

    private String cleanRsId(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        // Sometimes multiple rsIDs are semicolon-separated; take the first
        String first = raw.split(";")[0].trim();
        return first.startsWith("rs") ? first : null;
    }
}
