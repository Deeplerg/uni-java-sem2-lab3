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

public class MainServlet extends HttpServlet {

    private Path baseDirectory;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext context = config.getServletContext();

        this.baseDirectory = getPath(context);
        System.out.println("Serving files from base directory: " + this.baseDirectory);
    }

    private Path getPath(ServletContext context) throws ServletException {
        String realPath = context.getRealPath("/fileroot");

        if (realPath == null) {
            throw new ServletException("Cannot find fileroot directory");
        }
        Path baseDirectory = Paths.get(realPath);

        if (!Files.isDirectory(baseDirectory)) {
            throw new ServletException("fileroot is not a directory");
        }

        return baseDirectory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String requestedRelativePath = req.getParameter("path");
        if (requestedRelativePath == null || requestedRelativePath.isEmpty() || requestedRelativePath.equals("/")) {
            requestedRelativePath = ""; // Root of our base directory
        }

        // just in case
        requestedRelativePath = DirectoryHelper.replaceWindowsLineEndings(requestedRelativePath);

        // in case someone tries "../"
        Path requestedPath = this.baseDirectory.resolve(requestedRelativePath).normalize();
        if (!requestedPath.startsWith(this.baseDirectory)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "lmao no");
            return;
        }

        if(Files.notExists(requestedPath)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Path not found.");
            return;
        }

        if (Files.isDirectory(requestedPath)) {
            serveDirectory(requestedPath, resp, req);
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

    private void serveDirectory(Path directoryPath, HttpServletResponse resp, HttpServletRequest req)
            throws ServletException, IOException {

        var directory = new Directory(directoryPath, baseDirectory, DATE_FORMATTER);

        var generationTime = LocalDateTime.now();
        String formattedGenerationTime = generationTime.format(DATE_FORMATTER);

        req.setAttribute("directory", directory);
        req.setAttribute("generationTime", formattedGenerationTime);

        req.getRequestDispatcher("/WEB-INF/browser.jsp").forward(req, resp);
    }
}