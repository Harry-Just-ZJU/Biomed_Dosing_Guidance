package cn.edu.zju.vcf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CPIC-based haplotype (Star Allele) inference engine.
 *
 * For each gene:
 *   1. Look up which rsIDs from the patient VCF are relevant.
 *   2. For each rsID, read the patient's genotype (GT field).
 *   3. Determine which star alleles are present (het or hom).
 *   4. Assign the diplotype (e.g. *1/*2).
 *   5. Map diplotype → phenotype via CPIC activity score model.
 *
 * Genes covered (CPIC guidelines as of 2024):
 *   CYP2C19, CYP2C9, CYP2D6, TPMT, DPYD, SLCO1B1,
 *   VKORC1, UGT1A1, CYP3A5, NUDT15, G6PD, IFNL3
 */
public class HaplotypeEngine {

    private static final Logger log = LoggerFactory.getLogger(HaplotypeEngine.class);

    // -----------------------------------------------------------------------
    // Star Allele Definitions
    // Each entry: rsID -> ALT base that defines the star allele
    // Activity values: "Normal"=1.0, "Decreased"=0.5, "No function"=0.0,
    //                  "Increased"=1.5, "Uncertain"=0.0
    // -----------------------------------------------------------------------
    private static final List<StarAllele> STAR_ALLELES = new ArrayList<>();

    static {

        // ── CYP2C19 ─────────────────────────────────────────────────────────
        // *1 = wild-type (reference), activity = 1.0
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*2",
                Map.of("rs4244285", "A"), "No function"));        // c.681G>A, splice defect
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*3",
                Map.of("rs4986893", "A"), "No function"));        // c.636G>A, premature stop
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*4",
                Map.of("rs28399504", "A"), "No function"));       // c.1A>G, initiation codon
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*5",
                Map.of("rs56337013", "T"), "No function"));       // c.1297C>T
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*6",
                Map.of("rs72552267", "A"), "No function"));       // c.395G>A
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*7",
                Map.of("rs72558186", "A"), "No function"));       // splice defect
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*8",
                Map.of("rs41291556", "T"), "No function"));       // c.358T>C
        STAR_ALLELES.add(new StarAllele("CYP2C19", "*17",
                Map.of("rs12248560", "T"), "Increased"));         // c.-806C>T, increased expression

        // ── CYP2C9 ──────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*2",
                Map.of("rs1799853", "T"), "Decreased"));          // p.Arg144Cys
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*3",
                Map.of("rs1057910", "C"), "No function"));        // p.Ile359Leu
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*5",
                Map.of("rs28371686", "T"), "No function"));       // p.Asp360Glu
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*6",
                Map.of("rs9332131", "del"), "No function"));      // frameshift
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*8",
                Map.of("rs7900194", "T"), "Decreased"));          // p.Arg150His
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*11",
                Map.of("rs28371685", "T"), "Decreased"));         // p.Pro489Ser
        STAR_ALLELES.add(new StarAllele("CYP2C9", "*12",
                Map.of("rs9332239", "C"), "Decreased"));

        // ── CYP2D6 ──────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*3",
                Map.of("rs35742686", "del"), "No function"));     // frameshift
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*4",
                Map.of("rs3892097", "A"), "No function"));        // splice defect
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*5",
                Map.of("rs5030655", "del"), "No function"));      // gene deletion proxy
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*6",
                Map.of("rs5030655", "A"), "No function"));        // frameshift
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*10",
                Map.of("rs1135840", "C"), "Decreased"));          // p.Pro34Ser (East Asian)
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*17",
                Map.of("rs28371706", "C"), "Decreased"));         // p.Thr107Ile (African)
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*29",
                Map.of("rs59421388", "T"), "Decreased"));
        STAR_ALLELES.add(new StarAllele("CYP2D6", "*41",
                Map.of("rs28371725", "A"), "Decreased"));         // splice defect
        // *2xN duplication → Ultrarapid (handled separately in phenotype logic)

        // ── TPMT ────────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("TPMT", "*2",
                Map.of("rs1800462", "C"), "No function"));        // p.Ala80Pro
        STAR_ALLELES.add(new StarAllele("TPMT", "*3A",           // defined by BOTH SNPs
                Map.of("rs1800460", "T", "rs1142345", "C"), "No function"));
        STAR_ALLELES.add(new StarAllele("TPMT", "*3B",
                Map.of("rs1800460", "T"), "No function"));        // p.Ala154Thr
        STAR_ALLELES.add(new StarAllele("TPMT", "*3C",
                Map.of("rs1142345", "C"), "No function"));        // p.Tyr240Cys
        STAR_ALLELES.add(new StarAllele("TPMT", "*4",
                Map.of("rs1800584", "A"), "No function"));        // splice defect

        // ── DPYD ────────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("DPYD", "*2A",
                Map.of("rs3918290", "T"), "No function"));        // splice defect IVS14+1G>A
        STAR_ALLELES.add(new StarAllele("DPYD", "*13",
                Map.of("rs55886062", "A"), "No function"));       // p.Ile560Ser
        STAR_ALLELES.add(new StarAllele("DPYD", "c.2846A>T",
                Map.of("rs67376798", "T"), "Decreased"));         // p.Asp949Val
        STAR_ALLELES.add(new StarAllele("DPYD", "HapB3",
                Map.of("rs56038477", "T"), "Decreased"));         // c.1236G>A tag SNP

        // ── SLCO1B1 ─────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("SLCO1B1", "*5",
                Map.of("rs4149056", "C"), "Decreased"));          // p.Val174Ala (statin myopathy)
        STAR_ALLELES.add(new StarAllele("SLCO1B1", "*15",
                Map.of("rs4149056", "C", "rs2306283", "G"), "Decreased"));
        STAR_ALLELES.add(new StarAllele("SLCO1B1", "*1B",
                Map.of("rs2306283", "G"), "Normal"));             // increased transport, protective

        // ── VKORC1 ──────────────────────────────────────────────────────────
        // VKORC1 uses a different model: haplotype A (sensitive) vs B (resistant)
        STAR_ALLELES.add(new StarAllele("VKORC1", "A_haplotype",
                Map.of("rs9923231", "T"), "Sensitive"));          // -1639G>A → low warfarin dose
        STAR_ALLELES.add(new StarAllele("VKORC1", "A_haplotype_tag2",
                Map.of("rs2359612", "C"), "Sensitive"));
        STAR_ALLELES.add(new StarAllele("VKORC1", "A_haplotype_tag3",
                Map.of("rs8050894", "C"), "Sensitive"));

        // ── UGT1A1 ──────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("UGT1A1", "*28",
                Map.of("rs8175347", "A7"), "Decreased"));         // TA repeat (7 vs 6)
        STAR_ALLELES.add(new StarAllele("UGT1A1", "*6",
                Map.of("rs4148323", "A"), "Decreased"));          // p.Gly71Arg (Asian)
        STAR_ALLELES.add(new StarAllele("UGT1A1", "*37",
                Map.of("rs8175347", "A8"), "Decreased"));         // 8 TA repeats
        STAR_ALLELES.add(new StarAllele("UGT1A1", "*36",
                Map.of("rs8175347", "A5"), "Normal"));            // 5 TA repeats

        // ── CYP3A5 ──────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("CYP3A5", "*3",
                Map.of("rs776746", "A"), "No function"));         // splice defect (most common non-expressor)
        STAR_ALLELES.add(new StarAllele("CYP3A5", "*6",
                Map.of("rs10264272", "C"), "No function"));       // splice defect
        STAR_ALLELES.add(new StarAllele("CYP3A5", "*7",
                Map.of("rs41303343", "T"), "No function"));       // frameshift

        // ── NUDT15 ──────────────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("NUDT15", "*2",
                Map.of("rs746071566", "A"), "No function"));
        STAR_ALLELES.add(new StarAllele("NUDT15", "*3",
                Map.of("rs116855232", "T"), "No function"));      // p.Arg139Cys (Asian)
        STAR_ALLELES.add(new StarAllele("NUDT15", "*4",
                Map.of("rs147390019", "A"), "No function"));
        STAR_ALLELES.add(new StarAllele("NUDT15", "*5",
                Map.of("rs186364861", "T"), "Decreased"));

        // ── G6PD ────────────────────────────────────────────────────────────
        // G6PD is X-linked; simplified model here
        STAR_ALLELES.add(new StarAllele("G6PD", "A-",
                Map.of("rs1050828", "T"), "Deficient"));          // c.202G>A (African A-)
        STAR_ALLELES.add(new StarAllele("G6PD", "A",
                Map.of("rs1050829", "T"), "Variable"));           // c.376A>G
        STAR_ALLELES.add(new StarAllele("G6PD", "Mediterranean",
                Map.of("rs5030868", "T"), "Deficient"));          // c.563C>T

        // ── IFNL3 (IL28B) ───────────────────────────────────────────────────
        STAR_ALLELES.add(new StarAllele("IFNL3", "rs12979860_C",
                Map.of("rs12979860", "C"), "Favorable"));         // favorable response to IFN
        STAR_ALLELES.add(new StarAllele("IFNL3", "rs8099917_T",
                Map.of("rs8099917", "T"), "Favorable"));
    }

    // -----------------------------------------------------------------------
    // Activity score → phenotype mapping (CPIC standard)
    // -----------------------------------------------------------------------

    /** Activity score values per allele name */
    private static final Map<String, Double> ACTIVITY_SCORES = new HashMap<>();
    static {
        ACTIVITY_SCORES.put("Normal",    1.0);
        ACTIVITY_SCORES.put("Increased", 1.5);
        ACTIVITY_SCORES.put("Decreased", 0.5);
        ACTIVITY_SCORES.put("No function", 0.0);
        ACTIVITY_SCORES.put("Uncertain", 0.0);
        ACTIVITY_SCORES.put("Sensitive", 0.0);  // VKORC1 uses different scale
        ACTIVITY_SCORES.put("Variable",  0.5);
        ACTIVITY_SCORES.put("Deficient", 0.0);
        ACTIVITY_SCORES.put("Favorable", 1.0);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Given a map of rsID → genotype string (e.g. "0/1", "1/1", "0/0"),
     * infer diplotypes and phenotypes for all relevant genes.
     *
     * @param patientGenotypes  rsID → GT field value from VCF
     * @return list of GenotypeCall, one per gene with a non-reference call
     */
    public List<GenotypeCall> inferGenotypes(Map<String, String> patientGenotypes) {
        List<GenotypeCall> results = new ArrayList<>();

        // Group star alleles by gene
        Map<String, List<StarAllele>> byGene = new LinkedHashMap<>();
        for (StarAllele sa : STAR_ALLELES) {
            byGene.computeIfAbsent(sa.getGene(), k -> new ArrayList<>()).add(sa);
        }

        for (Map.Entry<String, List<StarAllele>> entry : byGene.entrySet()) {
            String gene = entry.getKey();
            List<StarAllele> geneAlleles = entry.getValue();

            GenotypeCall call = callDiplotype(gene, geneAlleles, patientGenotypes);
            if (call != null) {
                results.add(call);
                log.info("Haplotype call: {}", call);
            }
        }
        return results;
    }

    // -----------------------------------------------------------------------
    // Private logic
    // -----------------------------------------------------------------------

    private GenotypeCall callDiplotype(String gene,
                                        List<StarAllele> geneAlleles,
                                        Map<String, String> patientGenotypes) {

        // Collect which star alleles the patient carries, and how many copies
        // allele → copy count (1 = heterozygous, 2 = homozygous)
        Map<StarAllele, Integer> presentAlleles = new LinkedHashMap<>();
        List<String> matchedRsids = new ArrayList<>();

        for (StarAllele sa : geneAlleles) {
            int copiesPresent = copiesOfAllele(sa, patientGenotypes, matchedRsids);
            if (copiesPresent > 0) {
                presentAlleles.put(sa, copiesPresent);
            }
        }

        // If no variant alleles detected → assume *1/*1 (wild-type) — skip reporting
        if (presentAlleles.isEmpty()) {
            return null;
        }

        // Build diplotype: assign the two alleles
        // Simple model: take the highest-priority (most-impactful) alleles
        String allele1 = "*1";
        String allele2 = "*1";
        double score1  = 1.0;
        double score2  = 1.0;

        List<Map.Entry<StarAllele, Integer>> sorted = new ArrayList<>(presentAlleles.entrySet());
        // Sort: No function first, then Decreased, then Increased
        sorted.sort((a, b) -> activityRank(a.getKey()) - activityRank(b.getKey()));

        if (!sorted.isEmpty()) {
            StarAllele first = sorted.get(0).getKey();
            int copies = sorted.get(0).getValue();
            allele1 = first.getAllele();
            score1  = getActivity(first);
            if (copies >= 2) {
                // Homozygous: both alleles are this star allele
                allele2 = first.getAllele();
                score2  = getActivity(first);
            } else if (sorted.size() >= 2) {
                // Heterozygous compound: second variant allele
                StarAllele second = sorted.get(1).getKey();
                allele2 = second.getAllele();
                score2  = getActivity(second);
            }
            // else heterozygous with *1: allele2 stays *1
        }

        double totalScore = score1 + score2;

        // Special handling per gene
        if ("VKORC1".equals(gene))   return buildVkorc1Call(gene, allele1, allele2, totalScore, matchedRsids);
        if ("UGT1A1".equals(gene))   return buildUgt1a1Call(gene, allele1, allele2, totalScore, matchedRsids);
        if ("G6PD".equals(gene))     return buildG6pdCall(gene, allele1, allele2, totalScore, matchedRsids);
        if ("IFNL3".equals(gene))    return buildIfnl3Call(gene, allele1, allele2, matchedRsids);
        if ("CYP3A5".equals(gene))   return buildCyp3a5Call(gene, allele1, allele2, totalScore, matchedRsids);
        if ("SLCO1B1".equals(gene))  return buildSlco1b1Call(gene, allele1, allele2, totalScore, matchedRsids);

        // Default: activity-score based phenotype
        String diplotype    = allele1 + "/" + allele2;
        String[] phenoResult = activityScoreToPhenotype(gene, totalScore);
        return new GenotypeCall(gene, allele1, allele2, diplotype,
                phenoResult[0], phenoResult[1], totalScore, matchedRsids);
    }

    /** How many copies of this star allele does the patient carry? */
    private int copiesOfAllele(StarAllele sa,
                                Map<String, String> patientGenotypes,
                                List<String> matchedRsids) {
        for (Map.Entry<String, String> variant : sa.getDefiningVariants().entrySet()) {
            String rsid = variant.getKey().toLowerCase();
            String gt   = patientGenotypes.get(rsid);
            if (gt == null) continue;

            // Parse GT: "0/1", "1/1", "0|1", "1|0", etc.
            int altCount = countAltAlleles(gt);
            if (altCount > 0) {
                if (!matchedRsids.contains(rsid)) matchedRsids.add(rsid);
                return altCount;
            }
        }
        return 0;
    }

    /** Count ALT alleles in a GT string (0=REF, 1=ALT) */
    private int countAltAlleles(String gt) {
        if (gt == null || gt.equals(".") || gt.equals("./.") || gt.equals(".|.")) return 0;
        String[] parts = gt.replace('|', '/').split("/");
        int count = 0;
        for (String p : parts) {
            try {
                if (Integer.parseInt(p.trim()) > 0) count++;
            } catch (NumberFormatException ignored) {}
        }
        return count;
    }

    /** Sort by impact: No function (0) < Decreased (1) < Uncertain (2) < Normal/Increased (3) */
    private int activityRank(StarAllele sa) {
        switch (sa.getActivityValue()) {
            case "No function": return 0;
            case "Deficient":   return 0;
            case "Decreased":   return 1;
            case "Variable":    return 2;
            case "Uncertain":   return 2;
            case "Sensitive":   return 1;
            default:            return 3;
        }
    }

    private double getActivity(StarAllele sa) {
        return ACTIVITY_SCORES.getOrDefault(sa.getActivityValue(), 0.0);
    }

    // ── Gene-specific phenotype builders ────────────────────────────────────

    /**
     * CPIC activity-score model for CYP2C19, CYP2C9, CYP2D6, TPMT, DPYD, NUDT15
     */
    private String[] activityScoreToPhenotype(String gene, double score) {
        // CYP2D6 / CYP2C19 / CYP2C9 use the same thresholds
        if (score == 0.0) return new String[]{"Poor Metabolizer", "PM"};
        if (score <= 0.5) return new String[]{"Poor Metabolizer", "PM"};
        if (score <= 1.0) return new String[]{"Intermediate Metabolizer", "IM"};
        if (score <= 2.0) return new String[]{"Normal Metabolizer", "NM"};
        return new String[]{"Ultrarapid Metabolizer", "UM"};
    }

    private GenotypeCall buildVkorc1Call(String gene,
                                          String a1, String a2,
                                          double score,
                                          List<String> rsids) {
        // VKORC1: A/A haplotype = low warfarin dose; A/B = intermediate; B/B = high
        boolean hasA = a1.contains("A_haplotype") || a2.contains("A_haplotype");
        boolean bothA = a1.contains("A_haplotype") && a2.contains("A_haplotype");
        String phenotype, code;
        if (bothA) {
            phenotype = "Sensitive (Low Dose Required)";  code = "PM";
        } else if (hasA) {
            phenotype = "Intermediate Sensitivity";       code = "IM";
        } else {
            phenotype = "Normal Sensitivity";             code = "NM";
        }
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, score, rsids);
    }

    private GenotypeCall buildUgt1a1Call(String gene,
                                          String a1, String a2,
                                          double score,
                                          List<String> rsids) {
        String phenotype, code;
        boolean hasStar28 = "*28".equals(a1) || "*28".equals(a2);
        boolean hasStar6  = "*6".equals(a1)  || "*6".equals(a2);
        boolean bothDecreased = (score <= 1.0);
        if (score == 0.0 || bothDecreased && (hasStar28 && hasStar6)) {
            phenotype = "Poor Metabolizer";        code = "PM";
        } else if (hasStar28 || hasStar6) {
            phenotype = "Intermediate Metabolizer"; code = "IM";
        } else {
            phenotype = "Normal Metabolizer";       code = "NM";
        }
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, score, rsids);
    }

    private GenotypeCall buildCyp3a5Call(String gene,
                                          String a1, String a2,
                                          double score,
                                          List<String> rsids) {
        // CYP3A5: *1 = expresser; *3, *6, *7 = non-expresser
        // *1/*1 or *1/*3 = Expresser; *3/*3 = Non-expresser
        boolean hasWild = "*1".equals(a1) || "*1".equals(a2);
        String phenotype, code;
        if (score == 0.0) {
            phenotype = "Poor Expresser (Non-Expresser)"; code = "PM";
        } else if (hasWild) {
            phenotype = "Expresser";                      code = "NM";
        } else {
            phenotype = "Intermediate Expresser";          code = "IM";
        }
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, score, rsids);
    }

    private GenotypeCall buildSlco1b1Call(String gene,
                                           String a1, String a2,
                                           double score,
                                           List<String> rsids) {
        String phenotype, code;
        boolean hasStar5 = "*5".equals(a1) || "*5".equals(a2) ||
                           "*15".equals(a1) || "*15".equals(a2);
        boolean bothLow = score <= 1.0;
        if (bothLow && hasStar5) {
            phenotype = "Poor Function";          code = "PM";
        } else if (hasStar5) {
            phenotype = "Decreased Function";     code = "IM";
        } else {
            phenotype = "Normal Function";        code = "NM";
        }
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, score, rsids);
    }

    private GenotypeCall buildG6pdCall(String gene,
                                        String a1, String a2,
                                        double score,
                                        List<String> rsids) {
        String phenotype, code;
        // X-linked: males hemizygous, simplified here
        if (score == 0.0) {
            phenotype = "Deficient";               code = "PM";
        } else if (score <= 0.5) {
            phenotype = "Variable / Intermediate"; code = "IM";
        } else {
            phenotype = "Normal";                  code = "NM";
        }
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, score, rsids);
    }

    private GenotypeCall buildIfnl3Call(String gene,
                                         String a1, String a2,
                                         List<String> rsids) {
        // IFNL3: CC genotype = favorable IFN response
        boolean isFavorable = a1.contains("Favorable") || a2.contains("Favorable");
        String phenotype = isFavorable
                ? "Favorable Response to Interferon"
                : "Unfavorable Response to Interferon";
        String code = isFavorable ? "NM" : "PM";
        String diplotype = a1 + "/" + a2;
        return new GenotypeCall(gene, a1, a2, diplotype, phenotype, code, 1.0, rsids);
    }
}
