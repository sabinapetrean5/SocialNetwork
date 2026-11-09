
package service;

import domain.Friendship;
import domain.User;
import repository.Repository;
import utils.Tuple;
import validation.FriendshipValidator;
import validation.ValidationException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Provides operations for managing friendships between users.
 */
public class FriendshipService {

    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;
    private final UserService userService;
    private final FriendshipValidator friendshipValidator;

    /**
     * Creates a friendship service with the required dependencies.
     *
     * @param friendshipRepository the repository used to manage friendships
     * @param userService the service used to access users
     * @param friendshipValidator the validator used to validate friendships
     */
    public FriendshipService(
            Repository<Tuple<Long, Long>, Friendship> friendshipRepository,
            UserService userService,
            FriendshipValidator friendshipValidator) {

        this.friendshipRepository = friendshipRepository;
        this.userService = userService;
        this.friendshipValidator = friendshipValidator;
    }

    /**
     * Retrieves all friendships.
     *
     * @return an iterable containing all friendships
     */
    public Iterable<Friendship> getAllFriendships() {
        return friendshipRepository.findAll();
    }

    /**
     * Creates a friendship between two existing users.
     * The identifiers are normalized to prevent the same friendship
     * from being stored in both orders.
     *
     * @param firstUserId the identifier of the first user
     * @param secondUserId the identifier of the second user
     * @return true if the friendship was added successfully;
     *         false if the friendship already exists
     * @throws ValidationException if an identifier is null,
     *         the identifiers are equal, one of the users does not exist,
     *         or the friendship is invalid
     */
    public boolean addFriend(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null) {
            throw new ValidationException("User IDs cannot be null.");
        }

        if (firstUserId.equals(secondUserId)) {
            throw new ValidationException(
                    "A user cannot be friends with themselves."
            );
        }

        User firstUser = userService.getUserById(firstUserId);
        User secondUser = userService.getUserById(secondUserId);

        if (firstUser == null || secondUser == null) {
            throw new ValidationException("Both users must exist.");
        }

        Long smallerId = Math.min(firstUserId, secondUserId);
        Long largerId = Math.max(firstUserId, secondUserId);

        Tuple<Long, Long> friendshipId =
                new Tuple<>(smallerId, largerId);

        Friendship friendship = new Friendship(
                friendshipId,
                userService.getUserById(smallerId),
                userService.getUserById(largerId)
        );

        friendshipValidator.validate(friendship);

        return friendshipRepository.save(friendship) == null;
    }

    /**
     * Removes the friendship between two users.
     *
     * @param firstUserId the identifier of the first user
     * @param secondUserId the identifier of the second user
     * @return true if the friendship was removed;
     *         false if the friendship does not exist
     * @throws ValidationException if an identifier is null
     *         or both identifiers are equal
     */
    public boolean removeFriend(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null) {
            throw new ValidationException("User IDs cannot be null.");
        }

        if (firstUserId.equals(secondUserId)) {
            throw new ValidationException(
                    "A user cannot be friends with themselves."
            );
        }

        Tuple<Long, Long> friendshipId = new Tuple<>(
                Math.min(firstUserId, secondUserId),
                Math.max(firstUserId, secondUserId)
        );

        if (friendshipRepository.findOne(friendshipId) == null) {
            return false;
        }

        friendshipRepository.delete(friendshipId);

        return true;
    }

    /**
     * Removes all friendships involving a specified user.
     * The friendship identifiers are collected using the Stream API
     * before the corresponding repository entries are deleted.
     *
     * @param userId the identifier of the user whose friendships
     *               must be removed
     */
    public void removeFriendshipsOfUser(Long userId) {

        List<Tuple<Long, Long>> friendshipsToDelete =
                StreamSupport.stream(
                                friendshipRepository.findAll().spliterator(),
                                false
                        )
                        .filter(friendship ->
                                friendship.getFirstUser().getId().equals(userId)
                                        || friendship.getSecondUser().getId().equals(userId)
                        )
                        .map(Friendship::getId)
                        .collect(Collectors.toList());

        friendshipsToDelete.forEach(friendshipRepository::delete);
    }
}
