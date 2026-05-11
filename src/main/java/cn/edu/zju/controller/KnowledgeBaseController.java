package cn.edu.zju.controller;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.servlet.DispatchServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class KnowledgeBaseController {

    private final DrugDao            drugDao            = new DrugDao();
    private final DrugLabelDao       drugLabelDao       = new DrugLabelDao();
    private final DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/drugs",           this::drugs);
        dispatcher.registerGetMapping("/drugLabels",      this::drugLabels);
        dispatcher.registerGetMapping("/dosingGuideline", this::dosingGuideline);
        // Detail pages
        dispatcher.registerGetMapping("/drugDetail",          this::drugDetail);
        dispatcher.registerGetMapping("/drugLabelDetail",     this::drugLabelDetail);
        dispatcher.registerGetMapping("/dosingGuidelineDetail", this::dosingGuidelineDetail);
    }

    public void drugs(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String q = req.getParameter("q");
        List<Drug> all = drugDao.findAll();
        if (q != null && !q.isBlank()) {
            String lq = q.toLowerCase();
            all = all.stream()
                .filter(d -> d.getName() != null && d.getName().toLowerCase().contains(lq)
                          || d.getId()   != null && d.getId().toLowerCase().contains(lq))
                .collect(Collectors.toList());
        }
        req.setAttribute("drugs", all);
        req.setAttribute("q", q);
        req.getRequestDispatcher("/views/drugs.jsp").forward(req, resp);
    }

    public void drugLabels(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String q = req.getParameter("q");
        List<DrugLabel> all = drugLabelDao.findAll();
        if (q != null && !q.isBlank()) {
            String lq = q.toLowerCase();
            all = all.stream()
                .filter(d -> (d.getName() != null && d.getName().toLowerCase().contains(lq))
                          || (d.getSource() != null && d.getSource().toLowerCase().contains(lq))
                          || (d.getSummaryMarkdown() != null && d.getSummaryMarkdown().toLowerCase().contains(lq)))
                .collect(Collectors.toList());
        }
        req.setAttribute("drugLabels", all);
        req.setAttribute("q", q);
        req.getRequestDispatcher("/views/drug_labels.jsp").forward(req, resp);
    }

    public void dosingGuideline(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String q = req.getParameter("q");
        List<DosingGuideline> all = dosingGuidelineDao.findAll();
        if (q != null && !q.isBlank()) {
            String lq = q.toLowerCase();
            all = all.stream()
                .filter(d -> (d.getName() != null && d.getName().toLowerCase().contains(lq))
                          || (d.getSource() != null && d.getSource().toLowerCase().contains(lq))
                          || (d.getSummaryMarkdown() != null && d.getSummaryMarkdown().toLowerCase().contains(lq)))
                .collect(Collectors.toList());
        }
        req.setAttribute("dosingGuidelines", all);
        req.setAttribute("q", q);
        req.getRequestDispatcher("/views/dosing_guideline.jsp").forward(req, resp);
    }

    public void drugDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String id = req.getParameter("id");
        Drug drug = id != null ? drugDao.findById(id) : null;
        if (drug == null) { resp.sendRedirect("drugs"); return; }
        List<DrugLabel>       labels     = drugLabelDao.findByDrugId(id);
        List<DosingGuideline> guidelines = dosingGuidelineDao.findByDrugId(id);
        req.setAttribute("drug", drug);
        req.setAttribute("labels", labels);
        req.setAttribute("guidelines", guidelines);
        req.getRequestDispatcher("/views/drug_detail.jsp").forward(req, resp);
    }

    public void drugLabelDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String id = req.getParameter("id");
        DrugLabel label = id != null ? drugLabelDao.findById(id) : null;
        if (label == null) { resp.sendRedirect("drugLabels"); return; }
        req.setAttribute("label", label);
        req.getRequestDispatcher("/views/drug_label_detail.jsp").forward(req, resp);
    }

    public void dosingGuidelineDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        String id = req.getParameter("id");
        DosingGuideline g = id != null ? dosingGuidelineDao.findById(id) : null;
        if (g == null) { resp.sendRedirect("dosingGuideline"); return; }
        req.setAttribute("guideline", g);
        req.getRequestDispatcher("/views/dosing_guideline_detail.jsp").forward(req, resp);
    }
}
