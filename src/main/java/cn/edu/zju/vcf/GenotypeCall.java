package cn.edu.zju.vcf;

import java.util.List;

/**
 * The result of haplotype calling for a single gene.
 *
 * Example for CYP2C19:
 *   gene        = "CYP2C19"
 *   allele1     = "*1"
 *   allele2     = "*2"
 *   diplotype   = "*1/*2"
 *   phenotype   = "Intermediate Metabolizer"
 *   phenotypeCode = "IM"
 *   activityScore = 1.0
 *   matchedRsids = ["rs4244285"]
 */
public class GenotypeCall {

    private final String       gene;
    private final String       allele1;
    private final String       allele2;
    private final String       diplotype;
    private final String       phenotype;
    private final String       phenotypeCode;
    private final double       activityScore;
    private final List<String> matchedRsids;

    public GenotypeCall(String gene,
                        String allele1, String allele2,
                        String diplotype,
                        String phenotype, String phenotypeCode,
                        double activityScore,
                        List<String> matchedRsids) {
        this.gene          = gene;
        this.allele1       = allele1;
        this.allele2       = allele2;
        this.diplotype     = diplotype;
        this.phenotype     = phenotype;
        this.phenotypeCode = phenotypeCode;
        this.activityScore = activityScore;
        this.matchedRsids  = matchedRsids;
    }

    public String       getGene()          { return gene; }
    public String       getAllele1()        { return allele1; }
    public String       getAllele2()        { return allele2; }
    public String       getDiplotype()     { return diplotype; }
    public String       getPhenotype()     { return phenotype; }
    public String       getPhenotypeCode() { return phenotypeCode; }
    public double       getActivityScore() { return activityScore; }
    public List<String> getMatchedRsids()  { return matchedRsids; }

    /** Badge colour for UI: PM/UM = danger, IM/RM = warning, NM = success */
    public String getRiskLevel() {
        if ("PM".equals(phenotypeCode) || "UM".equals(phenotypeCode)) return "danger";
        if ("IM".equals(phenotypeCode) || "RM".equals(phenotypeCode)) return "warning";
        return "success";
    }

    @Override
    public String toString() {
        return gene + " " + diplotype + " -> " + phenotype;
    }
}
