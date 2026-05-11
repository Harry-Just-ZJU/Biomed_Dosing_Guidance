package cn.edu.zju.dao;

import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class DrugLabelDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(DrugLabelDao.class);

    public boolean existsById(String id) { return super.existsById(id, "drug_label"); }

    public void saveDrugLabel(DrugLabel dl) {
        if (existsById(dl.getId())) return;
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO drug_label (id,name,obj_cls,alternate_drug_available," +
                    "dosing_information,prescribing_markdown,source,text_markdown," +
                    "summary_markdown,raw,drug_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
                ps.setString(1, dl.getId()); ps.setString(2, dl.getName());
                ps.setString(3, dl.getObjCls()); ps.setBoolean(4, dl.isAlternateDrugAvailable());
                ps.setBoolean(5, dl.isDosingInformation()); ps.setString(6, dl.getPrescribingMarkdown());
                ps.setString(7, dl.getSource()); ps.setString(8, dl.getTextMarkdown());
                ps.setString(9, dl.getSummaryMarkdown()); ps.setString(10, dl.getRaw());
                ps.setString(11, dl.getDrugId()); ps.execute();
            } catch (SQLException e) { log.error("saveDrugLabel id={}", dl.getId(), e); }
        });
    }

    public List<DrugLabel> findAll() {
        List<DrugLabel> list = new ArrayList<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,name,obj_cls,alternate_drug_available,dosing_information," +
                    "prescribing_markdown,source,text_markdown,summary_markdown,raw,drug_id " +
                    "FROM drug_label ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new DrugLabel(
                        rs.getString("id"), rs.getString("name"), rs.getString("obj_cls"),
                        rs.getBoolean("alternate_drug_available"), rs.getBoolean("dosing_information"),
                        rs.getString("prescribing_markdown"), rs.getString("source"),
                        rs.getString("text_markdown"), rs.getString("summary_markdown"),
                        rs.getString("raw"), rs.getString("drug_id")));
            } catch (SQLException e) { log.error("findAll drugLabels", e); }
        });
        return list;
    }
    public DrugLabel findById(String id) {
        if (id == null) return null;
        java.util.concurrent.atomic.AtomicReference<DrugLabel> ref = new java.util.concurrent.atomic.AtomicReference<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,obj_cls,alternate_drug_available,dosing_information,prescribing_markdown,source,text_markdown,summary_markdown,raw,drug_id FROM drug_label WHERE id=?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ref.set(new DrugLabel(rs.getString("id"),rs.getString("name"),rs.getString("obj_cls"),rs.getBoolean("alternate_drug_available"),rs.getBoolean("dosing_information"),rs.getString("prescribing_markdown"),rs.getString("source"),rs.getString("text_markdown"),rs.getString("summary_markdown"),rs.getString("raw"),rs.getString("drug_id")));
                }
            } catch (SQLException e) { log.error("findById label", e); }
        });
        return ref.get();
    }
    public java.util.List<DrugLabel> findByDrugId(String drugId) {
        java.util.List<DrugLabel> list = new java.util.ArrayList<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,obj_cls,alternate_drug_available,dosing_information,prescribing_markdown,source,text_markdown,summary_markdown,raw,drug_id FROM drug_label WHERE drug_id=? ORDER BY name")) {
                ps.setString(1, drugId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(new DrugLabel(rs.getString("id"),rs.getString("name"),rs.getString("obj_cls"),rs.getBoolean("alternate_drug_available"),rs.getBoolean("dosing_information"),rs.getString("prescribing_markdown"),rs.getString("source"),rs.getString("text_markdown"),rs.getString("summary_markdown"),rs.getString("raw"),rs.getString("drug_id")));
                }
            } catch (SQLException e) { log.error("findByDrugId label", e); }
        });
        return list;
    }
}
