package labs.dirbrowser;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RegisterServlet extends HttpServlet {
    private final UserService userService = new UserService();
    private Path baseDirectory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();

        this.baseDirectory = ConfigurationHelper.getFileRootDirectory(context);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("errors", new ArrayList<String>());
        req.getRequestDispatcher("/WEB-INF/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        var errors = new ArrayList<String>();

        req.setAttribute("errors", errors);

        if(username == null || username.isBlank()) {
            errors.add("Username cannot be empty.");
        } else {
            Path userHome = baseDirectory.resolve(username).normalize();

            if (!userHome.startsWith(this.baseDirectory)) {
                errors.add("What are you trying to do?");
            }
        }

        if(password == null || password.isBlank()) {
            errors.add("Password cannot be empty.");
        }

        User user;
        if(errors.isEmpty()) {
            user = userService.createUser(username, password, email);

            if(user == null) {
                errors.add("User already exists.");
            }
        }

        if(!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.getRequestDispatcher("/WEB-INF/register.jsp").forward(req, resp);
            return;
        }

        Path userHome = baseDirectory.resolve(username).normalize();

        if (!Files.exists(userHome)) {
            Files.createDirectory(userHome);
            System.out.println("Created home directory for user " + username + " at " + userHome);
        }

        HttpSession session = req.getSession();
        session.setAttribute("username", username);
        resp.sendRedirect(req.getContextPath() + "/browse/");
    }
}