package cn.edu.zju.controller;

import cn.edu.zju.servlet.DispatchServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class IndexController {
    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/", this::index);
    }
    public void index(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (AuthController.requireLogin(req, resp)) return;
        req.getRequestDispatcher("/views/index.jsp").forward(req, resp);
    }
}
