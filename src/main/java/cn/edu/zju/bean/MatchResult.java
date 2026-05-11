package cn.edu.zju.bean;

import java.util.List;

/**
 * Wraps a DrugLabel together with:
 *  - the set of gene symbols that caused the match
 *  - the dosing guidelines that belong to the same drug
 */
public class MatchResult {

    private DrugLabel           drugLabel;
    private List<String>        matchedGenes;
    private List<DosingGuideline> guidelines;
    private String              riskLevel;   // "danger" | "warning" | "success"

    public MatchResult() {}

    public MatchResult(DrugLabel drugLabel,
                       List<String> matchedGenes,
                       List<DosingGuideline> guidelines,
                       String riskLevel) {
        this.drugLabel    = drugLabel;
        this.matchedGenes = matchedGenes;
        this.guidelines   = guidelines;
        this.riskLevel    = riskLevel;
    }

    public DrugLabel             getDrugLabel()              { return drugLabel; }
    public void                  setDrugLabel(DrugLabel d)   { this.drugLabel = d; }
    public List<String>          getMatchedGenes()           { return matchedGenes; }
    public void                  setMatchedGenes(List<String> g) { this.matchedGenes = g; }
    public List<DosingGuideline> getGuidelines()             { return guidelines; }
    public void                  setGuidelines(List<DosingGuideline> g) { this.guidelines = g; }
    public String                getRiskLevel()              { return riskLevel; }
    public void                  setRiskLevel(String r)      { this.riskLevel = r; }
}
