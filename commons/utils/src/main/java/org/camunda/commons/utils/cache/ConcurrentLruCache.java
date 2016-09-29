/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.commons.utils.cache;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe LRU {@link Cache} with a fixed capacity. If the cache reaches
 * the capacity, it discards the least recently used entry first.
 * <p>
 * *Note*: The consistency of the keys queue with the keys in the cache is not ensured! This means, the keys queue
 * can contain duplicates of the same key and not all the keys of the queue are necessarily in the cache.
 * However, all the keys of the cache are at least once contained in the keys queue.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public class ConcurrentLruCache<K, V> implements Cache<K, V> {

  private final int capacity;

  private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<K, V>();
  private final ConcurrentLinkedQueue<K> keys = new ConcurrentLinkedQueue<K>();

  /**
   * Creates the cache with a fixed capacity.
   *
   * @param capacity max number of cache entries
   * @throws IllegalArgumentException if capacity is negative
   */
  public ConcurrentLruCache(int capacity) {
    if (capacity < 0) {
      throw new IllegalArgumentException();
    }
    this.capacity = capacity;
  }

  @Override
  public V get(K key) {
    V value = cache.get(key);
    if (value != null) {
      keys.remove(key);
      keys.add(key);
    }
    return value;
  }

  @Override
  public void put(K key, V value) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }

    V previousValue = cache.put(key, value);
    if (previousValue != null) {
      keys.remove(key);
    }
    keys.add(key);

    if (cache.size() > capacity) {
      K lruKey = keys.poll();
      if (lruKey != null) {
        cache.remove(lruKey);

        // remove duplicated keys
        this.removeAll(lruKey);

        // queue may not contain any key of a possibly concurrently added entry of the same key in the cache
        if (cache.containsKey(lruKey)) {
          keys.add(lruKey);
        }
      }
    }
  }

  @Override
  public void remove(K key) {
    this.cache.remove(key);
    keys.remove(key);
  }

  @Override
  public void clear() {
    cache.clear();
    keys.clear();
  }

  @Override
  public boolean isEmpty() {
    return cache.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return cache.keySet();
  }

  @Override
  public int size() {
    return cache.size();
  }

  /**
   * Removes all instances of the given key within the keys queue.
   */
  protected void removeAll(K key) {
    while (keys.remove(key)) {
    }
  }

}
