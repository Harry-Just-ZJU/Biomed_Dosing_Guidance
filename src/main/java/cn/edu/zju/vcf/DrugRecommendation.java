package cn.edu.zju.vcf;

/**
 * A concrete drug dosing recommendation produced by the CPIC pipeline.
 */
public class DrugRecommendation {

    private final String gene;
    private final String diplotype;
    private final String phenotype;
    private final String phenotypeCode;
    private final String drugName;
    private final String recommendation;   // Human-readable recommendation text
    private final String cpicLevel;        // A / B / C / D
    private final String riskLevel;        // danger / warning / success  (for UI)
    private final String implication;      // What the phenotype means for this drug

    public DrugRecommendation(String gene, String diplotype,
                              String phenotype, String phenotypeCode,
                              String drugName, String recommendation,
                              String cpicLevel, String riskLevel,
                              String implication) {
        this.gene           = gene;
        this.diplotype      = diplotype;
        this.phenotype      = phenotype;
        this.phenotypeCode  = phenotypeCode;
        this.drugName       = drugName;
        this.recommendation = recommendation;
        this.cpicLevel      = cpicLevel;
        this.riskLevel      = riskLevel;
        this.implication    = implication;
    }

    public String getGene()           { return gene; }
    public String getDiplotype()      { return diplotype; }
    public String getPhenotype()      { return phenotype; }
    public String getPhenotypeCode()  { return phenotypeCode; }
    public String getDrugName()       { return drugName; }
    public String getRecommendation() { return recommendation; }
    public String getCpicLevel()      { return cpicLevel; }
    public String getRiskLevel()      { return riskLevel; }
    public String getImplication()    { return implication; }
}
