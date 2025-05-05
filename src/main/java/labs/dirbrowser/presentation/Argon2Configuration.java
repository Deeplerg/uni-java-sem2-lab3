package labs.dirbrowser.presentation;

public record Argon2Configuration(
        int memoryKiB,
        int iterations,
        int parallelismThreads,
        int keyLengthBytes) {
}
