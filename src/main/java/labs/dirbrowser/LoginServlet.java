package labs.dirbrowser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

public class LoginServlet extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("errors", new ArrayList<String>());
        req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String user = req.getParameter("username");
        String password = req.getParameter("password");

        boolean isUserValid = userService.validateUser(user, password);

        var errors = new ArrayList<String>();

       if(!isUserValid) {
           errors.add("Invalid username or password.");
       }

       if(!errors.isEmpty()) {
           req.setAttribute("errors", errors);
           req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
           return;
       }

        HttpSession session = req.getSession();
        session.setAttribute("username", user);
        resp.sendRedirect(req.getContextPath() + "/browse/");
    }
}