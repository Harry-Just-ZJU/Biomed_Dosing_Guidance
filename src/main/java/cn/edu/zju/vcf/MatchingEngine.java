package cn.edu.zju.vcf;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.bean.MatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matches a set of patient gene symbols against the pharmacogenomics
 * knowledge base (drug labels + dosing guidelines).
 *
 * Matching strategy:
 *  1. Gene-name substring search inside summaryMarkdown / textMarkdown /
 *     prescribingMarkdown of each DrugLabel.
 *  2. Gene-name search inside summaryMarkdown / textMarkdown of each
 *     DosingGuideline.
 *  3. Risk stratification based on keywords in the matched text.
 */
public class MatchingEngine {

    private static final Logger log = LoggerFactory.getLogger(MatchingEngine.class);

    /**
     * Run the full matching pipeline.
     *
     * @param patientGenes   distinct gene symbols extracted from the patient VCF
     * @param allLabels      all drug labels from the knowledge base
     * @param allGuidelines  all dosing guidelines from the knowledge base
     * @return list of MatchResult, deduplicated by drug label id, sorted by risk
     */
    public List<MatchResult> match(List<String>          patientGenes,
                                   List<DrugLabel>        allLabels,
                                   List<DosingGuideline>  allGuidelines) {

        if (patientGenes == null || patientGenes.isEmpty()) {
            log.warn("No patient genes supplied – returning empty result.");
            return Collections.emptyList();
        }

        // Index guidelines by drugId for O(1) lookup
        Map<String, List<DosingGuideline>> guidelinesByDrug = allGuidelines.stream()
                .collect(Collectors.groupingBy(DosingGuideline::getDrugId));

        List<MatchResult> results = new ArrayList<>();

        for (DrugLabel label : allLabels) {
            List<String> matchedGenes = genesMatchingLabel(patientGenes, label, allGuidelines);
            if (matchedGenes.isEmpty()) continue;

            // Fetch dosing guidelines for this drug
            List<DosingGuideline> relatedGuidelines =
                    guidelinesByDrug.getOrDefault(label.getDrugId(), Collections.emptyList());

            // Also include guidelines that mention any of the matched genes
            List<DosingGuideline> geneMatchedGuidelines = allGuidelines.stream()
                    .filter(g -> matchedGenes.stream()
                            .anyMatch(gene -> containsGene(g.getSummaryMarkdown(), gene)
                                          || containsGene(g.getTextMarkdown(), gene)))
                    .collect(Collectors.toList());

            // Merge the two lists, dedup by id
            Map<String, DosingGuideline> mergedMap = new LinkedHashMap<>();
            relatedGuidelines.forEach(g -> mergedMap.put(g.getId(), g));
            geneMatchedGuidelines.forEach(g -> mergedMap.put(g.getId(), g));

            List<DosingGuideline> finalGuidelines = new ArrayList<>(mergedMap.values());

            String riskLevel = assessRisk(label, finalGuidelines);

            results.add(new MatchResult(label, matchedGenes, finalGuidelines, riskLevel));
            log.debug("Matched label {} (drug {}) via genes {}", label.getId(),
                    label.getDrugId(), matchedGenes);
        }

        // Sort: danger first, then warning, then success
        results.sort(Comparator.comparingInt(r -> riskScore(r.getRiskLevel())));
        log.info("Matching complete: {} results for {} patient genes", results.size(), patientGenes.size());
        return results;
    }

    // -----------------------------------------------------------------------

    /**
     * Returns the subset of patientGenes that are mentioned in the label's
     * text fields, or in any guideline linked to the same drug.
     */
    private List<String> genesMatchingLabel(List<String>           patientGenes,
                                             DrugLabel              label,
                                             List<DosingGuideline>  allGuidelines) {
        // Build a combined text blob for the label
        String labelText = concat(label.getSummaryMarkdown(),
                                  label.getTextMarkdown(),
                                  label.getPrescribingMarkdown());

        // Build a text blob for all guidelines of this drug
        String guidelineText = allGuidelines.stream()
                .filter(g -> label.getDrugId() != null
                          && label.getDrugId().equals(g.getDrugId()))
                .map(g -> concat(g.getSummaryMarkdown(), g.getTextMarkdown()))
                .collect(Collectors.joining(" "));

        String combined = labelText + " " + guidelineText;

        return patientGenes.stream()
                .filter(gene -> containsGene(combined, gene))
                .collect(Collectors.toList());
    }

    /**
     * True if the text contains the gene symbol as a whole word (case-insensitive).
     * Requires word boundaries so "CYP2C9" doesn't match "CYP2C19".
     */
    private boolean containsGene(String text, String gene) {
        if (text == null || text.isBlank() || gene == null || gene.isBlank()) return false;
        // Use word boundary – the gene symbol must be surrounded by non-word chars
        String pattern = "(?i)(?<![A-Za-z0-9_\\-])" + java.util.regex.Pattern.quote(gene) + "(?![A-Za-z0-9_\\-])";
        return text.matches("(?s).*" + pattern + ".*");
    }

    /**
     * Assess drug interaction risk from the text content.
     * Returns "danger", "warning", or "success".
     */
    private String assessRisk(DrugLabel label, List<DosingGuideline> guidelines) {
        String combined = concat(label.getSummaryMarkdown(), label.getTextMarkdown(),
                label.getPrescribingMarkdown());
        combined += guidelines.stream()
                .map(g -> concat(g.getSummaryMarkdown(), g.getTextMarkdown()))
                .collect(Collectors.joining(" "));
        combined = combined.toLowerCase();

        if (containsAny(combined, "avoid", "contraindicated", "do not use",
                "not recommended", "fatal", "severe")) {
            return "danger";
        }
        if (containsAny(combined, "reduce", "adjust", "caution", "monitor",
                "increased risk", "lower dose", "use with caution")) {
            return "warning";
        }
        return "success";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private String concat(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p != null && !p.isBlank()) sb.append(p).append(" ");
        }
        return sb.toString();
    }

    /** Lower score = higher priority */
    private int riskScore(String level) {
        // After (Java 11 compatible)
        if ("danger".equals(level))  return 0;
        if ("warning".equals(level)) return 1;
        return 2;
    }
}
