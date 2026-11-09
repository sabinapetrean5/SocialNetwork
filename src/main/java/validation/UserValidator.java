
package validation;

import domain.User;

/**
 * Validates users according to the social network's user validation rules.
 * A valid user must have a positive identifier, a non-empty first name,
 * and a non-empty last name. Both names must start with an uppercase letter.
 */
public class UserValidator implements Validator<User> {

    /**
     * Validates a user and collects all detected validation errors.
     *
     * @param entity the user to validate
     * @throws ValidationException if the user is null or fails one
     *         or more validation rules
     */
    @Override
    public void validate(User entity) {
        StringBuilder errors = new StringBuilder();

        if (entity == null) {
            throw new ValidationException("User cannot be null.");
        }

        if (entity.getId() == null || entity.getId() <= 0) {
            errors.append("User ID must be positive. ");
        }

        if (entity.getFirstName() == null
                || entity.getFirstName().isEmpty()
                || !Character.isUpperCase(entity.getFirstName().charAt(0))) {
            errors.append("First name must be non-empty and start with an uppercase letter. ");
        }

        if (entity.getLastName() == null
                || entity.getLastName().isEmpty()
                || !Character.isUpperCase(entity.getLastName().charAt(0))) {
            errors.append("Last name must be non-empty and start with an uppercase letter. ");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }
}
