
package repository.criteria;

import domain.User;
import java.util.function.Predicate;

/**
 * Provides reusable search criteria for filtering users.
 */
public class UserCriteria {

    /**
     * Creates a predicate that checks whether a user has
     * the specified last name.
     *
     * @param lastName the last name to match
     * @return a predicate that returns true if the user's
     *         last name matches the specified value
     */
    public static Predicate<User> hasLastName(String lastName) {
        return user -> user.getLastName().equals(lastName);
    }

    /**
     * Creates a predicate that checks whether a user has
     * the specified first name.
     *
     * @param firstName the first name to match
     * @return a predicate that returns true if the user's
     *         first name matches the specified value
     */
    public static Predicate<User> hasFirstName(String firstName) {
        return user -> user.getFirstName().equals(firstName);
    }
}
