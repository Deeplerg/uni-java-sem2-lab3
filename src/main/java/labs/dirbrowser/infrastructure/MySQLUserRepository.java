package labs.dirbrowser.infrastructure;

import labs.dirbrowser.domain.User;
import labs.dirbrowser.domain.UserRepository;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.UUID;

public class MySQLUserRepository implements UserRepository {
    private final SessionFactory sessionFactory;

    public MySQLUserRepository(String connectionUrl) {
        var configuration = new Configuration();
        configure(configuration, connectionUrl);

        sessionFactory = configuration.buildSessionFactory();
    }

    @Override
    public User getById(UUID id) {
        var session = sessionFactory.openSession();
        try (var users = new UserDAO(session)) {
            var dataSet = users.getById(id);
            if (dataSet == null)
                return null;
            return dataSet.toUser();
        }
    }

    @Override
    public User getByName(String username) {
        var session = sessionFactory.openSession();
        try (var users = new UserDAO(session)) {
            var dataSet = users.getByName(username);
            if (dataSet == null)
                return null;
            return dataSet.toUser();
        }
    }

    @Override
    public void save(User user) {
        var session = sessionFactory.openSession();
        try (var users = new UserDAO(session)) {
            var dataSet = UserDataSet.fromUser(user);
            users.addOrUpdate(dataSet);
            users.save();
        }
    }

    private void configure(Configuration configuration, String connectionUrl) {
        configuration.addAnnotatedClass(UserDataSet.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        configuration.setProperty("hibernate.connection.url", connectionUrl);
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
    }
}
