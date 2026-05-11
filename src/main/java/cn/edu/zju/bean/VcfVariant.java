package cn.edu.zju.bean;

/**
 * Represents a single variant record parsed from a VCF file.
 * Maps to one data row: CHROM POS ID REF ALT QUAL FILTER INFO [FORMAT SAMPLE...]
 */
public class VcfVariant {

    private int    sampleId;
    private String chrom;
    private long   pos;
    private String varId;   // rsID or "."
    private String ref;
    private String alt;
    private String qual;
    private String filterVal;
    private String info;
    private String genotype; // GT field from sample column

    public VcfVariant() {}

    public VcfVariant(int sampleId, String chrom, long pos,
                      String varId, String ref, String alt,
                      String qual, String filterVal,
                      String info, String genotype) {
        this.sampleId  = sampleId;
        this.chrom     = chrom;
        this.pos       = pos;
        this.varId     = varId;
        this.ref       = ref;
        this.alt       = alt;
        this.qual      = qual;
        this.filterVal = filterVal;
        this.info      = info;
        this.genotype  = genotype;
    }

    public int    getSampleId()               { return sampleId; }
    public void   setSampleId(int sampleId)   { this.sampleId = sampleId; }
    public String getChrom()                  { return chrom; }
    public void   setChrom(String chrom)      { this.chrom = chrom; }
    public long   getPos()                    { return pos; }
    public void   setPos(long pos)            { this.pos = pos; }
    public String getVarId()                  { return varId; }
    public void   setVarId(String varId)      { this.varId = varId; }
    public String getRef()                    { return ref; }
    public void   setRef(String ref)          { this.ref = ref; }
    public String getAlt()                    { return alt; }
    public void   setAlt(String alt)          { this.alt = alt; }
    public String getQual()                   { return qual; }
    public void   setQual(String qual)        { this.qual = qual; }
    public String getFilterVal()              { return filterVal; }
    public void   setFilterVal(String f)      { this.filterVal = f; }
    public String getInfo()                   { return info; }
    public void   setInfo(String info)        { this.info = info; }
    public String getGenotype()               { return genotype; }
    public void   setGenotype(String g)       { this.genotype = g; }
}
