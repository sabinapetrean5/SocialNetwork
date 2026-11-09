
package repository.file;

import domain.User;
import exceptions.RepositoryException;
import validation.Validator;

/**
 * File repository for users.
 * File format: {@code id;prenume;nume}.
 */
public class UserFileRepository extends AbstractFileRepository<Long, User> {

    /**
     * Creates the repository and loads existing users.
     *
     * @param fileName relative user-file path
     * @param validator user validator
     * @throws IllegalArgumentException if the file name or validator is invalid
     * @throws RepositoryException if an existing file cannot be read
     */
    public UserFileRepository(String fileName, Validator<User> validator) {
        super(fileName, validator);
        loadData();
    }

    /**
     * Converts a text line into a User entity.
     *
     * @param line the line read from the file
     * @return the user extracted from the line
     * @throws IllegalArgumentException if the line has an invalid structure
     * @throws RepositoryException if the user identifier is invalid
     */
    @Override
    protected User extractEntity(String line) {
        String[] fields = line.split(";", -1);

        if (fields.length != 3) {
            throw new IllegalArgumentException(
                    "Expected 3 fields: id;prenume;nume."
            );
        }

        try {
            Long id = Long.parseLong(fields[0].trim());

            return new User(
                    fields[1].trim(),
                    fields[2].trim(),
                    id
            );

        } catch (NumberFormatException e) {
            throw new RepositoryException(
                    "Invalid user ID: " + fields[0] + ".", e
            );
        }
    }

    /**
     * Converts a User entity into its text-file representation.
     *
     * @param entity the user to convert
     * @return a line containing the user identifier, first name,
     *         and last name separated by semicolons
     */
    @Override
    protected String createEntityAsString(User entity) {
        return entity.getId() + ";"
                + entity.getFirstName() + ";"
                + entity.getLastName();
    }
}
