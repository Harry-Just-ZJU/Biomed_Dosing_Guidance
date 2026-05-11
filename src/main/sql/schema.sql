-- ============================================================
-- Precision Medicine Matching System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS biomed DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biomed;

-- Drug master table
CREATE TABLE IF NOT EXISTS drug (
    id         VARCHAR(100) NOT NULL,
    name       VARCHAR(500) NULL,
    obj_cls    VARCHAR(100) NULL,
    drug_url   VARCHAR(200) NULL,
    biomarker  TINYINT(1)   NULL,
    CONSTRAINT drug_id_uindex UNIQUE (id),
    PRIMARY KEY (id)
);

-- Drug label table (FDA/EMA/PMDA etc.)
CREATE TABLE IF NOT EXISTS drug_label (
    id                      VARCHAR(100) NOT NULL,
    name                    VARCHAR(200) NULL,
    obj_cls                 VARCHAR(100) NULL,
    alternate_drug_available TINYINT(1)  NULL,
    dosing_information      TINYINT(1)   NULL,
    prescribing_markdown    TEXT         NULL,
    source                  VARCHAR(100) NULL,
    text_markdown           MEDIUMTEXT   NULL,
    summary_markdown        TEXT         NULL,
    raw                     MEDIUMTEXT   NULL,
    drug_id                 VARCHAR(100) NULL,
    CONSTRAINT drug_label_id_uindex UNIQUE (id),
    PRIMARY KEY (id)
);

-- Dosing guideline table (CPIC/DPWG/FDA etc.)
CREATE TABLE IF NOT EXISTS dosing_guideline (
    id               VARCHAR(100)  NOT NULL,
    obj_cls          VARCHAR(100)  NULL,
    name             VARCHAR(500)  NULL,
    recommendation   TINYINT(1)    NULL,
    drug_id          VARCHAR(100)  NULL,
    source           VARCHAR(100)  NULL,
    summary_markdown TEXT          NULL,
    text_markdown    MEDIUMTEXT    NULL,
    raw              LONGTEXT      NULL,
    CONSTRAINT dosing_guideline_id_uindex UNIQUE (id),
    PRIMARY KEY (id)
);

-- Patient sample records
CREATE TABLE IF NOT EXISTS sample (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    created_at  DATETIME NULL,
    uploaded_by TEXT     NULL,
    vcf_filename TEXT    NULL
);

-- VCF variant records (parsed from uploaded VCF files)
CREATE TABLE IF NOT EXISTS vcf_variant (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sample_id   INT          NOT NULL,
    chrom       VARCHAR(50)  NOT NULL,
    pos         BIGINT       NOT NULL,
    var_id      VARCHAR(200) NULL,
    ref         VARCHAR(500) NOT NULL,
    alt         VARCHAR(500) NOT NULL,
    qual        VARCHAR(50)  NULL,
    filter_val  VARCHAR(200) NULL,
    info        MEDIUMTEXT   NULL,
    genotype    VARCHAR(200) NULL,
    INDEX idx_sample_id (sample_id),
    INDEX idx_chrom_pos (chrom, pos)
);

-- Gene annotations extracted from VCF INFO field or matched from dbSNP
CREATE TABLE IF NOT EXISTS vcf_gene (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    sample_id   INT          NOT NULL,
    gene_symbol VARCHAR(200) NOT NULL,
    chrom       VARCHAR(50)  NULL,
    pos         BIGINT       NULL,
    ref         VARCHAR(500) NULL,
    alt         VARCHAR(500) NULL,
    rsid        VARCHAR(200) NULL,
    INDEX idx_sample_gene (sample_id, gene_symbol)
);
