package cn.edu.zju.dao;

import cn.edu.zju.bean.Sample;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SampleDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(SampleDao.class);

    public int save(String uploadedBy, String vcfFilename) {
        AtomicInteger key = new AtomicInteger(-1);
        try {
            DBUtils.execSQL(conn -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO sample (created_at, uploaded_by, vcf_filename) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                    ps.setString(2, uploadedBy);
                    ps.setString(3, vcfFilename);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) key.set(rs.getInt(1));
                    }
                } catch (SQLException e) {
                    if (e.getMessage() != null && e.getMessage().contains("vcf_filename")) {
                        log.warn("vcf_filename column missing, using fallback. Run migrate.sql.");
                        try (PreparedStatement ps2 = conn.prepareStatement(
                                "INSERT INTO sample (created_at, uploaded_by) VALUES (?,?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            ps2.setTimestamp(1, new Timestamp(new Date().getTime()));
                            ps2.setString(2, uploadedBy);
                            ps2.executeUpdate();
                            try (ResultSet rs = ps2.getGeneratedKeys()) {
                                if (rs.next()) key.set(rs.getInt(1));
                            }
                        } catch (SQLException ex) {
                            log.error("save sample fallback failed: {}", ex.getMessage(), ex);
                        }
                    } else {
                        log.error("save sample failed: {}", e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            log.error("SampleDao.save error: {}", e.getMessage(), e);
        }
        return key.get();
    }

    public List<Sample> findAll() {
        List<Sample> list = new ArrayList<>();
        try {
            DBUtils.execSQL(conn -> {
                boolean hasFn = columnExists(conn, "vcf_filename");
                String sql = hasFn
                        ? "SELECT id,created_at,uploaded_by,vcf_filename FROM sample ORDER BY id DESC"
                        : "SELECT id,created_at,uploaded_by FROM sample ORDER BY id DESC";
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs, hasFn));
                } catch (SQLException e) { log.error("findAll samples", e); }
            });
        } catch (Exception e) { log.error("SampleDao.findAll", e); }
        return list;
    }

    public Sample findById(int id) {
        AtomicReference<Sample> ref = new AtomicReference<>();
        try {
            DBUtils.execSQL(conn -> {
                boolean hasFn = columnExists(conn, "vcf_filename");
                String sql = hasFn
                        ? "SELECT id,created_at,uploaded_by,vcf_filename FROM sample WHERE id=?"
                        : "SELECT id,created_at,uploaded_by FROM sample WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) ref.set(map(rs, hasFn));
                    }
                } catch (SQLException e) { log.error("findById sample id={}", id, e); }
            });
        } catch (Exception e) { log.error("SampleDao.findById", e); }
        return ref.get();
    }

    private boolean columnExists(Connection conn, String column) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='sample' AND COLUMN_NAME=?")) {
            ps.setString(1, column);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        } catch (SQLException e) { return false; }
    }

    private Sample map(ResultSet rs, boolean hasFn) throws SQLException {
        return new Sample(rs.getInt("id"),
                new Date(rs.getTimestamp("created_at").getTime()),
                rs.getString("uploaded_by"),
                hasFn ? rs.getString("vcf_filename") : null);
    }
}
