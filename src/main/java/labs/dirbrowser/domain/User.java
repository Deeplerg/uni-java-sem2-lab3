package labs.dirbrowser.domain;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private String email;

    private User(UUID id, String username, String passwordHash, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public static User create(
            UUID id,
            String username,
            String password,
            String email,
            PasswordHasher hasher) {
        var hash = hasher.hash(password);
        return new User(id, username, hash, email);
    }

    public static User load(UUID id, String username, String passwordHash, String email) {
        return new User(id, username, passwordHash, email);
    }

    public boolean validatePassword(String password, PasswordHasher hasher) {
        return hasher.verify(password, this.passwordHash);
    }

    public void setPassword(String password, PasswordHasher hasher) {
        this.passwordHash = hasher.hash(password);
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
}