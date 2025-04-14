package labs.dirbrowser;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationHelper {
    public static Path getFileRootDirectory(ServletContext context) throws ServletException {
        String relativeDirectory = "/fileroot";

        String realPath = context.getRealPath(relativeDirectory);

        if (realPath == null) {
            throw new ServletException("Cannot find directory " + relativeDirectory);
        }
        Path baseDirectory = Paths.get(realPath);

        if (!Files.isDirectory(baseDirectory)) {
            throw new ServletException(relativeDirectory + " is not a directory");
        }

        return baseDirectory;
    }
}
