package dk.sdu.mmmi.cbse.common;

import java.util.Objects;

/**
 * A generic container for two related objects.
 *
 * @param <A> Type of the first element
 * @param <B> Type of the second element
 */
public final class Pair<A, B> {
	private final A first;
	private final B second;

	/**
	 * Creates a new pair of objects.
	 *
	 * @param first First element
	 * @param second Second element
	 */
	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Gets the first element.
	 *
	 * @return First element
	 */
	public A getFirst() {
		return first;
	}

	/**
	 * Gets the second element.
	 *
	 * @return Second element
	 */
	public B getSecond() {
		return second;
	}

	/**
	 * Factory method for creating pairs.
	 *
	 * @param <A> Type of first element
	 * @param <B> Type of second element
	 * @param first First element
	 * @param second Second element
	 * @return A new pair
	 */
	public static <A, B> Pair<A, B> of(A first, B second) {
		return new Pair<>(first, second);
	}

	/**
	 * Creates an ordered pair where the items are sorted by their hashCode.
	 * Useful when the pair should be treated as an unordered pair
	 * (e.g. for collision detection where A,B is equivalent to B,A).
	 *
	 * @param <T> The common type of both objects
	 * @param a The first object
	 * @param b The second object
	 * @return A pair with the objects ordered by their hashCode
	 */
	public static <T> Pair<T, T> ordered(T a, T b) {
		return System.identityHashCode(a) <= System.identityHashCode(b) ?
			new Pair<>(a, b) :
			new Pair<>(b, a);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "Pair{" + first + ", " + second + '}';
	}
}