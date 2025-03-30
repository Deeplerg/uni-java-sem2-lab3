package labs.dirbrowser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DirectoryEntry {
    private final File file;
    private final Path baseDirectory;
    private final DateTimeFormatter formatter;

    public DirectoryEntry(File file,
                          Path baseDirectory,
                          DateTimeFormatter formatter) {
        this.file = file;
        this.baseDirectory = baseDirectory;
        this.formatter = formatter;
    }

    public String getName() {
        return file.getName();
    }

    public DirectoryEntryType getType() {
        if(file.isDirectory()) {
            return DirectoryEntryType.Directory;
        }

        return DirectoryEntryType.File;
    }

    public String getRelativePath() {
        return DirectoryHelper.replaceWindowsLineEndings(baseDirectory
                .relativize(file.toPath())
                .toString());
    }

    public String getLastModified() {
        return LocalDateTime
                .ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault())
                .format(formatter);
    }

    public String getSize() {
        long bytes = file.length();
        return getBytesRepresentation(bytes);
    }

    private String getBytesRepresentation(long bytes) {
        if (bytes < 1024)
            return bytes + " B";

        return getKilobytesOrHigher(bytes / 1024);
    }

    private String getKilobytesOrHigher(long kilobytes) {
        if (kilobytes < 1024)
            return kilobytes + " KB";

        return getMegabytesOrHigher(kilobytes / 1024);
    }

    private String getMegabytesOrHigher(long megabytes) {
        if (megabytes < 1024)
            return megabytes + " MB";

        return getGigabytes(megabytes / 1024);
    }

    private String getGigabytes(long megabytes) {
        return megabytes + " GB";
    }
}
