package exceptions;

/**
 * Unchecked exception used for persistence and repository configuration errors.
 */
public class RepositoryException extends RuntimeException {
    /**
     * Creates a repository exception with a message.
     *
     * @param message the error message
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a repository exception and preserves the original cause.
     *
     * @param message the error message
     * @param cause the original exception
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
