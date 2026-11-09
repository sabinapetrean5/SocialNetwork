package utils;

import java.util.Objects;

/**
 * Generic pair of values.
 *
 * @param <T1> type of the first value
 * @param <T2> type of the second value
 */
public class Tuple<T1, T2> {
    private final T1 t1;
    private final T2 t2;

    /**
     * Creates a tuple.
     *
     * @param t1 the first value
     * @param t2 the second value
     */
    public Tuple(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    /** @return the first value */
    public T1 getT1() { return t1; }

    /** @return the second value */
    public T2 getT2() { return t2; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
        return Objects.equals(t1, tuple.t1) && Objects.equals(t2, tuple.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    @Override
    public String toString() {
        return "(" + t1 + ", " + t2 + ")";
    }
}
