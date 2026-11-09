package domain;

import java.util.Objects;

/**
 * Represents a generic entity identified by an ID.
 *
 * @param <ID> the identifier type
 */
public abstract class Entity<ID> {
    private ID id;

    /**
     * Creates an entity with the specified identifier.
     *
     * @param id the entity identifier
     */
    protected Entity(ID id) {
        this.id = id;
    }

    /**
     * Returns the entity identifier.
     *
     * @return the entity identifier
     */
    public ID getId() {
        return id;
    }

    /**
     * Changes the entity identifier.
     *
     * @param id the new identifier
     */
    public void setId(ID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Entity<?> entity = (Entity<?>) obj;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
