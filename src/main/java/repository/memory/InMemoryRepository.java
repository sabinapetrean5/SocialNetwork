
package repository.memory;

import domain.Entity;
import repository.Repository;
import validation.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic repository that stores validated entities in memory.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>>
        implements Repository<ID, E> {

    protected final Map<ID, E> entities;
    protected final Validator<E> validator;

    /**
     * Creates an in-memory repository.
     *
     * @param validator validator applied before save and update
     * @throws IllegalArgumentException if the validator is null
     */
    public InMemoryRepository(Validator<E> validator) {
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null.");
        }

        this.validator = validator;
        this.entities = new LinkedHashMap<ID, E>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        return entities.get(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<E> findAll() {
        return new ArrayList<E>(entities.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E save(E entity) {
        checkEntity(entity);
        validator.validate(entity);

        if (entities.containsKey(entity.getId())) {
            return entity;
        }

        entities.put(entity.getId(), entity);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E update(E entity) {
        checkEntity(entity);
        validator.validate(entity);

        if (!entities.containsKey(entity.getId())) {
            return entity;
        }

        entities.put(entity.getId(), entity);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null.");
        }

        entities.remove(id);
    }

    /**
     * Checks whether an entity and its identifier are non-null.
     *
     * @param entity the entity to check
     * @throws IllegalArgumentException if the entity or its identifier is null
     */
    private void checkEntity(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null.");
        }

        if (entity.getId() == null) {
            throw new IllegalArgumentException("Entity ID cannot be null.");
        }
    }
}
