package labs.dirbrowser.presentation;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationHelper {
    public static Path getFileRootDirectory(ServletContext context) throws ServletException {
        String relativeDirectory = getParameterOrThrow(context, "fileRootRelativeDirectory");

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

    public static String getMySQLConnectionUrl(ServletContext context) {
        return getParameterOrThrow(context, "mySQLConnectionUrl");
    }

    public static Argon2Configuration getArgon2Configuration(ServletContext context) {
        return new Argon2Configuration(
                getIntegerParameterOrThrow(context, "argon2MemoryKiB"),
                getIntegerParameterOrThrow(context, "argon2Iterations"),
                getIntegerParameterOrThrow(context, "argon2ParallelismThreads"),
                getIntegerParameterOrThrow(context, "argon2KeyLengthBytes")
        );
    }

    private static int getIntegerParameterOrThrow(ServletContext context, String parameterName) {
        return Integer.parseInt(getParameterOrThrow(context, parameterName));
    }

    private static String getParameterOrThrow(ServletContext context, String parameterName) {
        var parameter = context.getInitParameter(parameterName);
        if(parameter == null) {
            throw new IllegalStateException(String.format("Parameter %s not found", parameterName));
        }
        return parameter;
    }
}
