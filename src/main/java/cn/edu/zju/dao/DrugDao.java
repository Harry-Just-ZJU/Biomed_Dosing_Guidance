package cn.edu.zju.dao;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class DrugDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(DrugDao.class);

    public boolean existsById(String id) { return super.existsById(id, "drug"); }

    public void saveDrug(Drug drug) {
        if (existsById(drug.getId())) return;
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO drug (id,name,obj_cls,biomarker,drug_url) VALUES (?,?,?,?,?)")) {
                ps.setString(1, drug.getId()); ps.setString(2, drug.getName());
                ps.setString(3, drug.getObjCls()); ps.setBoolean(4, drug.isBiomarker());
                ps.setString(5, drug.getDrugUrl()); ps.execute();
            } catch (SQLException e) { log.error("saveDrug id={}", drug.getId(), e); }
        });
    }

    public List<Drug> findAll() {
        List<Drug> list = new ArrayList<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id,name,obj_cls,drug_url,biomarker FROM drug ORDER BY name");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Drug(rs.getString("id"), rs.getString("name"),
                        rs.getBoolean("biomarker"), rs.getString("drug_url"), rs.getString("obj_cls")));
            } catch (SQLException e) { log.error("findAll drugs", e); }
        });
        return list;
    }
    public Drug findById(String id) {
        if (id == null) return null;
        java.util.concurrent.atomic.AtomicReference<Drug> ref = new java.util.concurrent.atomic.AtomicReference<>();
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id,name,obj_cls,drug_url,biomarker FROM drug WHERE id=?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) ref.set(new Drug(rs.getString("id"),rs.getString("name"),rs.getBoolean("biomarker"),rs.getString("drug_url"),rs.getString("obj_cls")));
                }
            } catch (SQLException e) { log.error("findById drug", e); }
        });
        return ref.get();
    }
}
