package labs.dirbrowser.application;
import labs.dirbrowser.domain.PasswordHasher;
import labs.dirbrowser.domain.User;
import labs.dirbrowser.domain.UserRepository;
import java.util.UUID;

public class UserService {
    private final UserRepository userRepository;
    private final PasswordHasher hasher;

    public UserService(
            UserRepository userRepository,
            PasswordHasher hasher) {
        this.userRepository = userRepository;
        this.hasher = hasher;
    }

    public User findUserById(UUID id) {
        return userRepository.getById(id);
    }
    public User findUserByName(String username) {
        return userRepository.getByName(username);
    }

    /**
     * @param username Username
     * @param password Plain text password
     * @param email Email
     * @return {@link User} if a new user was created, or {@code null} if the user already exists
     */
    public User createUser(String username, String password, String email) {
        if(findUserByName(username) != null) {
            return null;
        }

        var id = UUID.randomUUID();

        var user = User.create(id, username, password, email, hasher);
        userRepository.save(user);

        return user;
    }

    /**
     * @param user User
     * @param password Plain text password
     * @return Whether the {@code username}/{@code password} combination is correct
     */
    public boolean validateUser(User user, String password) {
        return user != null && user.validatePassword(password, hasher);
    }
}