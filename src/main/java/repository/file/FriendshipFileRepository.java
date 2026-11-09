
package repository.file;

import domain.Friendship;
import domain.User;
import exceptions.RepositoryException;
import repository.Repository;
import utils.Tuple;
import validation.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * File repository for friendships.
 * File format: {@code idUtilizator1;idUtilizator2;dataPrieteniei}.
 */
public class FriendshipFileRepository
        extends AbstractFileRepository<Tuple<Long, Long>, Friendship> {

    private final Repository<Long, User> userRepository;

    /**
     * Creates the repository and loads existing friendships after the user
     * repository is already available.
     *
     * @param fileName relative friendship-file path
     * @param validator friendship validator
     * @param userRepository repository used to verify that both users exist
     * @throws IllegalArgumentException if the file name, validator,
     *         or user repository is invalid
     * @throws RepositoryException if an existing file cannot be read
     */
    public FriendshipFileRepository(
            String fileName,
            Validator<Friendship> validator,
            Repository<Long, User> userRepository) {

        super(fileName, validator);

        if (userRepository == null) {
            throw new IllegalArgumentException(
                    "User repository cannot be null."
            );
        }

        this.userRepository = userRepository;
        loadData();
    }

    /**
     * Converts a text line into a Friendship entity.
     *
     * @param line the line read from the file
     * @return the friendship extracted from the line
     * @throws IllegalArgumentException if the line has an invalid structure
     *         or references a user that does not exist
     * @throws RepositoryException if a user identifier or friendship date
     *         cannot be parsed
     */
    @Override
    protected Friendship extractEntity(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Expected 3 fields: idUtilizator1;idUtilizator2;dataPrieteniei."
            );
        }

        try {
            Long firstId = Long.parseLong(fields[0].trim());
            Long secondId = Long.parseLong(fields[1].trim());

            Tuple<Long, Long> id = normalize(firstId, secondId);

            User firstUser = userRepository.findOne(id.getT1());
            User secondUser = userRepository.findOne(id.getT2());

            if (firstUser == null || secondUser == null) {
                throw new IllegalArgumentException(
                        "Friendship references a user that does not exist."
                );
            }

            LocalDateTime friendshipDate =
                    LocalDateTime.parse(fields[2].trim());

            return new Friendship(
                    id, firstUser, secondUser, friendshipDate
            );

        } catch (NumberFormatException e) {
            throw new RepositoryException(
                    "Invalid friendship user ID.", e
            );

        } catch (DateTimeParseException e) {
            throw new RepositoryException(
                    "Invalid friendship date. Expected ISO format, for example 2026-10-19T10:30.",
                    e
            );
        }
    }

    /**
     * Converts a Friendship entity into its text-file representation.
     *
     * @param entity the friendship to convert
     * @return a line containing the normalized user identifiers
     *         and friendship date separated by semicolons
     */
    @Override
    protected String createEntityAsString(Friendship entity) {
        Tuple<Long, Long> id = normalize(
                entity.getId().getT1(),
                entity.getId().getT2()
        );

        return id.getT1() + ";"
                + id.getT2() + ";"
                + entity.getFriendshipDate();
    }

    /**
     * Finds a friendship using normalized user identifiers.
     *
     * @param id the friendship identifier
     * @return the matching friendship or {@code null} if it does not exist
     * @throws IllegalArgumentException if the identifier or either
     *         user identifier is null
     */
    @Override
    public Friendship findOne(Tuple<Long, Long> id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        return super.findOne(normalize(id.getT1(), id.getT2()));
    }

    /**
     * Saves a friendship between two existing users.
     *
     * @param entity the friendship to save
     * @return {@code null} on success, or the supplied normalized
     *         friendship if its identifier already exists
     * @throws IllegalArgumentException if the friendship or its identifier
     *         is null, or if either user does not exist
     */
    @Override
    public Friendship save(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }

        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity ID cannot be null.");
        }

        Tuple<Long, Long> id = normalize(
                entity.getId().getT1(),
                entity.getId().getT2()
        );

        User firstUser = userRepository.findOne(id.getT1());
        User secondUser = userRepository.findOne(id.getT2());

        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException(
                    "A friendship can be saved only between existing users."
            );
        }

        Friendship normalized = new Friendship(
                id, firstUser, secondUser, entity.getFriendshipDate()
        );

        return super.save(normalized);
    }

    /**
     * Updates an existing friendship using normalized user identifiers.
     *
     * @param entity the friendship containing the updated data
     * @return {@code null} on success, or the supplied normalized
     *         friendship if its identifier does not exist
     * @throws IllegalArgumentException if the friendship or its identifier
     *         is null, or if either user does not exist
     */
    @Override
    public Friendship update(Friendship entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }

        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity ID cannot be null.");
        }

        Tuple<Long, Long> id = normalize(
                entity.getId().getT1(),
                entity.getId().getT2()
        );

        User firstUser = userRepository.findOne(id.getT1());
        User secondUser = userRepository.findOne(id.getT2());

        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException(
                    "A friendship can exist only between existing users."
            );
        }

        return super.update(
                new Friendship(
                        id, firstUser, secondUser, entity.getFriendshipDate()
                )
        );
    }

    /**
     * Deletes a friendship using normalized user identifiers.
     *
     * @param id the friendship identifier
     * @throws IllegalArgumentException if the identifier or either
     *         user identifier is null
     */
    @Override
    public void delete(Tuple<Long, Long> id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        super.delete(normalize(id.getT1(), id.getT2()));
    }

    /**
     * Normalizes a friendship identifier by placing the smaller
     * user identifier first.
     *
     * @param firstId the identifier of the first user
     * @param secondId the identifier of the second user
     * @return a tuple containing the identifiers in ascending order
     * @throws IllegalArgumentException if either identifier is null
     */
    private Tuple<Long, Long> normalize(Long firstId, Long secondId) {
        if (firstId == null || secondId == null) {
            throw new IllegalArgumentException(
                    "Friendship user IDs cannot be null."
            );
        }

        if (firstId <= secondId) {
            return new Tuple<Long, Long>(firstId, secondId);
        }

        return new Tuple<Long, Long>(secondId, firstId);
    }
}
