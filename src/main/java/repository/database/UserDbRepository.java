
package repository.database;

import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC repository for users stored in a PostgreSQL database.
 *
 * <p>Provides CRUD operations using prepared statements and
 * manages database resources through try-with-resources.</p>
 *
 * <p>User IDs are generated automatically by PostgreSQL.</p>
 */
public class UserDbRepository implements Repository<Long, User> {

    private final Validator<User> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Creates a PostgreSQL repository for users.
     *
     * @param validator validator for user data
     * @param url database connection URL
     * @param user database username
     * @param password database password
     */
    public UserDbRepository(
            Validator<User> validator,
            String url,
            String user,
            String password) {

        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Converts the current database row into a User object.
     *
     * @param resultSet result set positioned on a user row
     * @return the user represented by the current row
     * @throws SQLException if reading the row fails
     */
    private User extractUser(ResultSet resultSet)
            throws SQLException {

        Long id = resultSet.getLong("id");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");

        return new User(firstName, lastName, id);
    }

    /**
     * Finds a user by its ID.
     *
     * @param id ID of the user to find
     * @return the user, or null if no user has the given ID
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if database access fails
     */
    @Override
    public User findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null."
            );
        }

        String sql =
                "SELECT id, first_name, last_name " +
                        "FROM users WHERE id = ?";

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractUser(resultSet);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not find user: " + e.getMessage(), e
            );
        }
    }

    /**
     * Retrieves all users from the database, ordered by ID.
     *
     * @return an iterable collection of all stored users
     * @throws RepositoryException if database access fails
     */
    @Override
    public Iterable<User> findAll() {
        List<User> users = new ArrayList<>();

        String sql =
                "SELECT id, first_name, last_name " +
                        "FROM users ORDER BY id";

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(extractUser(resultSet));
            }

            return users;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not load users: " + e.getMessage(), e
            );
        }
    }

    /**
     * Saves a new user in the database.
     *
     * <p>PostgreSQL generates the user's ID, which is assigned
     * to the provided entity after successful insertion.</p>
     *
     * <p>A temporary positive ID is used only during validation
     * because the existing validator requires a valid ID.</p>
     *
     * @param entity the user to save
     * @return null after successful insertion
     * @throws IllegalArgumentException if entity is null
     * @throws RepositoryException if database access fails
     */
    @Override
    public User save(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "User cannot be null."
            );
        }

        validator.validate(new User(
                entity.getFirstName(),
                entity.getLastName(),
                1L
        ));

        String sql =
                "INSERT INTO users (first_name, last_name) " +
                        "VALUES (?, ?) RETURNING id";

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    entity.setId(resultSet.getLong("id"));
                    return null;
                }

                throw new RepositoryException(
                        "PostgreSQL did not return a generated user ID."
                );
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not save user: " + e.getMessage(), e
            );
        }
    }

    /**
     * Updates the first and last name of an existing user.
     *
     * <p>The user is validated before the update is executed.</p>
     *
     * @param entity the user containing the updated data
     * @return null if the update succeeds, or the given entity
     *         if its ID does not exist in the database
     * @throws IllegalArgumentException if entity is null
     * @throws RepositoryException if database access fails
     */
    @Override
    public User update(User entity) {
        if (entity == null) {
            throw new IllegalArgumentException(
                    "User cannot be null."
            );
        }

        validator.validate(entity);

        String sql =
                "UPDATE users " +
                        "SET first_name = ?, last_name = ? " +
                        "WHERE id = ?";

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                return entity;
            }

            return null;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not update user: " + e.getMessage(), e
            );
        }
    }

    /**
     * Deletes a user from the database.
     *
     * <p>The deleted user's data is returned using PostgreSQL's
     * RETURNING clause.</p>
     *
     * @param id ID of the user to delete
     * @return the deleted user, or null if the ID does not exist
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if database access fails
     */
    @Override
    public User delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "User ID cannot be null."
            );
        }

        String sql =
                "DELETE FROM users WHERE id = ? " +
                        "RETURNING id, first_name, last_name";

        try (Connection connection =
                     DriverManager.getConnection(url, user, password);
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractUser(resultSet);
                }

                return null;
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Could not delete user: " + e.getMessage(), e
            );
        }
    }
}
