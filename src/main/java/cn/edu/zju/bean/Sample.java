package cn.edu.zju.bean;

import java.util.Date;

public class Sample {
    private int    id;
    private Date   createdAt;
    private String uploadedBy;
    private String vcfFilename;

    public Sample() {}

    public Sample(int id, Date createdAt, String uploadedBy, String vcfFilename) {
        this.id          = id;
        this.createdAt   = createdAt;
        this.uploadedBy  = uploadedBy;
        this.vcfFilename = vcfFilename;
    }

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }
    public Date   getCreatedAt()       { return createdAt; }
    public void   setCreatedAt(Date d) { this.createdAt = d; }
    public String getUploadedBy()      { return uploadedBy; }
    public void   setUploadedBy(String s) { this.uploadedBy = s; }
    public String getVcfFilename()     { return vcfFilename; }
    public void   setVcfFilename(String s) { this.vcfFilename = s; }
}
