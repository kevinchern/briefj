package briefj.collections;


import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;



/**
 * A map from objects to doubles. Includes convenience methods for getting,
 * setting, and incrementing element counts. Objects not in the counter will
 * return a count of zero. The counter is backed by a HashMap (unless specified
 * otherwise with the MapFactory constructor).
 * 
 * @author Dan Klein
 */
public class Counter<E> implements Serializable, Iterable<E> {
	private static final long serialVersionUID = 5724671156522771655L;

	public Map<E, Double> entries;
	


  int currentModCount = 0;
	int cacheModCount = -1;
	double cacheTotalCount = 0.0;

	/**
	 * The elements in the counter.
	 * 
	 * @return set of keys
	 */
	public Set<E> keySet() {
		return entries.keySet();
	}
	
	public void clear(){
		currentModCount = 0;
		cacheModCount = -1;
		cacheTotalCount = 0.0;
		entries.clear();
	}

	/**
	 * The number of entries in the counter (not the total count -- use
	 * totalCount() instead).
	 */
	public int size() {
		return entries.size();
	}

	/**
	 * True if there are no entries in the counter (false does not mean
	 * totalCount greater than 0)
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Returns whether the counter contains the given key. Note that this is the
	 * way to distinguish keys which are in the counter with count zero, and
	 * those which are not in the counter (and will therefore return count zero
	 * from getCount().
	 * 
	 * @param key
	 * @return whether the counter contains the key
	 */
	public boolean containsKey(E key) {
		return entries.containsKey(key);
	}

	/**
	 * Remove a key from the counter. Returns the count associated with that key
	 * or zero if the key wasn't in the counter to begin with
	 * 
	 * @param key
	 * @return the count associated with the key
	 */
	public double removeKey(E key) {
		Double d = entries.remove(key);
		return (d == null ? 0.0 : d);
	}

	/**
	 * Get the count of the element, or zero if the element is not in the
	 * counter.
	 * 
	 * @param key
	 * @return
	 */
	public double getCount(E key) {
		Double value = entries.get(key);
		if (value == null)
			return 0;
		return value;
	}

	/**
	 * Set the count for the given key, clobbering any previous count.
	 * 
	 * @param key
	 * @param count
	 */
	public void setCount(E key, double count) {
		currentModCount++;
		entries.put(key, count);
	}

	/**
	 * Increment a key's count by the given amount.
	 * 
	 * @param key
	 * @param increment
	 */
	public void incrementCount(E key, double increment) {
		setCount(key, getCount(key) + increment);
	}

	/**
	 * Increment each element in a given collection by a given amount.
	 */
	public void incrementAll(Collection<? extends E> collection, double count) {
		for (E key : collection) {
			incrementCount(key, count);
		}
	}

	public <T extends E> void incrementAll(Counter<T> counter) {
		for (T key : counter.keySet()) {
			double count = counter.getCount(key);
			incrementCount(key, count);
		}
	}

	/**
	 * Finds the total of all counts in the counter. This implementation uses
	 * cached count which may get out of sync if the entries map is modified in
	 * some unantipicated way.
	 * 
	 * @return the counter's total
	 */
	public double totalCount() {
		if (currentModCount != cacheModCount) {
			double total = 0.0;
			for (Map.Entry<E, Double> entry : entries.entrySet()) {
				total += entry.getValue();
			}
			cacheTotalCount = total;
			cacheModCount = currentModCount;
		}
		return cacheTotalCount;
	}

	/**
	 * Destructively normalize this Counter in place.
	 */
	public void normalize() {
		double totalCount = totalCount();
		for (E key : keySet()) {
			setCount(key, getCount(key) / totalCount);
		}
	}

	/**
	 * Finds the key with maximum count. This is a linear operation, and ties
	 * are broken arbitrarily.
	 * 
	 * @return a key with maximum count
	 */
	public E argMax() {
		double maxCount = Double.NEGATIVE_INFINITY;
		E maxKey = null;
		for (Map.Entry<E, Double> entry : entries.entrySet()) {
			if (entry.getValue() > maxCount || maxKey == null) {
				maxKey = entry.getKey();
				maxCount = entry.getValue();
			}
		}
		return maxKey;
	}

	public double max() {
		double maxCount = Double.NEGATIVE_INFINITY;
		E maxKey = null;
		for (Map.Entry<E, Double> entry : entries.entrySet()) {
			if (entry.getValue() > maxCount || maxKey == null) {
				maxKey = entry.getKey();
				maxCount = entry.getValue();
			}
		}
		return maxCount;
	}
	
	 /**
   * Finds the key with min count. This is a linear operation, and ties
   * are broken arbitrarily.
   * 
   * @return a key with min count
   */
  public E argMin() {
    double minCount = Double.POSITIVE_INFINITY;
    E minKey = null;
    for (Map.Entry<E, Double> entry : entries.entrySet()) {
      if (entry.getValue() < minCount || minKey == null) {
        minKey = entry.getKey();
        minCount = entry.getValue();
      }
    }
    return minKey;
  }

  public double min() {
    double minCount = Double.POSITIVE_INFINITY;
    E minKey = null;
    for (Map.Entry<E, Double> entry : entries.entrySet()) {
      if (entry.getValue() < minCount || minKey == null) {
        minKey = entry.getKey();
        minCount = entry.getValue();
      }
    }
    return minCount;
  }

	/**
	 * Returns a string representation with the keys ordered by decreasing
	 * counts.
	 * 
	 * @return string representation
	 */
	public String toString() {
		return toString(keySet().size());
	}

	/**
	 * Returns a string representation which includes no more than the
	 * maxKeysToPrint elements with largest counts.
	 * 
	 * @param maxKeysToPrint
	 * @return partial string representation
	 */
	public String toString(int maxKeysToPrint) {
		return asPriorityQueue().toString(maxKeysToPrint);
	}

	/**
	 * Builds a priority queue whose elements are the counter's elements, and
	 * whose priorities are those elements' counts in the counter.
	 */
	public PriorityQueue<E> asPriorityQueue() {
		PriorityQueue<E> pq = new PriorityQueue<E>(entries.size());
		for (Map.Entry<E, Double> entry : entries.entrySet()) {
			pq.add(entry.getKey(), entry.getValue());
		}
		return pq;
	}

	public Iterator<E> iterator() {
		return asPriorityQueue();
	}

	public Counter() {
		this(new MapFactory.LinkedHashMapFactory<E, Double>());
	}

	public Counter(MapFactory<E, Double> mf) {
		entries = mf.buildMap();
	}

	public Counter(Counter<? extends E> counter) {
		this();
		incrementAll(counter);
	}

	public Counter(Collection<? extends E> collection) {
		this();
		incrementAll(collection, 1.0);
	}

}
