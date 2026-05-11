package cn.edu.zju.vcf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * CPIC guideline recommendation engine.
 *
 * Given a GenotypeCall (gene + phenotype), returns concrete drug
 * recommendations drawn from published CPIC guidelines.
 *
 * Reference: https://cpicpgx.org/guidelines/
 * Guidelines implemented: CYP2C19, CYP2C9, CYP2D6, TPMT, DPYD,
 *                         SLCO1B1, VKORC1, UGT1A1, CYP3A5, NUDT15,
 *                         G6PD, IFNL3
 */
public class CpicRecommendationEngine {

    private static final Logger log = LoggerFactory.getLogger(CpicRecommendationEngine.class);

    // -----------------------------------------------------------------------
    // Recommendation table
    // Key: "GENE::PHENOTYPE_CODE"
    // Value: list of DrugRecommendation objects
    // -----------------------------------------------------------------------
    private static final Map<String, List<DrugRecommendation>> REC_TABLE = new LinkedHashMap<>();

    static {

        // ════════════════════════════════════════════════════════════════════
        // CYP2C19
        // CPIC guideline: doi 10.1111/cts.12452 (clopidogrel, SSRIs, PPIs, etc.)
        // ════════════════════════════════════════════════════════════════════
        put("CYP2C19", "PM",
                rec("CYP2C19", "Poor Metabolizer", "PM",
                        "Clopidogrel",
                        "Clopidogrel is contraindicated. Use an alternative antiplatelet agent " +
                        "(e.g., prasugrel or ticagrelor) at standard doses.",
                        "A", "danger",
                        "CYP2C19 PM cannot convert clopidogrel to its active metabolite, " +
                        "resulting in markedly reduced platelet inhibition and increased risk of " +
                        "cardiovascular events."),
                rec("CYP2C19", "Poor Metabolizer", "PM",
                        "Voriconazole",
                        "Voriconazole exposure is significantly increased in PM. " +
                        "Consider alternative antifungal (e.g., isavuconazole) or " +
                        "reduce dose and monitor drug levels closely.",
                        "A", "danger",
                        "CYP2C19 PM leads to markedly elevated voriconazole plasma levels, " +
                        "increasing risk of visual disturbances, hepatotoxicity and neurotoxicity."),
                rec("CYP2C19", "Poor Metabolizer", "PM",
                        "Sertraline / SSRIs",
                        "Initiate therapy with the lowest recommended dose. " +
                        "Titrate slowly and monitor for adverse effects (QTc prolongation, side effects).",
                        "A", "warning",
                        "CYP2C19 PM results in increased SSRI exposure."),
                rec("CYP2C19", "Poor Metabolizer", "PM",
                        "Omeprazole / PPIs",
                        "Standard or reduced dose is likely sufficient. " +
                        "Consider dose reduction (e.g., 20 mg omeprazole instead of 40 mg).",
                        "A", "warning",
                        "CYP2C19 PM leads to increased PPI exposure, which may enhance efficacy " +
                        "but can also increase side-effect risk with long-term use."),
                rec("CYP2C19", "Poor Metabolizer", "PM",
                        "Amitriptyline / Tricyclic antidepressants",
                        "Reduce starting dose by 50%. Titrate based on response and tolerability. " +
                        "Consider TDM.",
                        "A", "warning",
                        "Increased TCA plasma levels in CYP2C19 PM."));

        put("CYP2C19", "IM",
                rec("CYP2C19", "Intermediate Metabolizer", "IM",
                        "Clopidogrel",
                        "Consider alternative antiplatelet agent especially for high-risk patients " +
                        "(ACS, PCI). Prasugrel or ticagrelor preferred.",
                        "A", "warning",
                        "Reduced conversion of clopidogrel to active metabolite, " +
                        "leading to diminished platelet inhibition."),
                rec("CYP2C19", "Intermediate Metabolizer", "IM",
                        "Voriconazole",
                        "Monitor voriconazole plasma levels. " +
                        "Consider dose reduction if levels are supratherapeutic.",
                        "A", "warning",
                        "Moderately elevated voriconazole exposure in IM."),
                rec("CYP2C19", "Intermediate Metabolizer", "IM",
                        "Sertraline / SSRIs",
                        "Initiate at standard dose. Monitor for adverse effects.",
                        "B", "success",
                        "Slightly increased SSRI exposure, clinical significance uncertain."));

        put("CYP2C19", "UM",
                rec("CYP2C19", "Ultrarapid Metabolizer", "UM",
                        "Clopidogrel",
                        "Standard dose is appropriate. " +
                        "Monitor for unexpected bleeding risk due to enhanced platelet inhibition.",
                        "B", "warning",
                        "Increased conversion to active metabolite may cause excessive platelet inhibition."),
                rec("CYP2C19", "Ultrarapid Metabolizer", "UM",
                        "Sertraline / SSRIs",
                        "Consider alternative agent or increased dose. " +
                        "Monitor for reduced therapeutic efficacy.",
                        "A", "warning",
                        "Rapid metabolism leads to subtherapeutic SSRI plasma levels."),
                rec("CYP2C19", "Ultrarapid Metabolizer", "UM",
                        "Voriconazole",
                        "Voriconazole is likely ineffective. " +
                        "Use an alternative antifungal agent.",
                        "A", "danger",
                        "Rapid CYP2C19 metabolism results in subtherapeutic voriconazole levels."));

        // ════════════════════════════════════════════════════════════════════
        // CYP2C9  —  Warfarin, NSAIDs, Phenytoin, Siponimod
        // CPIC guideline: doi 10.1038/clpt.2011.185
        // ════════════════════════════════════════════════════════════════════
        put("CYP2C9", "PM",
                rec("CYP2C9", "Poor Metabolizer", "PM",
                        "Warfarin",
                        "Initiate with ≥40% lower than average dose. " +
                        "Use CPIC/IWPC warfarin dosing algorithm. " +
                        "Increase INR monitoring frequency during initiation.",
                        "A", "danger",
                        "CYP2C9 PM have significantly reduced warfarin clearance, " +
                        "leading to supertherapeutic anticoagulation and bleeding risk."),
                rec("CYP2C9", "Poor Metabolizer", "PM",
                        "NSAIDs (celecoxib, ibuprofen, flurbiprofen)",
                        "Use the lowest effective dose. " +
                        "Consider alternative analgesic (e.g., acetaminophen). " +
                        "Avoid long-term NSAID use.",
                        "A", "warning",
                        "Elevated NSAID plasma concentrations increase GI and cardiovascular risk."),
                rec("CYP2C9", "Poor Metabolizer", "PM",
                        "Phenytoin",
                        "Reduce phenytoin dose by 25–50%. " +
                        "Monitor plasma phenytoin levels closely.",
                        "A", "danger",
                        "Markedly reduced phenytoin metabolism leads to drug accumulation " +
                        "and neurotoxicity risk."),
                rec("CYP2C9", "Poor Metabolizer", "PM",
                        "Siponimod",
                        "Siponimod is contraindicated in CYP2C9 *3/*3 genotype. " +
                        "Avoid use.",
                        "A", "danger",
                        "Significantly increased siponimod exposure in PM."));

        put("CYP2C9", "IM",
                rec("CYP2C9", "Intermediate Metabolizer", "IM",
                        "Warfarin",
                        "Initiate with ≥25% lower than average dose. " +
                        "Use CPIC warfarin dosing algorithm incorporating CYP2C9 and VKORC1.",
                        "A", "warning",
                        "Moderately reduced warfarin clearance in CYP2C9 IM."),
                rec("CYP2C9", "Intermediate Metabolizer", "IM",
                        "NSAIDs",
                        "Use minimum effective dose. Monitor for adverse effects.",
                        "A", "warning",
                        "Moderately elevated NSAID plasma levels in CYP2C9 IM."),
                rec("CYP2C9", "Intermediate Metabolizer", "IM",
                        "Phenytoin",
                        "Initiate at low-normal dose. Monitor levels after 5–7 days.",
                        "A", "warning",
                        "Reduced phenytoin metabolism, moderate drug accumulation risk."));

        // ════════════════════════════════════════════════════════════════════
        // CYP2D6  —  Codeine, Tramadol, Tamoxifen, Antidepressants
        // CPIC guideline: doi 10.1038/clpt.2011.34
        // ════════════════════════════════════════════════════════════════════
        put("CYP2D6", "PM",
                rec("CYP2D6", "Poor Metabolizer", "PM",
                        "Codeine",
                        "Codeine is contraindicated. " +
                        "Use a non-opioid analgesic or an opioid not metabolised by CYP2D6 " +
                        "(e.g., morphine, oxycodone with dose reduction, buprenorphine).",
                        "A", "danger",
                        "CYP2D6 PM cannot convert codeine to morphine, resulting in lack of " +
                        "analgesia. Paradoxically accumulates norcodeine causing adverse effects."),
                rec("CYP2D6", "Poor Metabolizer", "PM",
                        "Tramadol",
                        "Tramadol is not recommended. " +
                        "Use an alternative analgesic not dependent on CYP2D6 activation.",
                        "A", "danger",
                        "Reduced formation of active O-desmethyltramadol, leading to poor analgesia."),
                rec("CYP2D6", "Poor Metabolizer", "PM",
                        "Tamoxifen",
                        "Consider alternative hormonal therapy (e.g., aromatase inhibitor " +
                        "for postmenopausal patients). If tamoxifen must be used, " +
                        "do not use higher doses — evidence insufficient.",
                        "A", "danger",
                        "CYP2D6 PM have reduced endoxifen (active metabolite) formation, " +
                        "significantly compromising tamoxifen efficacy in breast cancer treatment."),
                rec("CYP2D6", "Poor Metabolizer", "PM",
                        "Antidepressants (amitriptyline, nortriptyline, paroxetine, fluoxetine)",
                        "Avoid tricyclic antidepressants or use with 50% dose reduction. " +
                        "Consider alternative agent (e.g., citalopram, escitalopram).",
                        "A", "warning",
                        "Elevated TCA and some SSRI plasma levels in CYP2D6 PM."));

        put("CYP2D6", "UM",
                rec("CYP2D6", "Ultrarapid Metabolizer", "UM",
                        "Codeine",
                        "Codeine is contraindicated (FDA black-box warning). " +
                        "Use an alternative opioid analgesic.",
                        "A", "danger",
                        "CYP2D6 UM rapidly converts codeine to morphine, causing life-threatening " +
                        "opioid toxicity (respiratory depression, death reported in children)."),
                rec("CYP2D6", "Ultrarapid Metabolizer", "UM",
                        "Tramadol",
                        "Tramadol is not recommended due to risk of opioid toxicity. " +
                        "Use an alternative analgesic.",
                        "A", "danger",
                        "Rapid formation of active metabolite may cause opioid-related adverse effects."),
                rec("CYP2D6", "Ultrarapid Metabolizer", "UM",
                        "Tamoxifen",
                        "Standard tamoxifen dose (20 mg/day) is appropriate; " +
                        "endoxifen levels may be adequate.",
                        "B", "success",
                        "Increased CYP2D6 activity may produce higher endoxifen levels."),
                rec("CYP2D6", "Ultrarapid Metabolizer", "UM",
                        "Antidepressants (tricyclics, paroxetine)",
                        "Avoid tricyclic antidepressants due to subtherapeutic levels. " +
                        "Consider sertraline, citalopram or escitalopram.",
                        "A", "warning",
                        "Rapid metabolism leads to subtherapeutic antidepressant plasma levels."));

        put("CYP2D6", "IM",
                rec("CYP2D6", "Intermediate Metabolizer", "IM",
                        "Codeine",
                        "Use with caution at standard doses. " +
                        "Monitor for reduced efficacy or unexpected side effects.",
                        "B", "warning",
                        "Moderately reduced morphine formation from codeine."),
                rec("CYP2D6", "Intermediate Metabolizer", "IM",
                        "Tamoxifen",
                        "Standard dose. Monitor endoxifen levels if available.",
                        "B", "warning",
                        "Reduced but potentially adequate endoxifen production."));

        // ════════════════════════════════════════════════════════════════════
        // TPMT  —  Thiopurines (azathioprine, mercaptopurine, thioguanine)
        // CPIC guideline: doi 10.1158/1078-0432.CCR-10-2484
        // ════════════════════════════════════════════════════════════════════
        put("TPMT", "PM",
                rec("TPMT", "Poor Metabolizer", "PM",
                        "Azathioprine / Mercaptopurine / Thioguanine",
                        "Reduce dose by 10-fold (to 10% of standard dose). " +
                        "Administer 3 times per week instead of daily. " +
                        "Monitor FBC closely. Consider alternative non-thiopurine immunosuppressant.",
                        "A", "danger",
                        "TPMT PM accumulates cytotoxic thioguanine nucleotides, causing " +
                        "severe and potentially fatal myelosuppression."));

        put("TPMT", "IM",
                rec("TPMT", "Intermediate Metabolizer", "IM",
                        "Azathioprine / Mercaptopurine / Thioguanine",
                        "Reduce starting dose by 30–70% of standard dose. " +
                        "Titrate based on tolerance and disease response. " +
                        "Monitor FBC more frequently.",
                        "A", "warning",
                        "TPMT IM have moderately elevated thioguanine nucleotide levels " +
                        "increasing myelosuppression risk."));

        // ════════════════════════════════════════════════════════════════════
        // NUDT15  —  Thiopurines (same drugs as TPMT, same CPIC guideline)
        // ════════════════════════════════════════════════════════════════════
        put("NUDT15", "PM",
                rec("NUDT15", "Poor Metabolizer", "PM",
                        "Azathioprine / Mercaptopurine / Thioguanine",
                        "Reduce dose to 10% of standard. Administer 3×/week. " +
                        "Monitor FBC weekly. Consider alternative therapy.",
                        "A", "danger",
                        "NUDT15 PM accumulates thioguanine nucleotides leading to severe myelosuppression. " +
                        "Particularly relevant in East Asian populations."));

        put("NUDT15", "IM",
                rec("NUDT15", "Intermediate Metabolizer", "IM",
                        "Azathioprine / Mercaptopurine / Thioguanine",
                        "Reduce starting dose by 30–50%. Monitor FBC closely.",
                        "A", "warning",
                        "Moderate accumulation of cytotoxic metabolites in NUDT15 IM."));

        // ════════════════════════════════════════════════════════════════════
        // DPYD  —  Fluoropyrimidines (5-FU, capecitabine, tegafur)
        // CPIC guideline: doi 10.1002/cpt.1989
        // ════════════════════════════════════════════════════════════════════
        put("DPYD", "PM",
                rec("DPYD", "Poor Metabolizer", "PM",
                        "Fluorouracil (5-FU) / Capecitabine / Tegafur",
                        "AVOID fluoropyrimidines. Use alternative chemotherapy regimen. " +
                        "If no alternative, reduce dose by ≥50% with TDM and enhanced monitoring. " +
                        "Seek specialist oncology/pharmacogenomics advice.",
                        "A", "danger",
                        "DPYD PM have severely impaired fluoropyrimidine catabolism, " +
                        "leading to life-threatening toxicity (mucositis, diarrhea, " +
                        "myelosuppression, neurotoxicity, death)."));

        put("DPYD", "IM",
                rec("DPYD", "Intermediate Metabolizer", "IM",
                        "Fluorouracil (5-FU) / Capecitabine",
                        "Reduce starting dose by 25–50%. " +
                        "Titrate based on toxicity assessment. " +
                        "Enhanced clinical monitoring recommended.",
                        "A", "warning",
                        "Reduced DPYD activity leads to elevated fluoropyrimidine plasma levels " +
                        "and increased toxicity risk."));

        // ════════════════════════════════════════════════════════════════════
        // SLCO1B1  —  Statins (simvastatin, atorvastatin, rosuvastatin)
        // CPIC guideline: doi 10.1038/clpt.2012.57
        // ════════════════════════════════════════════════════════════════════
        put("SLCO1B1", "PM",
                rec("SLCO1B1", "Poor Function", "PM",
                        "Simvastatin",
                        "Avoid simvastatin or use lowest available dose (10 mg/day). " +
                        "Switch to pravastatin, rosuvastatin, or fluvastatin " +
                        "(less affected by SLCO1B1).",
                        "A", "danger",
                        "SLCO1B1 poor function severely impairs hepatic uptake of simvastatin, " +
                        "causing markedly elevated plasma concentrations and high myopathy risk."),
                rec("SLCO1B1", "Poor Function", "PM",
                        "Atorvastatin",
                        "Use lower atorvastatin doses. Consider pravastatin or rosuvastatin.",
                        "B", "warning",
                        "Moderately elevated atorvastatin exposure in SLCO1B1 poor function."));

        put("SLCO1B1", "IM",
                rec("SLCO1B1", "Decreased Function", "IM",
                        "Simvastatin",
                        "Use simvastatin ≤40 mg/day. " +
                        "Consider alternative statin if high-intensity statin required.",
                        "A", "warning",
                        "Moderately impaired simvastatin hepatic uptake, " +
                        "approximately 2-fold increased myopathy risk."));

        // ════════════════════════════════════════════════════════════════════
        // VKORC1  —  Warfarin (combined with CYP2C9)
        // CPIC guideline: doi 10.1038/clpt.2011.185
        // ════════════════════════════════════════════════════════════════════
        put("VKORC1", "PM",  // used for "Sensitive" phenotype
                rec("VKORC1", "Sensitive (Low Dose Required)", "PM",
                        "Warfarin",
                        "VKORC1 A/A haplotype: initiate with markedly reduced warfarin dose " +
                        "(typically 3–4 mg/day or per IWPC algorithm). " +
                        "Combine with CYP2C9 genotype for precise dosing. " +
                        "Frequent INR monitoring during initiation.",
                        "A", "danger",
                        "VKORC1 -1639A reduces VKORC1 gene expression, " +
                        "greatly increasing warfarin sensitivity and bleeding risk at standard doses."));

        put("VKORC1", "IM",
                rec("VKORC1", "Intermediate Sensitivity", "IM",
                        "Warfarin",
                        "Initiate with intermediate warfarin dose per CPIC/IWPC algorithm " +
                        "incorporating VKORC1 and CYP2C9 genotypes.",
                        "A", "warning",
                        "Heterozygous VKORC1 A/B haplotype: intermediate warfarin sensitivity."));

        // ════════════════════════════════════════════════════════════════════
        // UGT1A1  —  Irinotecan, atazanavir
        // CPIC guideline: doi 10.1158/1078-0432.CCR-14-0648
        // ════════════════════════════════════════════════════════════════════
        put("UGT1A1", "PM",
                rec("UGT1A1", "Poor Metabolizer", "PM",
                        "Irinotecan",
                        "Reduce irinotecan starting dose by at least one dose level. " +
                        "At high doses (≥250 mg/m²), consider additional dose reduction. " +
                        "Monitor closely for severe neutropenia and diarrhea.",
                        "A", "danger",
                        "UGT1A1 PM (*28/*28 or *6/*6) have markedly reduced SN-38 glucuronidation, " +
                        "leading to severe toxicity (febrile neutropenia, diarrhea)."),
                rec("UGT1A1", "Poor Metabolizer", "PM",
                        "Atazanavir",
                        "Atazanavir can be used but anticipate hyperbilirubinemia (jaundice). " +
                        "This is generally benign (indirect) but may affect patient adherence.",
                        "A", "warning",
                        "UGT1A1 PM have elevated atazanavir-associated unconjugated bilirubin."));

        put("UGT1A1", "IM",
                rec("UGT1A1", "Intermediate Metabolizer", "IM",
                        "Irinotecan",
                        "Standard doses acceptable at doses <180 mg/m². " +
                        "At higher doses, monitor for neutropenia.",
                        "A", "warning",
                        "Slightly reduced SN-38 glucuronidation, moderate toxicity risk increase."));

        // ════════════════════════════════════════════════════════════════════
        // CYP3A5  —  Tacrolimus
        // CPIC guideline: doi 10.1038/tpj.2015.56
        // ════════════════════════════════════════════════════════════════════
        put("CYP3A5", "PM",  // Non-expresser
                rec("CYP3A5", "Non-Expresser", "PM",
                        "Tacrolimus",
                        "Initiate tacrolimus at standard weight-based dose " +
                        "(no dose adjustment needed for non-expressers). " +
                        "Non-expressers typically require lower doses than expressers " +
                        "to achieve target trough levels.",
                        "A", "warning",
                        "CYP3A5 non-expressers (*3/*3) metabolise tacrolimus more slowly; " +
                        "standard dosing often results in target-range levels."));

        put("CYP3A5", "NM",  // Expresser
                rec("CYP3A5", "Expresser", "NM",
                        "Tacrolimus",
                        "Consider initiating at 1.5–2× the standard weight-based dose. " +
                        "Monitor trough levels closely and adjust accordingly.",
                        "A", "warning",
                        "CYP3A5 expressers (*1/*1 or *1/*3) metabolise tacrolimus rapidly; " +
                        "standard doses often result in subtherapeutic trough levels."));

        // ════════════════════════════════════════════════════════════════════
        // G6PD  —  Rasburicase, dapsone, primaquine, nitrofurantoin
        // CPIC guideline: doi 10.1002/cpt.1908
        // ════════════════════════════════════════════════════════════════════
        put("G6PD", "PM",  // Deficient
                rec("G6PD", "Deficient", "PM",
                        "Rasburicase",
                        "Rasburicase is CONTRAINDICATED in G6PD deficiency. " +
                        "Use allopurinol or febuxostat for hyperuricemia management.",
                        "A", "danger",
                        "Rasburicase generates H2O2, causing severe acute haemolytic anaemia " +
                        "in G6PD-deficient patients."),
                rec("G6PD", "Deficient", "PM",
                        "Dapsone / Primaquine / Nitrofurantoin",
                        "Avoid oxidant drugs. Use alternative agents. " +
                        "If unavoidable, use with extreme caution and monitor Hb closely.",
                        "A", "danger",
                        "Oxidant drugs trigger acute haemolytic anaemia in G6PD deficiency."));

        put("G6PD", "IM",
                rec("G6PD", "Variable / Intermediate", "IM",
                        "Rasburicase / Oxidant drugs",
                        "Use with caution. Baseline G6PD enzyme activity testing recommended. " +
                        "Monitor for haemolysis.",
                        "B", "warning",
                        "Variable G6PD activity; haemolysis risk depends on residual enzyme activity."));

        // ════════════════════════════════════════════════════════════════════
        // IFNL3  —  Pegylated interferon-alpha for Hepatitis C
        // CPIC guideline: doi 10.1038/clpt.2013.37
        // ════════════════════════════════════════════════════════════════════
        put("IFNL3", "PM",  // Unfavorable
                rec("IFNL3", "Unfavorable Response to Interferon", "PM",
                        "Pegylated Interferon-alpha (Hepatitis C)",
                        "Patient is less likely to achieve sustained virological response (SVR) " +
                        "with interferon-based therapy. " +
                        "Prefer direct-acting antiviral (DAA) regimens which are not affected by IFNL3 genotype.",
                        "A", "warning",
                        "IFNL3 TT/TG genotype (rs12979860) is associated with " +
                        "significantly lower SVR rates to IFN-based HCV treatment."));

        put("IFNL3", "NM",  // Favorable
                rec("IFNL3", "Favorable Response to Interferon", "NM",
                        "Pegylated Interferon-alpha (Hepatitis C)",
                        "Standard interferon-based therapy appropriate. " +
                        "High likelihood of sustained virological response (SVR).",
                        "A", "success",
                        "IFNL3 CC genotype (rs12979860) is strongly associated with " +
                        "favourable response to IFN-based HCV treatment."));
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Given a list of GenotypeCall results, return all applicable drug recommendations.
     */
    public List<DrugRecommendation> getRecommendations(List<GenotypeCall> genotypeCalls) {
        List<DrugRecommendation> all = new ArrayList<>();
        for (GenotypeCall call : genotypeCalls) {
            String key = call.getGene() + "::" + call.getPhenotypeCode();
            List<DrugRecommendation> recs = REC_TABLE.get(key);
            if (recs != null) {
                all.addAll(recs);
                log.info("Found {} recommendations for {}", recs.size(), key);
            } else {
                log.info("No specific recommendations for {} {} ({})",
                        call.getGene(), call.getDiplotype(), call.getPhenotypeCode());
            }
        }
        // Sort: danger first, then warning, then success
        all.sort(Comparator.comparingInt(r -> riskScore(r.getRiskLevel())));
        return all;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    @SafeVarargs
    private static void put(String gene, String phenotypeCode, DrugRecommendation... recs) {
        String key = gene + "::" + phenotypeCode;
        REC_TABLE.computeIfAbsent(key, k -> new ArrayList<>()).addAll(Arrays.asList(recs));
    }

    private static DrugRecommendation rec(String gene, String phenotype, String phenotypeCode,
                                           String drug, String recommendation,
                                           String cpicLevel, String riskLevel,
                                           String implication) {
        return new DrugRecommendation(gene, null, phenotype, phenotypeCode,
                drug, recommendation, cpicLevel, riskLevel, implication);
    }

    private int riskScore(String level) {
        if ("danger".equals(level))  return 0;
        if ("warning".equals(level)) return 1;
        return 2;
    }
}
