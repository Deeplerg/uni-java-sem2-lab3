package labs.dirbrowser.presentation;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import labs.dirbrowser.application.UserService;
import labs.dirbrowser.domain.User;
import labs.dirbrowser.infrastructure.Argon2PasswordHasher;
import labs.dirbrowser.infrastructure.MySQLUserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RegisterServlet extends HttpServlet {
    private UserService userService;
    private Path baseDirectory;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();

        this.baseDirectory = ConfigurationHelper.getFileRootDirectory(context);

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
        }
        if(password == null || password.isBlank()) {
            errors.add("Password cannot be empty.");
        }

        User user = null;
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

        Path userHome = baseDirectory.resolve(user.getId().toString()).normalize();

        if (!Files.exists(userHome)) {
            Files.createDirectory(userHome);
            System.out.println("Created home directory for user " + username + " at " + userHome);
        }

        HttpSession session = req.getSession();
        session.setAttribute("userId", user.getId().toString());
        resp.sendRedirect(req.getContextPath() + "/browse/");
    }
}