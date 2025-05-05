package labs.dirbrowser.domain;

import java.util.UUID;

public interface UserRepository {
    User getById(UUID id);
    User getByName(String username);
    void save(User user);
}
