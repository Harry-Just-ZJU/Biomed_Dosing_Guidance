USE biomed;
-- Run this if you already have the old schema and need to upgrade
ALTER TABLE sample ADD COLUMN vcf_filename TEXT NULL;

CREATE TABLE IF NOT EXISTS vcf_variant (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    sample_id  INT          NOT NULL,
    chrom      VARCHAR(50)  NOT NULL,
    pos        BIGINT       NOT NULL,
    var_id     VARCHAR(200) NULL,
    ref        VARCHAR(500) NOT NULL,
    alt        VARCHAR(500) NOT NULL,
    qual       VARCHAR(50)  NULL,
    filter_val VARCHAR(200) NULL,
    info       MEDIUMTEXT   NULL,
    genotype   VARCHAR(200) NULL,
    INDEX idx_sample_id (sample_id)
);

CREATE TABLE IF NOT EXISTS vcf_gene (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sample_id   INT          NOT NULL,
    gene_symbol VARCHAR(200) NOT NULL,
    INDEX idx_sample_gene (sample_id, gene_symbol)
);

SELECT 'Migration complete.' AS status;
