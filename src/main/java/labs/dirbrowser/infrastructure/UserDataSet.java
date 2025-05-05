package labs.dirbrowser.infrastructure;

import labs.dirbrowser.domain.User;

import java.util.UUID;

public class UserDataSet {
    private UUID id;
    private String username;
    private String passwordHash;
    private String email;

    public UserDataSet(
            UUID id,
            String username,
            String passwordHash,
            String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User toUser() {
        return User.load(id, username, passwordHash, email);
    }

    public static UserDataSet fromUser(User user) {
        return new UserDataSet(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getEmail());
    }
}
