package labs.dirbrowser;

import jakarta.servlet.http.HttpServletResponse;

import java.nio.file.Path;

public class DirectoryHelper {
    public static String replaceWindowsLineEndings(String input) {
        return input.replace('\\', '/');
    }
}
