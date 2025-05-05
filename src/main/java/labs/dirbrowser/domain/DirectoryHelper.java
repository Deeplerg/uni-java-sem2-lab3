package labs.dirbrowser.domain;

public class DirectoryHelper {
    public static String replaceWindowsLineEndings(String input) {
        return input.replace('\\', '/');
    }
}
