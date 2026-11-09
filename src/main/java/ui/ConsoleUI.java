
package ui;

import domain.User;
import exceptions.RepositoryException;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;
import validation.ValidationException;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Console user interface for the social network.
 */
public class ConsoleUI {

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CommunityService communityService;
    private final Scanner scanner;

    /**
     * Creates the console UI.
     *
     * @param userService service for user operations
     * @param friendshipService service for friendship operations
     * @param communityService service for community operations
     */
    public ConsoleUI(
            UserService userService,
            FriendshipService friendshipService,
            CommunityService communityService) {

        this.userService = userService;
        this.friendshipService = friendshipService;
        this.communityService = communityService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Runs the console menu and handles errors that can be shown to the user.
     */
    public void run() {
        boolean running = true;

        while (running) {
            printMenu();

            System.out.print("Choose an option: ");
            String option = scanner.nextLine().trim();

            try {
                switch (option) {
                    case "1":
                        displayUsers();
                        break;
                    case "2":
                        addUser();
                        break;
                    case "3":
                        removeUser();
                        break;
                    case "4":
                        displayFriendships();
                        break;
                    case "5":
                        addFriend();
                        break;
                    case "6":
                        removeFriend();
                        break;
                    case "7":
                        displayNumberOfCommunities();
                        break;
                    case "8":
                        displayMostSociableCommunity();
                        break;
                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option.");
                        break;
                }

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid number. Please enter a numeric ID."
                );

            } catch (ValidationException e) {
                System.out.println(
                        "Validation error: " + e.getMessage()
                );

            } catch (RepositoryException e) {
                System.out.println(
                        "Persistence error: " + e.getMessage()
                );

            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Invalid argument: " + e.getMessage()
                );

            } catch (UnsupportedOperationException e) {
                System.out.println(
                        "Operation unavailable: " + e.getMessage()
                );
            }

            System.out.println();
        }
    }

    /**
     * Displays the available console menu options.
     */
    private void printMenu() {
        System.out.println("Social Network Menu");
        System.out.println("1. Display all users");
        System.out.println("2. Add user");
        System.out.println("3. Remove user");
        System.out.println("4. Display all friendships");
        System.out.println("5. Add friendship");
        System.out.println("6. Remove friendship");
        System.out.println("7. Display number of communities");
        System.out.println("8. Display most sociable community");
        System.out.println("0. Exit");
    }

    /**
     * Displays all users in the social network.
     * Uses Stream API to format user information.
     */
    private void displayUsers() {
        System.out.println("Users:");

        List<String> users = StreamSupport.stream(userService.getAllUsers().spliterator(), false).map(user -> user.getId() + " - " + user).collect(Collectors.toList());

        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            users.forEach(System.out::println);
        }
    }

    /**
     * Reads user information from the console and adds a new user.
     */
    private void addUser() {
        System.out.print("User ID: ");
        Long id = Long.parseLong(
                scanner.nextLine().trim()
        );

        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();

        if (userService.addUser(firstName, lastName, id)) {
            System.out.println(
                    "User added successfully."
            );
        } else {
            System.out.println(
                    "A user with this ID already exists."
            );
        }
    }

    /**
     * Removes a user and all friendships associated with that user.
     *
     * @throws ValidationException if the user does not exist
     */
    private void removeUser() {
        System.out.print("User ID to remove: ");

        Long id = Long.parseLong(
                scanner.nextLine().trim()
        );

        userService.findUserById(id).orElseThrow(() ->
                new ValidationException(
                        "User with ID " + id + " does not exist."
                )
        );

        friendshipService.removeFriendshipsOfUser(id);
        userService.deleteUser(id);

        System.out.println(
                "User removed successfully."
        );
    }

    /**
     * Displays all friendships in the social network.
     * Uses Stream API to format friendship information.
     */
    private void displayFriendships() {
        System.out.println("Friendships:");

        List<String> friendships = StreamSupport.stream(
                        friendshipService.getAllFriendships().spliterator(),
                        false
                )
                .map(friendship ->
                        friendship.getId().getT1()
                                + " - "
                                + friendship.getId().getT2()
                                + ": "
                                + friendship
                )
                .collect(Collectors.toList());

        if (friendships.isEmpty()) {
            System.out.println("No friendships found.");
        } else {
            friendships.forEach(System.out::println);
        }
    }

    /**
     * Reads two user identifiers and creates a friendship between them.
     */
    private void addFriend() {
        System.out.print("First user ID: ");

        Long firstUserId = Long.parseLong(
                scanner.nextLine().trim()
        );

        System.out.print("Second user ID: ");

        Long secondUserId = Long.parseLong(
                scanner.nextLine().trim()
        );

        if (friendshipService.addFriend(
                firstUserId,
                secondUserId)) {

            System.out.println(
                    "Friendship added successfully."
            );

        } else {
            System.out.println(
                    "These users are already friends."
            );
        }
    }

    /**
     * Reads two user identifiers and removes their friendship.
     */
    private void removeFriend() {
        System.out.print("First user ID: ");

        Long firstUserId = Long.parseLong(
                scanner.nextLine().trim()
        );

        System.out.print("Second user ID: ");

        Long secondUserId = Long.parseLong(
                scanner.nextLine().trim()
        );

        if (friendshipService.removeFriend(
                firstUserId,
                secondUserId)) {

            System.out.println(
                    "Friendship removed successfully."
            );

        } else {
            System.out.println(
                    "This friendship does not exist."
            );
        }
    }

    /**
     * Displays the number of connected communities in the social network.
     */
    private void displayNumberOfCommunities() {
        System.out.println(
                "Number of communities: "
                        + communityService.getNumberOfCommunities()
        );
    }

    /**
     * Displays the most sociable community in the social network.
     * Uses Stream API to format and print its members.
     */
    private void displayMostSociableCommunity() {

        List<User> community =
                communityService.getMostSociableCommunity();

        if (community.isEmpty()) {
            System.out.println(
                    "There are no users in the network."
            );
            return;
        }

        System.out.println(
                "Most sociable community:"
        );

        community.stream()
                .map(user -> user.getId() + " - " + user)
                .forEach(System.out::println);
    }
}
