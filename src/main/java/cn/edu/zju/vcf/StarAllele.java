package cn.edu.zju.vcf;

import java.util.Map;

/**
 * Represents a single Star Allele (e.g. CYP2C19*2) definition.
 * A star allele is defined by one or more rsID → required genotype mappings.
 *
 * Example:
 *   gene    = "CYP2C19"
 *   allele  = "*2"
 *   rsids   = { "rs4244285": "A" }   // ALT allele that defines this star allele
 *   function = "No function"
 */
public class StarAllele {

    private final String gene;
    private final String allele;          // e.g. "*2", "*17"
    private final Map<String, String> definingVariants; // rsID → ALT base
    private final String activityValue;   // "Normal", "Decreased", "No function", "Increased"

    public StarAllele(String gene, String allele,
                      Map<String, String> definingVariants,
                      String activityValue) {
        this.gene             = gene;
        this.allele           = allele;
        this.definingVariants = definingVariants;
        this.activityValue    = activityValue;
    }

    public String getGene()             { return gene; }
    public String getAllele()           { return allele; }
    public Map<String, String> getDefiningVariants() { return definingVariants; }
    public String getActivityValue()   { return activityValue; }

    @Override
    public String toString() {
        return gene + allele + "(" + activityValue + ")";
    }
}
