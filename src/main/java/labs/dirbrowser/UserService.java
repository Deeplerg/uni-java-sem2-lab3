package labs.dirbrowser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private static final Map<String, User> users = new ConcurrentHashMap<>();

    public User findUser(String username) {
        return users.get(username);
    }

    /**
     * @param username Username
     * @param password Plain text password
     * @param email Email
     * @return {@link User} if a new user was created, or {@code null} if the user already exists
     */
    public User createUser(String username, String password, String email) {
        if(users.get(username) != null) {
            return null;
        }

        var user = new User(username, password, email);
        users.put(username, user);

        return user;
    }

    /**
     * @param username Username
     * @param password Plain text password
     * @return Whether the {@code username}/{@code password} combination is correct
     */
    public boolean validateUser(String username, String password) {
        var user = findUser(username);
        return user != null && user.validatePassword(password);
    }
}