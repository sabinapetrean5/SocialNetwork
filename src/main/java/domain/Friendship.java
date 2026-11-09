package domain;

import utils.Tuple;

import java.time.LocalDateTime;

/**
 * Represents a friendship between two users.
 * The friendship is identified by the normalized pair of user IDs and stores
 * the date and time when the friendship was created.
 */
public class Friendship extends Entity<Tuple<Long, Long>> {
    private User firstUser;
    private User secondUser;
    private LocalDateTime friendshipDate;

    /**
     * Creates a new friendship using the current date and time.
     *
     * @param id the normalized pair of user identifiers
     * @param firstUser the first user
     * @param secondUser the second user
     */
    public Friendship(Tuple<Long, Long> id, User firstUser, User secondUser) {
        this(id, firstUser, secondUser, LocalDateTime.now());
    }

    /**
     * Creates a friendship with an explicitly supplied creation date.
     * This constructor is used when friendships are loaded from a text file.
     *
     * @param id the normalized pair of user identifiers
     * @param firstUser the first user
     * @param secondUser the second user
     * @param friendshipDate the friendship creation date
     */
    public Friendship(Tuple<Long, Long> id, User firstUser, User secondUser,
                      LocalDateTime friendshipDate) {
        super(id);
        this.firstUser = firstUser;
        this.secondUser = secondUser;
        this.friendshipDate = friendshipDate;
    }

    /** @return the first user */
    public User getFirstUser() { return firstUser; }

    /** @return the second user */
    public User getSecondUser() { return secondUser; }

    /** @return the friendship creation date */
    public LocalDateTime getFriendshipDate() { return friendshipDate; }

    /** @param firstUser the new first user */
    public void setFirstUser(User firstUser) { this.firstUser = firstUser; }

    /** @param secondUser the new second user */
    public void setSecondUser(User secondUser) { this.secondUser = secondUser; }

    /** @param friendshipDate the new friendship creation date */
    public void setFriendshipDate(LocalDateTime friendshipDate) { this.friendshipDate = friendshipDate; }

    @Override
    public String toString() {
        return firstUser + " - " + secondUser + " (" + friendshipDate + ")";
    }
}
