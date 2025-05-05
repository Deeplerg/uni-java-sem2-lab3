package labs.dirbrowser.presentation;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import labs.dirbrowser.application.UserService;
import labs.dirbrowser.domain.Directory;
import labs.dirbrowser.domain.DirectoryHelper;
import labs.dirbrowser.domain.User;
import labs.dirbrowser.infrastructure.Argon2PasswordHasher;
import labs.dirbrowser.infrastructure.MySQLUserRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class BrowseServlet extends HttpServlet {
    private UserService userService;
    private Path baseDirectory;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

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

        System.out.println("Serving files from base directory: " + this.baseDirectory);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UUID userUUID = ensureLoggedInOrRedirect(req, resp);
        if(userUUID == null) {
            return;
        }

        var user = userService.findUserById(userUUID);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "User not found.");
            return;
        }

        String userId = user.getId().toString();

        String requestedRelativePath = req.getParameter("path");
        if (requestedRelativePath == null || requestedRelativePath.isEmpty() || requestedRelativePath.equals("/")) {
            requestedRelativePath = ""; // Root of our base directory
        }

        // just in case
        requestedRelativePath = DirectoryHelper.replaceWindowsLineEndings(requestedRelativePath);

        var requestedRelativePathWithinUserDirectory = Paths.get(userId, requestedRelativePath);

        Path requestedPath = this.baseDirectory.resolve(requestedRelativePathWithinUserDirectory).normalize();
        Path userBaseDirectory = getUserBaseDirectory(userId);
        // in case someone tries "../"
        if (!requestedPath.startsWith(userBaseDirectory)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "lmao no");
            return;
        }

        if(Files.notExists(requestedPath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Path not found.");
            return;
        }

        if (Files.isDirectory(requestedPath)) {
            serveDirectory(requestedPath, user, resp, req);
        } else {
            serveFile(requestedPath, resp);
        }
    }

    private void serveFile(Path filePath, HttpServletResponse resp) throws IOException {
        var context = getServletContext();

        String mime = context.getMimeType(filePath.toString());
        String filename = filePath.getFileName().toString();
        long length = Files.size(filePath);

        resp.setContentType(mime);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.setContentLengthLong(length);

        try (OutputStream out = resp.getOutputStream()) {
            Files.copy(filePath, out);
        }
    }

    private void serveDirectory(Path directoryPath, User user, HttpServletResponse resp, HttpServletRequest req)
            throws ServletException, IOException {
        var userId = user.getId().toString();
        var directory = new Directory(directoryPath, getUserBaseDirectory(userId), DATE_FORMATTER);

        var generationTime = LocalDateTime.now();
        String formattedGenerationTime = generationTime.format(DATE_FORMATTER);

        req.setAttribute("directory", directory);
        req.setAttribute("generationTime", formattedGenerationTime);
        req.setAttribute("username", user.getUsername());

        req.getRequestDispatcher("/WEB-INF/browse.jsp").forward(req, resp);
    }

    /**
     * @return User id if logged in, otherwise, {@code null} and redirect to the login page.
     * @throws IOException
     */
    private UUID ensureLoggedInOrRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("userId") != null;

        if(!loggedIn) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        return UUID.fromString(session.getAttribute("userId").toString());
    }

    private Path getUserBaseDirectory(String userId) {
        return this.baseDirectory.resolve(userId).normalize();
    }
}