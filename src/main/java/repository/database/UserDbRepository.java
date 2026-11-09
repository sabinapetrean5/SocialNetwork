package repository.database;

import domain.User;
import repository.Repository;
import validation.Validator;

/** Database repository skeleton reserved for Laboratory 4. */
public class UserDbRepository implements Repository<Long, User> {
    private final Validator<User> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Creates the database repository skeleton.
     *
     * @param validator user validator
     * @param url database URL
     * @param user database user
     * @param password database password
     */
    public UserDbRepository(Validator<User> validator, String url,
                                  String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(
                "DATABASE persistence is not implemented in Laboratory 3."
        );
    }

    @Override public User findOne(Long id) { throw unsupported(); }
    @Override public Iterable<User> findAll() { throw unsupported(); }
    @Override public User save(User entity) { throw unsupported(); }
    @Override public User update(User entity) { throw unsupported(); }
    @Override public void delete(Long id) { throw unsupported(); }
}
