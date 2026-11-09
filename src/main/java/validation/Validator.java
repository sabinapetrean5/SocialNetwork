
package validation;

/**
 * Defines a validation strategy for objects of a specified type.
 *
 * @param <T> the type of object to validate
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Combines this validator with another validator.
     * The current validator is executed first, followed by the
     * specified validator.
     *
     * @param other the validator to execute after this validator
     * @return a combined validator that executes both validation strategies
     * @throws ValidationException when either validator rejects
     *         an entity during validation
     */
    default Validator<T> andThen(Validator<T> other) {
        return entity -> {
            this.validate(entity);
            other.validate(entity);
        };
    }

    /**
     * Validates the specified object according to the implemented rules.
     *
     * @param entity the object to validate
     * @throws ValidationException if the object does not satisfy
     *         the validation rules
     */
    void validate(T entity);
}
