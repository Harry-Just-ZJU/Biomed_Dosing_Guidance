package cn.edu.zju.dao;

import cn.edu.zju.bean.VcfVariant;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class VcfVariantDao extends BaseDao {

    private static final Logger log = LoggerFactory.getLogger(VcfVariantDao.class);
    private static final int BATCH_SIZE = 500;

    /** Batch-insert all variant records for a sample. */
    public void saveVariants(int sampleId, List<VcfVariant> variants) {
        if (variants == null || variants.isEmpty()) return;

        DBUtils.execSQL(conn -> {
            String sql = "INSERT INTO vcf_variant " +
                    "(sample_id, chrom, pos, var_id, ref, alt, qual, filter_val, info, genotype) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)";
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    int count = 0;
                    for (VcfVariant v : variants) {
                        ps.setInt(1,    sampleId);
                        ps.setString(2, v.getChrom());
                        ps.setLong(3,   v.getPos());
                        ps.setString(4, truncate(v.getVarId(), 200));
                        ps.setString(5, truncate(v.getRef(), 500));
                        ps.setString(6, truncate(v.getAlt(), 500));
                        ps.setString(7, truncate(v.getQual(), 50));
                        ps.setString(8, truncate(v.getFilterVal(), 200));
                        ps.setString(9, v.getInfo());           // MEDIUMTEXT – no truncation
                        ps.setString(10, truncate(v.getGenotype(), 200));
                        ps.addBatch();
                        if (++count % BATCH_SIZE == 0) {
                            ps.executeBatch();
                            conn.commit();
                            ps.clearBatch();
                        }
                    }
                    ps.executeBatch();
                    conn.commit();
                }
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                log.error("saveVariants failed sampleId={}", sampleId, e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        });
        log.info("Saved {} variants for sampleId={}", variants.size(), sampleId);
    }

    /** Persist the extracted gene symbols linked to a sample. */
    public void saveGenes(int sampleId, List<String> geneSymbols) {
        if (geneSymbols == null || geneSymbols.isEmpty()) return;

        DBUtils.execSQL(conn -> {
            String sql = "INSERT IGNORE INTO vcf_gene (sample_id, gene_symbol) VALUES (?,?)";
            try {
                conn.setAutoCommit(false);
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (String gene : geneSymbols) {
                        ps.setInt(1, sampleId);
                        ps.setString(2, truncate(gene, 200));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    conn.commit();
                }
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                log.error("saveGenes failed sampleId={}", sampleId, e);
            } finally {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
        });
        log.info("Saved {} gene symbols for sampleId={}", geneSymbols.size(), sampleId);
    }

    /** Retrieve the distinct gene symbols stored for a sample. */
    public List<String> findGenesBySampleId(int sampleId) {
        List<String> genes = new java.util.ArrayList<>();
        DBUtils.execSQL(conn -> {
            String sql = "SELECT DISTINCT gene_symbol FROM vcf_gene WHERE sample_id=? ORDER BY gene_symbol";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, sampleId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) genes.add(rs.getString(1));
                }
            } catch (SQLException e) {
                log.error("findGenesBySampleId failed sampleId={}", sampleId, e);
            }
        });
        return genes;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
