package cn.edu.zju.crawler;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dbutils.DBUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.Map;

public class DrugLabelCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DrugLabelCrawler.class);

    public static final String URL_DRUG_LABEL        = "https://api.pharmgkb.org/v1/site/labelsByDrug";
    public static final String URL_DRUG_LABEL_DETAIL = "https://api.pharmgkb.org/v1/site/page/drugLabels/%s?view=base";

    private final DrugDao      drugDao      = new DrugDao();
    private final DrugLabelDao drugLabelDao = new DrugLabelDao();

    /** Step 1: crawl the master drug list */
    @SuppressWarnings("unchecked")
    public void doCrawlerDrug() {
        String content = getURLContent(URL_DRUG_LABEL);
        if (content == null) { log.error("Could not fetch drug list."); return; }

        Gson gson = new Gson();
        Map<String, Object> root = gson.fromJson(content, Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("data");

        for (Map<String, Object> entry : data) {
            Map<String, Object> drugMap = (Map<String, Object>) entry.get("drug");
            String id      = (String)  drugMap.get("id");
            String name    = (String)  drugMap.get("name");
            String objCls  = (String)  drugMap.get("objCls");
            String drugUrl = (String)  entry.get("drugUrl");
            boolean biomarker = Boolean.TRUE.equals(entry.get("biomarker"));
            Drug drug = new Drug(id, name, biomarker, drugUrl, objCls);
            drugDao.saveDrug(drug);
        }
        log.info("Drug crawl complete.");
    }

    /** Step 2: for each drug, crawl its labels */
    @SuppressWarnings("unchecked")
    public void doCrawlerDrugLabel() {
        DBUtils.execSQL(conn -> {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM drug");
                 ResultSet rs = ps.executeQuery()) {
                Gson gson = new Gson();
                while (rs.next()) {
                    String drugId = rs.getString("id");
                    String content = getURLContent(String.format(URL_DRUG_LABEL_DETAIL, drugId));
                    if (content == null) { log.warn("No content for drug {}", drugId); continue; }

                    Map<String, Object> result = gson.fromJson(content, Map.class);
                    Map<String, Object> data   = (Map<String, Object>) result.get("data");
                    List<Map<String, Object>> labels = (List<Map<String, Object>>) data.get("drugLabels");
                    if (labels == null) continue;

                    for (Map<String, Object> x : labels) {
                        String labelId  = (String) x.get("id");
                        String name     = labelId;   // use id as name fallback (same as original)
                        String objCls   = (String) x.get("objCls");
                        boolean altDrug = Boolean.TRUE.equals(x.get("alternateDrugAvailable"));
                        boolean dosingInfo = Boolean.TRUE.equals(x.get("dosingInformation"));

                        String prescribing = "";
                        if (x.containsKey("prescribingMarkdown")) {
                            prescribing = (String)((Map<?,?>)x.get("prescribingMarkdown")).get("html");
                        }
                        String source   = (String) x.get("source");
                        String textMd   = safeHtml(x, "textMarkdown");
                        String summaryMd = safeHtml(x, "summaryMarkdown");
                        String raw      = gson.toJson(x);

                        List<Map<String,Object>> chemicals =
                                (List<Map<String,Object>>) x.get("relatedChemicals");
                        String relDrugId = chemicals != null && !chemicals.isEmpty()
                                ? (String) chemicals.get(0).get("id") : drugId;

                        DrugLabel dl = new DrugLabel(labelId, name, objCls, altDrug, dosingInfo,
                                prescribing, source, textMd, summaryMd, raw, relDrugId);
                        drugLabelDao.saveDrugLabel(dl);
                    }
                    log.info("Crawled labels for drug {}", drugId);
                }
            } catch (SQLException e) {
                log.error("DrugLabel crawl failed", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private String safeHtml(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map) return (String)((Map<?,?>)v).get("html");
        return "";
    }
}
