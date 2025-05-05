package labs.dirbrowser.domain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Directory {
    private final Path currentDirectory;
    private final Path baseDirectory;
    private final DateTimeFormatter formatter;
    private final List<DirectoryEntry> entries;

    public Directory(Path currentDirectory,
                     Path baseDirectory,
                     DateTimeFormatter formatter) {
        this.currentDirectory = currentDirectory;
        this.baseDirectory = baseDirectory;
        this.formatter = formatter;

        entries = getFilesInDirectory()
                .stream()
                .map(file -> new DirectoryEntry(
                        file,
                        baseDirectory,
                        formatter
                ))
                .toList();
    }

    public String getName() {
        return currentDirectory.toString();
    }

    public List<DirectoryEntry> getDirectories() {
        return entries
                .stream()
                .filter(entry -> entry.getType() == DirectoryEntryType.Directory)
                .toList();
    }

    public List<DirectoryEntry> getFiles() {
        return entries
                .stream()
                .filter(entry -> entry.getType() == DirectoryEntryType.File)
                .toList();
    }

    private List<File> getFilesInDirectory() {
        var names = currentDirectory.toFile().listFiles();

        if(names == null) {
            return new ArrayList<>();
        }

        return Arrays.stream(names).toList();
    }

    public Directory getParent() throws IOException {
        Path parent;

        if (Files.isSameFile(baseDirectory, currentDirectory))
            parent = currentDirectory;
        else
            parent = currentDirectory.getParent();

        return new Directory(
                parent,
                baseDirectory,
                formatter
        );
    }

    public String getRelativeDirectory() {
        return DirectoryHelper.replaceWindowsLineEndings(baseDirectory
                .relativize(currentDirectory)
                .toString());
    }

    public Path getPath() {
        return currentDirectory;
    }
}