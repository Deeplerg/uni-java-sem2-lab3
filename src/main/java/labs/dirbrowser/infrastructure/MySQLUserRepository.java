package labs.dirbrowser.infrastructure;

import labs.dirbrowser.domain.User;
import labs.dirbrowser.domain.UserRepository;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLUserRepository implements UserRepository {
    private final UserDAO users;

    public MySQLUserRepository(String connectionUrl) {
        try {
            var connection = DriverManager.getConnection(connectionUrl);
            users = new UserDAO(connection);
            users.ensureTable();
        } catch (SQLException e) {
            throw new PersistenceException("Database connection failed.", e);
        }
    }

    @Override
    public User getById(UUID id) {
        try {
            var dataSet = users.getById(id);
            if(dataSet == null)
                return null;
            return dataSet.toUser();
        } catch (SQLException e) {
            throw new PersistenceException(String.format("Failed to retrieve user by id (%s).", id), e);
        }
    }

    @Override
    public User getByName(String username) {
        try {
            var dataSet = users.getByName(username);
            if(dataSet == null)
                return null;
            return dataSet.toUser();
        } catch (SQLException e) {
            throw new PersistenceException(String.format("Failed to retrieve user by name (%s).", username), e);
        }
    }

    @Override
    public void save(User user) {
        try {
            var dataSet = UserDataSet.fromUser(user);
            users.addOrUpdate(dataSet);
        } catch (SQLException e) {
            throw new PersistenceException(String.format("Failed to save user (%s).", user.getId()), e);
        }
    }
}
