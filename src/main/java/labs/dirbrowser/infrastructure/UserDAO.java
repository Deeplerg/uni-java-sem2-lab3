package labs.dirbrowser.infrastructure;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public void ensureTable() throws SQLException {
        var statement = connection.createStatement();
        statement.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(255) NOT NULL UNIQUE,
                password_hash VARCHAR(512) NOT NULL,
                email VARCHAR(255) NOT NULL
            );
            """);
    }

    public UserDataSet getById(UUID id) throws SQLException {
        var statement = connection.prepareStatement("""
            SELECT * FROM users
            WHERE id=?
            """);

        statement.setString(1, id.toString());

        var result = statement.executeQuery();
        return mapSingleUser(result);
    }

    public UserDataSet getByName(String username) throws SQLException {
        var statement = connection.prepareStatement("""
            SELECT * FROM users
            WHERE username=?
            """);

        statement.setString(1, username);

        var result = statement.executeQuery();
        return mapSingleUser(result);
    }

    public void addOrUpdate(UserDataSet user) throws SQLException {
        var statement = connection.prepareStatement("""
            INSERT INTO users (id, username, password_hash, email)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                username = VALUES(username),
                password_hash = VALUES(password_hash),
                email = VALUES(email)
            """);

        statement.setString(1, user.getId().toString());
        statement.setString(2, user.getUsername());
        statement.setString(3, user.getPasswordHash());
        statement.setString(4, user.getEmail());

        statement.executeUpdate();
    }

    private UserDataSet mapSingleUser(ResultSet result) throws SQLException {
        if(result.next())
            return mapTable(result);
        else
            return null;
    }

    private UserDataSet mapTable(ResultSet result) throws SQLException {
        var id = result.getString(1);
        var username = result.getString(2);
        var passwordHash = result.getString(3);
        var email = result.getString(4);

        var convertedId = UUID.fromString(id);

        return new UserDataSet(convertedId, username, passwordHash, email);
    }
}
