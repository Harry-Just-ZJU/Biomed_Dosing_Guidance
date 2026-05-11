package cn.edu.zju.crawler;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.dao.DosingGuidelineDao;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class DosingGuidelineCrawler extends BaseCrawler {

    private static final Logger log = LoggerFactory.getLogger(DosingGuidelineCrawler.class);

    public static final String URL_BASE       = "https://api.pharmgkb.org/v1/data%s";
    public static final String URL_GUIDELINES = "https://api.pharmgkb.org/v1/site/guidelinesByDrugs";

    private final DosingGuidelineDao dao = new DosingGuidelineDao();

    @SuppressWarnings("unchecked")
    public void doCrawlerDosingGuidelineList() {
        String content = getURLContent(URL_GUIDELINES);
        if (content == null) { log.error("Could not fetch guideline list."); return; }

        Gson gson = new Gson();
        Map<String, Object> root = gson.fromJson(content, Map.class);
        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("data");

        for (Map<String, Object> entry : data) {
            for (String source : List.of("cpic", "cpnds", "dpwg", "fda", "pro")) {
                List<Map<String, Object>> guidelines =
                        (List<Map<String, Object>>) entry.get(source);
                if (guidelines == null) continue;
                for (Map<String, Object> g : guidelines) {
                    String url = (String) g.get("url");
                    if (url != null) doCrawlerDosingGuideline(url);
                }
            }
        }
        log.info("Dosing guideline crawl complete.");
    }

    @SuppressWarnings("unchecked")
    public void doCrawlerDosingGuideline(String url) {
        String content = getURLContent(String.format(URL_BASE, url));
        if (content == null) { log.warn("No content for guideline url: {}", url); return; }

        Gson gson = new Gson();
        Map<String, Object> root = gson.fromJson(content, Map.class);
        Map<String, Object> data = (Map<String, Object>) root.get("data");

        String id             = (String)  data.get("id");
        String objCls         = (String)  data.get("objCls");
        String name           = (String)  data.get("name");
        boolean recommendation = Boolean.TRUE.equals(data.get("recommendation"));

        List<Map<String, Object>> chemicals =
                (List<Map<String, Object>>) data.get("relatedChemicals");
        String drugId = chemicals != null && !chemicals.isEmpty()
                ? (String) chemicals.get(0).get("id") : null;

        String source      = (String) data.get("source");
        String summaryMd   = safeHtml(data, "summaryMarkdown");
        String textMd      = safeHtml(data, "textMarkdown");
        String raw         = gson.toJson(root);

        DosingGuideline dg = new DosingGuideline(id, objCls, name, recommendation,
                drugId, source, summaryMd, textMd, raw);
        dao.saveDosingGuideline(dg);
        log.info("Saved dosing guideline: {}", id);
    }

    @SuppressWarnings("unchecked")
    private String safeHtml(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof Map) return (String)((Map<?,?>)v).get("html");
        return "";
    }
}
