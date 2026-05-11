package cn.edu.zju.dao;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class DosingGuidelineDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineDao.class);

    public boolean existsById(String id) { return super.existsById(id, "dosing_guideline"); }

    public void saveDosingGuideline(DosingGuideline dg) {
        if (existsById(dg.getId())) return;
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO dosing_guideline (id,obj_cls,name,recommendation," +
                    "drug_id,source,summary_markdown,text_markdown,raw) VALUES (?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, dg.getId()); ps.setString(2, dg.getObjCls());
                ps.setString(3, dg.getName()); ps.setBoolean(4, dg.isRecommendation());
                ps.setString(5, dg.getDrugId()); ps.setString(6, dg.getSource());
                ps.setString(7, dg.getSummaryMarkdown()); ps.setString(8, dg.getTextMarkdown());
                ps.setString(9, dg.getRaw()); ps.execute();
            } catch (SQLException e) { log.error("saveDosingGuideline id={}", dg.getId(), e); }
        });
    }

    public List<DosingGuideline> findAll() {
        List<DosingGuideline> list = new ArrayList<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,obj_cls,name,recommendation,drug_id,source," +
                    "summary_markdown,text_markdown,raw FROM dosing_guideline ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new DosingGuideline(
                        rs.getString("id"), rs.getString("obj_cls"), rs.getString("name"),
                        rs.getBoolean("recommendation"), rs.getString("drug_id"),
                        rs.getString("source"), rs.getString("summary_markdown"),
                        rs.getString("text_markdown"), rs.getString("raw")));
            } catch (SQLException e) { log.error("findAll dosingGuidelines", e); }
        });
        return list;
    }
    public DosingGuideline findById(String id) {
        if (id == null) return null;
        java.util.concurrent.atomic.AtomicReference<DosingGuideline> ref = new java.util.concurrent.atomic.AtomicReference<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,obj_cls,name,recommendation,drug_id,source,summary_markdown,text_markdown,raw FROM dosing_guideline WHERE id=?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ref.set(new DosingGuideline(rs.getString("id"),rs.getString("obj_cls"),rs.getString("name"),rs.getBoolean("recommendation"),rs.getString("drug_id"),rs.getString("source"),rs.getString("summary_markdown"),rs.getString("text_markdown"),rs.getString("raw")));
                }
            } catch (SQLException e) { log.error("findById guideline", e); }
        });
        return ref.get();
    }
    public java.util.List<DosingGuideline> findByDrugId(String drugId) {
        java.util.List<DosingGuideline> list = new java.util.ArrayList<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,obj_cls,name,recommendation,drug_id,source,summary_markdown,text_markdown,raw FROM dosing_guideline WHERE drug_id=? ORDER BY name")) {
                ps.setString(1, drugId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(new DosingGuideline(rs.getString("id"),rs.getString("obj_cls"),rs.getString("name"),rs.getBoolean("recommendation"),rs.getString("drug_id"),rs.getString("source"),rs.getString("summary_markdown"),rs.getString("text_markdown"),rs.getString("raw")));
                }
            } catch (SQLException e) { log.error("findByDrugId guideline", e); }
        });
        return list;
    }
}
