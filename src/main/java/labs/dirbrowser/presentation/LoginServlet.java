package labs.dirbrowser.presentation;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import labs.dirbrowser.application.UserService;
import labs.dirbrowser.infrastructure.Argon2PasswordHasher;
import labs.dirbrowser.infrastructure.MySQLUserRepository;

import java.io.IOException;
import java.util.ArrayList;

public class LoginServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();

        String connectionUrl = ConfigurationHelper.getMySQLConnectionUrl(context);
        var userRepository = new MySQLUserRepository(connectionUrl);

        var argon2Configuration = ConfigurationHelper.getArgon2Configuration(context);
        var hasher = new Argon2PasswordHasher(
                argon2Configuration.memoryKiB(),
                argon2Configuration.iterations(),
                argon2Configuration.parallelismThreads(),
                argon2Configuration.keyLengthBytes()
        );

        userService = new UserService(userRepository, hasher);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("errors", new ArrayList<String>());
        req.getRequestDispatcher("/WEB-INF/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        var user = userService.findUserByName(username);
        boolean isUserValid = user != null && userService.validateUser(user, password);

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
        session.setAttribute("userId", user.getId().toString());
        resp.sendRedirect(req.getContextPath() + "/browse/");
    }
}