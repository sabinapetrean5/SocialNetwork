
package repository;

import domain.Entity;
import validation.ValidationException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Defines the contract for repositories that manage entities.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds an entity by identifier.
     *
     * @param id the identifier to search for
     * @return the matching entity or {@code null} if it does not exist
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    E findOne(ID id);

    /**
     * Returns all stored entities.
     *
     * @return all stored entities
     */
    Iterable<E> findAll();

    /**
     * Finds all entities matching the specified search criterion.
     * The criterion is applied using the Stream API.
     *
     * @param searchCriteria the predicate used to filter entities
     * @return a list containing all entities that satisfy the predicate
     * @throws IllegalArgumentException if {@code searchCriteria} is {@code null}
     */
    default List<E> findAll(Predicate<E> searchCriteria) {
        if (searchCriteria == null) {
            throw new IllegalArgumentException("searchCriteria is null");
        }

        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(searchCriteria)
                .collect(Collectors.toList());
    }

    /**
     * Saves a new entity.
     *
     * @param entity the entity to save
     * @return {@code null} on success, or the supplied entity if its ID already exists
     * @throws IllegalArgumentException if {@code entity} or its ID is {@code null}
     * @throws ValidationException if the entity is invalid
     */
    E save(E entity);

    /**
     * Updates an existing entity.
     *
     * @param entity the entity containing the new data
     * @return {@code null} on success, or the supplied entity if its ID does not exist
     * @throws IllegalArgumentException if {@code entity} or its ID is {@code null}
     * @throws ValidationException if the entity is invalid
     */
    E update(E entity);

    /**
     * Deletes an entity by identifier.
     *
     * @param id the identifier to delete
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    void delete(ID id);
}
