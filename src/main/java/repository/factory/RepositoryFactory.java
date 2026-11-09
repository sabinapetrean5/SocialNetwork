
package repository.factory;

import domain.Friendship;
import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import repository.database.FriendshipDbRepository;
import repository.database.UserDbRepository;
import repository.file.FriendshipFileRepository;
import repository.file.UserFileRepository;
import repository.memory.InMemoryRepository;
import utils.Tuple;
import validation.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * Simple Factory. This is the only application class that knows the concrete
 * repository implementations.
 *
 * Repository creation is delegated to Function objects that receive
 * a validator and return the corresponding repository implementation.
 */
public class RepositoryFactory {

    private final Properties properties;
    private final RepositoryType repositoryType;
    private Repository<Long, User> createdUserRepository;

    /**
     * Maps each repository type to a Function that creates
     * a user repository using the supplied user validator.
     */
    private final Map<RepositoryType,
            Function<Validator<User>, Repository<Long, User>>> userCreators;

    /**
     * Maps each repository type to a Function that creates
     * a friendship repository using the supplied friendship validator.
     */
    private final Map<RepositoryType,
            Function<Validator<Friendship>,
                    Repository<Tuple<Long, Long>, Friendship>>> friendshipCreators;

    /**
     * Loads repository configuration from src/main/resources/config.properties
     * and initializes the repository creation functions.
     *
     * @throws RepositoryException if the configuration file is missing,
     *         unreadable, incomplete, or contains an unknown repository type
     */
    public RepositoryFactory() {
        properties = new Properties();
        Path configPath = Paths.get("src/main/resources/config.properties");

        try (InputStream input = Files.newInputStream(configPath)) {
            properties.load(input);
        } catch (IOException e) {
            throw new RepositoryException(
                    "Could not load src/main/resources/config.properties.", e
            );
        }

        String configuredType = requiredProperty("repository.type");

        try {
            repositoryType = RepositoryType.valueOf(
                    configuredType.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RepositoryException(
                    "Unknown repository.type '" + configuredType
                            + "'. Expected MEMORY, FILE or DATABASE.", e
            );
        }

        userCreators = new EnumMap<>(RepositoryType.class);

        userCreators.put(
                RepositoryType.MEMORY,
                InMemoryRepository::new
        );

        userCreators.put(
                RepositoryType.FILE,
                validator -> new UserFileRepository(
                        requiredProperty("users.file"),
                        validator
                )
        );

        userCreators.put(
                RepositoryType.DATABASE,
                validator -> new UserDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")
                )
        );

        friendshipCreators = new EnumMap<>(RepositoryType.class);

        friendshipCreators.put(
                RepositoryType.MEMORY,
                InMemoryRepository::new
        );

        friendshipCreators.put(
                RepositoryType.FILE,
                validator -> new FriendshipFileRepository(
                        requiredProperty("friendships.file"),
                        validator,
                        createdUserRepository
                )
        );

        friendshipCreators.put(
                RepositoryType.DATABASE,
                validator -> new FriendshipDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")
                )
        );
    }

    /**
     * Creates the user repository selected in config.properties.
     * The appropriate Function receives the user validator
     * and returns the configured repository implementation.
     *
     * @param validator the validator used for user entities
     * @return the repository abstraction for users
     * @throws RepositoryException if required configuration is missing
     *         or the repository type is unsupported
     */
    public Repository<Long, User> createUserRepository(
            Validator<User> validator) {

        Function<Validator<User>, Repository<Long, User>> creator =
                userCreators.get(repositoryType);

        if (creator == null) {
            throw new RepositoryException(
                    "Unsupported repository type: " + repositoryType
            );
        }

        createdUserRepository = creator.apply(validator);

        return createdUserRepository;
    }

    /**
     * Creates the friendship repository selected in config.properties.
     * The user repository must be created first so file friendships can verify
     * that both referenced users exist.
     *
     * The appropriate Function receives the friendship validator
     * and returns the configured repository implementation.
     *
     * @param validator the validator used for friendship entities
     * @return the repository abstraction for friendships
     * @throws RepositoryException if the user repository has not been created,
     *         required configuration is missing, or the repository type
     *         is unsupported
     */
    public Repository<Tuple<Long, Long>, Friendship> createFriendshipRepository(
            Validator<Friendship> validator) {

        if (createdUserRepository == null) {
            throw new RepositoryException(
                    "Create the user repository before the friendship repository."
            );
        }

        Function<Validator<Friendship>,
                Repository<Tuple<Long, Long>, Friendship>> creator =
                friendshipCreators.get(repositoryType);

        if (creator == null) {
            throw new RepositoryException(
                    "Unsupported repository type: " + repositoryType
            );
        }

        return creator.apply(validator);
    }

    /**
     * Returns the configured persistence type.
     *
     * @return the configured repository type
     */
    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    /**
     * Retrieves a required configuration property.
     *
     * @param key the name of the configuration property
     * @return the trimmed value of the property
     * @throws RepositoryException if the property is missing or empty
     */
    private String requiredProperty(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.trim().isEmpty()) {
            throw new RepositoryException(
                    "Missing configuration property: " + key
            );
        }

        return value.trim();
    }
}
