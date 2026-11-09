package validation;

import domain.Friendship;

/**
 * Validates friendships between users in the social network.
 * A valid friendship must reference two existing users with valid IDs,
 * must not connect a user to themselves, and must have an identifier
 * matching the IDs of the two users.
 */
public class FriendshipValidator implements Validator<Friendship> {

    /**
     * Validates a friendship and checks the consistency of its data.
     *
     * @param friendship the friendship to validate
     * @throws ValidationException if the friendship is null, its identifier
     *         is invalid, either user is missing, both users are identical,
     *         or the identifier does not match the users
     */
    @Override
    public void validate(Friendship friendship) {
        if (friendship == null) {
            throw new ValidationException(
                    "Friendship cannot be null."
            );
        }

        if (friendship.getId() == null
                || friendship.getId().getT1() == null
                || friendship.getId().getT2() == null) {
            throw new ValidationException(
                    "Friendship must have two user IDs."
            );
        }

        if (friendship.getFirstUser() == null
                || friendship.getSecondUser() == null) {
            throw new ValidationException(
                    "Both users must exist."
            );
        }

        if (friendship.getFirstUser().getId() == null
                || friendship.getSecondUser().getId() == null) {
            throw new ValidationException(
                    "Both users must have valid IDs."
            );
        }

        if (friendship.getFirstUser().getId().equals(
                friendship.getSecondUser().getId())) {
            throw new ValidationException(
                    "A user cannot be friends with themselves."
            );
        }

        if (!friendship.getId().getT1().equals(
                friendship.getFirstUser().getId())
                || !friendship.getId().getT2().equals(
                friendship.getSecondUser().getId())) {
            throw new ValidationException(
                    "Friendship ID does not match its users."
            );
        }
    }
}