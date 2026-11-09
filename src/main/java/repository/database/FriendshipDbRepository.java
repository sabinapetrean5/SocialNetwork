package repository.database;

import domain.Friendship;
import repository.Repository;
import utils.Tuple;
import validation.Validator;

/** Database repository skeleton reserved for Laboratory 4. */
public class FriendshipDbRepository implements Repository<Tuple<Long, Long>, Friendship> {
    private final Validator<Friendship> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Creates the database repository skeleton.
     *
     * @param validator friendship validator
     * @param url database URL
     * @param user database user
     * @param password database password
     */
    public FriendshipDbRepository(Validator<Friendship> validator, String url,
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

    @Override public Friendship findOne(Tuple<Long, Long> id) { throw unsupported(); }
    @Override public Iterable<Friendship> findAll() { throw unsupported(); }
    @Override public Friendship save(Friendship entity) { throw unsupported(); }
    @Override public Friendship update(Friendship entity) { throw unsupported(); }
    @Override public void delete(Tuple<Long, Long> id) { throw unsupported(); }
}
