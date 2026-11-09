
package repository.file;

import domain.Entity;
import exceptions.RepositoryException;
import repository.memory.InMemoryRepository;
import validation.ValidationException;
import validation.Validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Generic text-file repository. The common file algorithm is implemented here,
 * while subclasses provide only line-to-entity and entity-to-line conversion.
 * This is the Template Method pattern required by Laboratory 3.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>>
        extends InMemoryRepository<ID, E> {

    private final String fileName;

    /**
     * Creates a file repository. Subclasses must call {@link #loadData()} only
     * after their own fields have been initialized.
     *
     * @param fileName relative path of the text file
     * @param validator validator used for loaded and newly saved entities
     * @throws IllegalArgumentException if the file name is null or empty
     */
    protected AbstractFileRepository(String fileName, Validator<E> validator) {
        super(validator);

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty.");
        }

        this.fileName = fileName;
    }

    /**
     * Converts one text line into an entity.
     *
     * @param line the line read from the file
     * @return the extracted entity
     * @throws IllegalArgumentException if the line has an invalid structure
     * @throws RepositoryException if a field cannot be converted
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into its text-file representation.
     *
     * @param entity the entity to convert
     * @return the line written to the file
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Loads the file into memory. A missing file means an empty repository.
     * Invalid lines are reported with their line number and ignored.
     * Loaded entities are validated through {@code super.save(...)}.
     *
     * @throws RepositoryException if an existing file cannot be read
     */
    protected final void loadData() {
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            return;
        }

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {

            List<String> fileLines = lines.collect(Collectors.toList());

            IntStream.range(0, fileLines.size())
                    .filter(index -> !fileLines.get(index).trim().isEmpty())
                    .forEach(index -> {
                        String line = fileLines.get(index);
                        int lineNumber = index + 1;

                        try {
                            E entity = extractEntity(line);
                            E duplicate = super.save(entity);

                            if (duplicate != null) {
                                System.err.println(
                                        "Line " + lineNumber
                                                + " ignored: duplicate ID "
                                                + entity.getId() + "."
                                );
                            }

                        } catch (ValidationException e) {
                            System.err.println(
                                    "Line " + lineNumber
                                            + " ignored: " + e.getMessage()
                            );

                        } catch (RepositoryException e) {
                            System.err.println(
                                    "Line " + lineNumber
                                            + " ignored: " + e.getMessage()
                            );

                        } catch (IllegalArgumentException e) {
                            System.err.println(
                                    "Line " + lineNumber
                                            + " ignored: " + e.getMessage()
                            );
                        }
                    });

        } catch (IOException e) {
            throw new RepositoryException(
                    "Could not read file " + fileName + ".", e
            );
        }
    }

    /**
     * Writes all entities to the text file using Stream API.
     */
    private void writeAllToFile() {
        Path path = Paths.get(fileName);

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = entities.values().stream()
                    .map(this::createEntityAsString)
                    .collect(Collectors.toList());

            Files.write(path, lines, StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RepositoryException(
                    "Could not write file " + fileName + ".", e
            );
        }
    }

    @Override
    public E save(E entity) {
        E result = super.save(entity);

        if (result == null) {
            writeAllToFile();
        }

        return result;
    }

    @Override
    public E update(E entity) {
        E result = super.update(entity);

        if (result == null) {
            writeAllToFile();
        }

        return result;
    }

    @Override
    public void delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        boolean existed = entities.containsKey(id);

        super.delete(id);

        if (existed) {
            writeAllToFile();
        }
    }
}
