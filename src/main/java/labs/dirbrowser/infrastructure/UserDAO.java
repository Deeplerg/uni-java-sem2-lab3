package labs.dirbrowser.infrastructure;

import java.sql.SQLException;
import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserDAO implements AutoCloseable {
    private final Session session;
    private final Transaction transaction;

    public UserDAO(Session session) {
        this.session = session;
        this.transaction = session.beginTransaction();
    }

    public UserDataSet getById(UUID id) {
        return session.get(UserDataSet.class, id);
    }

    public UserDataSet getByName(String username) {
        var criteriaBuilder = session.getCriteriaBuilder();
        var criteriaQuery = criteriaBuilder.createQuery();

        var root = criteriaQuery.from(UserDataSet.class);

        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("username"), username));

        var query = session.createQuery(criteriaQuery);
        return (UserDataSet) query.getSingleResultOrNull();
    }

    public void addOrUpdate(UserDataSet user) {
        session.merge(user);
    }

    public void save() {
        transaction.commit();
        transaction.begin();
    }

    @Override
    public void close() {
        try {
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace(); // yeah, probably
        }
        finally {
            session.close();
        }
    }
}
