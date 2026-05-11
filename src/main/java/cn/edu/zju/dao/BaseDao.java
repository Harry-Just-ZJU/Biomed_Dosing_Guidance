package cn.edu.zju.dao;

import cn.edu.zju.dbutils.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseDao {

    private static final Logger log = LoggerFactory.getLogger(BaseDao.class);

    public boolean existsById(String id, String tableName) {
        AtomicBoolean exists = new AtomicBoolean(false);
        DBUtils.execSQL(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM " + tableName + " WHERE id = ?")) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) exists.set(true);
                }
            } catch (SQLException e) {
                log.error("existsById failed for table={}, id={}", tableName, id, e);
            }
        });
        return exists.get();
    }
}
