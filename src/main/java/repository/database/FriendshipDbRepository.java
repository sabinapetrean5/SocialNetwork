
package repository.database;

import domain.Friendship;
import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import utils.Tuple;
import validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for friendships stored in PostgreSQL.
 *
 * <p>Each friendship is identified by a pair of user IDs.
 * The IDs are normalized into ascending order to ensure
 * that each friendship has a consistent identifier.</p>
 *
 * <p>Database operations use prepared statements and
 * try-with-resources for safe resource management.</p>
 */
public class FriendshipDbRepository
        implements Repository<Tuple<Long, Long>, Friendship> {

    private final Validator<Friendship> validator;
    private final String url;
    private final String user;
    private final String password;
    private final Repository<Long, User> userRepository;

    /**
     * Creates a PostgreSQL repository for friendships.
     *
     * @param validator validator for friendship data
     * @param url database connection URL
     * @param user database username
     * @param password database password
     * @param userRepository repository used to retrieve users
     * @throws IllegalArgumentException if validator or
     *         userRepository is null
     */
    public FriendshipDbRepository(
            Validator<Friendship> validator,
            String url,
            String user,
            String password,
            Repository<Long, User> userRepository) {

        if (validator == null || userRepository == null) {
            throw new IllegalArgumentException(
                    "Validator and user repository cannot be null."
            );
        }

        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
        this.userRepository = userRepository;
    }

    /**
     * Normalizes a friendship ID into ascending order.
     *
     * <p>For example, the pair (8, 3) becomes (3, 8).</p>
     *
     * @param id the friendship ID to normalize
     * @return a new tuple containing the ordered IDs
     * @throws IllegalArgumentException if the tuple or
     *         either of its values is null
     */
    private Tuple<Long, Long> normalize(Tuple<Long, Long> id) {
        if (id == null || id.getT1() == null || id.getT2() == null) {
            throw new IllegalArgumentException(
                    "Friendship IDs cannot be null."
            );
        }

        Long first = id.getT1();
        Long second = id.getT2();

        if (first <= second) {
            return new Tuple<>(first, second);
        }

        return new Tuple<>(second, first);
    }

    /**
     * Converts the current database row into a Friendship object.
     *
     * <p>The associated users are retrieved through
     * the user repository.</p>
     *
     * @param rs result set positioned on a friendship row
     * @return the friendship represented by the current row
     * @throws SQLException if reading the row fails
     * @throws RepositoryException if a referenced user is missing
     *         or user retrieval fails
     */
    private Friendship extractFriendship(ResultSet rs)
            throws SQLException {

        Long firstId = rs.getLong("id1");
        Long secondId = rs.getLong("id2");

        User firstUser = userRepository.findOne(firstId);
        User secondUser = userRepository.findOne(secondId);

        if (firstUser == null || secondUser == null) {
            throw new RepositoryException(
                    "Friendship references a missing user."
            );
        }

        Tuple<Long, Long> id =
                new Tuple<>(firstId, secondId);

        return new Friendship(
                id,
                firstUser,
                secondUser,
                rs.getTimestamp("friends_from").toLocalDateTime()
        );
    }

    /**
     * Finds a friendship by its pair of user IDs.
     *
     * <p>The IDs are normalized before searching.</p>
     *
     * @param id the pair of user IDs identifying the friendship
     * @return the friendship, or null if it does not exist
     * @throws IllegalArgumentException if the ID is invalid
     * @throws RepositoryException if database access fails
     */
    @Override
    public Friendship findOne(Tuple<Long, Long> id) {
        Tuple<Long, Long> normalized = normalize(id);

        String sql = """
                SELECT id1, id2, friends_from
                FROM friendships
                WHERE id1 = ? AND id2 = ?
                """;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, normalized.getT1());
            statement.setLong(2, normalized.getT2());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractFriendship(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find friendship: " + e.getMessage(), e
            );
        }
    }

    /**
     * Retrieves all friendships from the database.
     *
     * <p>Friendships are ordered by their two user IDs.</p>
     *
     * @return an iterable collection of all stored friendships
     * @throws RepositoryException if database access fails
     */
    @Override
    public Iterable<Friendship> findAll() {
        List<Friendship> friendships = new ArrayList<>();

        String sql = """
                SELECT id1, id2, friends_from
                FROM friendships
                ORDER BY id1, id2
                """;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                friendships.add(extractFriendship(rs));
            }

            return friendships;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not load friendships: " + e.getMessage(), e
            );
        }
    }

    /**
     * Saves a new friendship in the database.
     *
     * <p>The friendship ID is normalized and both users
     * are retrieved before validation and insertion.</p>
     *
     * <p>If the friendship already exists, the database
     * does not insert a duplicate.</p>
     *
     * @param entity the friendship to save
     * @return null if inserted, or the normalized friendship
     *         if it already exists
     * @throws IllegalArgumentException if the friendship or
     *         its ID is null, or a referenced user is missing
     * @throws RepositoryException if database access fails
     */
    @Override
    public Friendship save(Friendship entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Friendship and its ID cannot be null."
            );
        }

        Tuple<Long, Long> id = normalize(entity.getId());

        User firstUser = userRepository.findOne(id.getT1());
        User secondUser = userRepository.findOne(id.getT2());

        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException(
                    "Both users must exist."
            );
        }

        Friendship normalized = new Friendship(
                id,
                firstUser,
                secondUser,
                entity.getFriendshipDate()
        );

        validator.validate(normalized);

        String sql = """
                INSERT INTO friendships (id1, id2, friends_from)
                VALUES (?, ?, ?)
                ON CONFLICT (id1, id2) DO NOTHING
                """;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id.getT1());
            statement.setLong(2, id.getT2());
            statement.setTimestamp(
                    3,
                    Timestamp.valueOf(normalized.getFriendshipDate())
            );

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return normalized;
            }

            return null;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save friendship: " + e.getMessage(), e
            );
        }
    }

    /**
     * Updates the date of an existing friendship.
     *
     * <p>The friendship is normalized and validated
     * before the update is executed.</p>
     *
     * @param entity the friendship containing the updated date
     * @return null if updated, or the normalized friendship
     *         if its ID does not exist
     * @throws IllegalArgumentException if the friendship or
     *         its ID is null, or a referenced user is missing
     * @throws RepositoryException if database access fails
     */
    @Override
    public Friendship update(Friendship entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException(
                    "Friendship and its ID cannot be null."
            );
        }

        Tuple<Long, Long> id = normalize(entity.getId());

        User firstUser = userRepository.findOne(id.getT1());
        User secondUser = userRepository.findOne(id.getT2());

        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException(
                    "Both users must exist."
            );
        }

        Friendship normalized = new Friendship(
                id,
                firstUser,
                secondUser,
                entity.getFriendshipDate()
        );

        validator.validate(normalized);

        String sql = """
                UPDATE friendships
                SET friends_from = ?
                WHERE id1 = ? AND id2 = ?
                """;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(normalized.getFriendshipDate())
            );
            statement.setLong(2, id.getT1());
            statement.setLong(3, id.getT2());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return normalized;
            }

            return null;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not update friendship: " + e.getMessage(), e
            );
        }
    }

    /**
     * Deletes a friendship identified by two user IDs.
     *
     * <p>The deleted friendship is returned using
     * PostgreSQL's RETURNING clause.</p>
     *
     * @param id the pair of user IDs identifying the friendship
     * @return the deleted friendship, or null if it does not exist
     * @throws IllegalArgumentException if the ID is invalid
     * @throws RepositoryException if database access fails
     */
    @Override
    public Friendship delete(Tuple<Long, Long> id) {
        Tuple<Long, Long> normalized = normalize(id);

        String sql = """
                DELETE FROM friendships
                WHERE id1 = ? AND id2 = ?
                RETURNING id1, id2, friends_from
                """;

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, normalized.getT1());
            statement.setLong(2, normalized.getT2());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return extractFriendship(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not delete friendship: " + e.getMessage(), e
            );
        }
    }
}
