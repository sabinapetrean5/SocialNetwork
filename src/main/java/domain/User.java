package domain;

/**
 * Represents a user in the social network.
 * Each user has an identifier, a first name, and a last name.
 */
public class User extends Entity<Long>{
    private String firstName;
    private String lastName;

    /**
     * Creates a user with the specified name and identifier.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param id the user's identifier
     */
    public User (String firstName, String lastName, Long id) {
        super(id);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Returns the user's first name.
     *
     * @return the user's first name
     */
    public String getFirstName(){
        return firstName;
    }

    /**
     * Returns the user's last name.
     *
     * @return the user's last name
     */
    public String getLastName(){
        return lastName;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName the new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the user's full name.
     *
     * @return the first name followed by the last name
     */
    @Override
    public String toString(){
        return firstName + " " + lastName;
    }
}
