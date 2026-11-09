
import demo.UserSearchDemo;
import domain.Friendship;
import domain.User;
import repository.Repository;
import repository.factory.RepositoryFactory;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;
import ui.ConsoleUI;
import utils.Tuple;
import validation.FriendshipValidator;
import validation.UserValidator;
import validation.ValidationException;
import validation.Validator;

import java.util.stream.IntStream;

/**
 * Application entry point.
 *
 * Initializes validators, repositories, services, and the console UI.
 * Demonstrates lambda-based validation and method references.
 */
public class Main {

    /**
     * Creates repositories through the factory and starts the console UI.
     *
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {

        UserValidator userValidator = new UserValidator();

        Validator<User> nameNoDigitsValidator = entity -> {
            String firstName = entity.getFirstName();
            String lastName = entity.getLastName();

            IntStream firstNameDigits = firstName.chars();
            IntStream lastNameDigits = lastName.chars();

            if (firstNameDigits.anyMatch(Character::isDigit)
                    || lastNameDigits.anyMatch(Character::isDigit)) {
                throw new ValidationException(
                        "The name cannot contain digits\n"
                );
            }
        };

        Validator<User> combinedValidator =
                userValidator.andThen(nameNoDigitsValidator);

        FriendshipValidator friendshipValidator =
                new FriendshipValidator();

        RepositoryFactory factory = new RepositoryFactory();

        Repository<Long, User> userRepository =
                factory.createUserRepository(combinedValidator);

        UserSearchDemo.demonstrateSearchCriteria(userRepository);

        Repository<Tuple<Long, Long>, Friendship> friendshipRepository =
                factory.createFriendshipRepository(friendshipValidator);

        UserService userService = new UserService(userRepository);

        FriendshipService friendshipService =
                new FriendshipService(
                        friendshipRepository,
                        userService,
                        friendshipValidator
                );

        CommunityService communityService =
                new CommunityService(userService, friendshipService);

        ConsoleUI ui = new ConsoleUI(
                userService,
                friendshipService,
                communityService
        );

        ui.run();
    }
}
