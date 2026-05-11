package cn.edu.zju.servlet;

import cn.edu.zju.controller.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(DispatchServlet.class);

    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> getMapping;
    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> postMapping;

    private final HttpConsumer<HttpServletRequest, HttpServletResponse> notFound = (req, resp) -> {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().write("404 Not Found: " + req.getPathInfo());
    };

    public class Dispatcher {
        public void registerGetMapping(String path,
                HttpConsumer<HttpServletRequest, HttpServletResponse> h) { getMapping.put(path, h); }
        public void registerPostMapping(String path,
                HttpConsumer<HttpServletRequest, HttpServletResponse> h) { postMapping.put(path, h); }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        getMapping  = new ConcurrentHashMap<>();
        postMapping = new ConcurrentHashMap<>();
        Dispatcher d = new Dispatcher();
        new AuthController().register(d);
        new IndexController().register(d);
        new KnowledgeBaseController().register(d);
        new MatchingController().register(d);
        log.info("DispatchServlet ready. GET={}", getMapping.keySet());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("{} {}", req.getMethod(), req.getPathInfo());
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        getMapping.getOrDefault(path(req), notFound).accept(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        postMapping.getOrDefault(path(req), notFound).accept(req, resp);
    }

    private String path(HttpServletRequest req) {
        String p = req.getPathInfo();
        return p == null ? "/" : p;
    }
}
