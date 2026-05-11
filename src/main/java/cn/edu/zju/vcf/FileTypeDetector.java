package cn.edu.zju.vcf;

/**
 * Detects whether an uploaded file is a standard VCF or an ANNOVAR table output.
 *
 * ANNOVAR table_annovar output has a header starting with:
 *   Chr  Start  End  Ref  Alt  Func.refGene  Gene.refGene ...
 *
 * Standard VCF files start with "##fileformat=VCF" meta lines,
 * followed by a "#CHROM" column header line.
 *
 * ANNOVAR-annotated VCF files (convert2annovar + annotate_variation output
 * piped back to VCF) are treated as VCF — they contain ##ANNOVAR in header.
 */
public class FileTypeDetector {

    public enum FileType {
        VCF,            // Standard VCF 4.x
        ANNOVAR_TABLE   // ANNOVAR table_annovar .txt output
    }

    /**
     * Detect file type from the first few lines of content.
     */
    public static FileType detect(String content) {
        if (content == null || content.isBlank()) return FileType.VCF;

        String[] lines = content.split("\\r?\\n", 20);
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            // VCF meta line
            if (line.startsWith("##")) return FileType.VCF;

            // VCF column header
            if (line.startsWith("#CHROM")) return FileType.VCF;

            // ANNOVAR table header — first non-blank non-comment line
            // starts with "Chr" and contains "Gene.refGene" or "Func.refGene"
            if (line.startsWith("Chr") &&
                    (line.contains("Gene.refGene") || line.contains("Func.refGene"))) {
                return FileType.ANNOVAR_TABLE;
            }

            // Any other first real line — treat as VCF
            break;
        }
        return FileType.VCF;
    }
}
