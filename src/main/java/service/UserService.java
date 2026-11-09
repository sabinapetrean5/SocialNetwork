
package service;

import domain.User;
import repository.Repository;
import validation.ValidationException;

import java.util.Optional;

/**
 * Provides operations for managing users in the social network.
 */
public class UserService {

    private final Repository<Long, User> userRepository;

    /**
     * Creates a user service using the specified repository.
     *
     * @param userRepository the repository used to manage users
     */
    public UserService(Repository<Long, User> userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all users.
     *
     * @return an iterable containing all users
     */
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by identifier.
     *
     * @param id the identifier of the user
     * @return an Optional containing the user, or an empty Optional
     *         if no such user exists
     * @throws IllegalArgumentException if the identifier is null
     */
    public Optional<User> findUserById(Long id) {
        return Optional.ofNullable(userRepository.findOne(id));
    }

    /**
     * Finds a user by identifier.
     * Preserves compatibility with existing service calls.
     *
     * @param id the identifier of the user
     * @return the matching user or null if it does not exist
     * @throws IllegalArgumentException if the identifier is null
     */
    public User getUserById(Long id) {
        return findUserById(id).orElse(null);
    }

    /**
     * Adds a new user.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param id the user's identifier
     * @return true if the user was added successfully;
     *         false if the identifier already exists
     * @throws ValidationException if the user is invalid
     * @throws IllegalArgumentException if the user or its identifier is null
     */
    public boolean addUser(String firstName, String lastName, Long id) {
        User user = new User(firstName, lastName, id);

        User existingUser = userRepository.save(user);

        return existingUser == null;
    }

    /**
     * Updates an existing user.
     *
     * @param firstName the new first name
     * @param lastName the new last name
     * @param id the identifier of the user to update
     * @return true if the user was updated successfully;
     *         false if the user does not exist
     * @throws ValidationException if the updated user is invalid
     * @throws IllegalArgumentException if the user or its identifier is null
     */
    public boolean updateUser(String firstName, String lastName, Long id) {
        User user = new User(firstName, lastName, id);

        User existingUser = userRepository.update(user);

        return existingUser == null;
    }

    /**
     * Deletes an existing user.
     *
     * @param id the identifier of the user to delete
     * @throws ValidationException if the identifier is null
     *         or the user does not exist
     */
    public void deleteUser(Long id) {
        if (id == null) {
            throw new ValidationException("User ID cannot be null.");
        }

        findUserById(id).orElseThrow(() ->
                new ValidationException(
                        "User with ID " + id + " does not exist."
                )
        );

        userRepository.delete(id);
    }
}
