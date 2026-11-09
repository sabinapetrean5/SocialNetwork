
package demo;

import domain.User;
import repository.Repository;
import repository.criteria.UserCriteria;

import java.util.function.Predicate;

/**
 * Demonstrates predicate-based user searches.
 *
 * Shows how search criteria can be combined using
 * AND, OR, NEGATE, and Predicate.not().
 */
public class UserSearchDemo {

    /**
     * Demonstrates combinations of user search criteria.
     *
     * @param userRepository repository containing the users to search
     */
    public static void demonstrateSearchCriteria(
            Repository<Long, User> userRepository) {

        Predicate<User> firstNameCriteria =
                UserCriteria.hasFirstName("Ana");

        Predicate<User> lastNameCriteria =
                UserCriteria.hasLastName("Pop");

        System.out.println(
                "AND - va contine si prenumele Ana si numele Pop:"
        );

        userRepository.findAll(
                firstNameCriteria.and(lastNameCriteria)
        ).forEach(System.out::println);

        System.out.println(
                "OR - va contine ori prenumele Ana si numele Pop:"
        );

        userRepository.findAll(
                firstNameCriteria.or(lastNameCriteria)
        ).forEach(System.out::println);

        System.out.println(
                "NEGATE - va contine toti utilizatorii care nu au numele de familie Pop:"
        );

        userRepository.findAll(
                lastNameCriteria.negate()
        ).forEach(System.out::println);

        System.out.println(
                "PREDICATE.NOT - va contine toti utilizatorii care nu au prenumele Ana:"
        );

        userRepository.findAll(
                Predicate.not(firstNameCriteria)
        ).forEach(System.out::println);
    }
}
