package labs.dirbrowser;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BrowseServlet extends HttpServlet {

    private Path baseDirectory;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();

        this.baseDirectory = ConfigurationHelper.getFileRootDirectory(context);
        System.out.println("Serving files from base directory: " + this.baseDirectory);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = ensureLoggedIn(req, resp);
        if(username == null) {
            return;
        }

        String requestedRelativePath = req.getParameter("path");
        if (requestedRelativePath == null || requestedRelativePath.isEmpty() || requestedRelativePath.equals("/")) {
            requestedRelativePath = ""; // Root of our base directory
        }

        // just in case
        requestedRelativePath = DirectoryHelper.replaceWindowsLineEndings(requestedRelativePath);

        var requestedRelativePathWithinUserDirectory = Paths.get(username, requestedRelativePath);

        Path requestedPath = this.baseDirectory.resolve(requestedRelativePathWithinUserDirectory).normalize();
        Path userBaseDirectory = getUserBaseDirectory(username);

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
            serveDirectory(requestedPath, username, resp, req);
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

    private void serveDirectory(Path directoryPath, String username, HttpServletResponse resp, HttpServletRequest req)
            throws ServletException, IOException {

        var directory = new Directory(directoryPath, getUserBaseDirectory(username), DATE_FORMATTER);

        var generationTime = LocalDateTime.now();
        String formattedGenerationTime = generationTime.format(DATE_FORMATTER);

        req.setAttribute("directory", directory);
        req.setAttribute("generationTime", formattedGenerationTime);

        req.getRequestDispatcher("/WEB-INF/browse.jsp").forward(req, resp);
    }

    /**
     * @return Username if logged in, otherwise, {@code null}.
     * @throws IOException
     */
    private String ensureLoggedIn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("username") != null;

        if(!loggedIn) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }

        return (String) session.getAttribute("username");
    }

    private Path getUserBaseDirectory(String username) {
        return this.baseDirectory.resolve(username).normalize();
    }
}