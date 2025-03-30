package labs.dirbrowser;

public class DirectoryHelper {
    public static String replaceWindowsLineEndings(String input) {
        return input.replace('\\', '/');
    }
}
