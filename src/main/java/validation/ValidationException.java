package validation;

/**
 * Represents an exception thrown when an object fails validation.
 */
public class ValidationException extends RuntimeException{

    /**
     * Creates a validation exception with the specified error message.
     *
     * @param message the description of the validation error
     */
    public ValidationException(String message){
        super(message);
    }
}
