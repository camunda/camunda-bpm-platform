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

/**
 * A Map-like data structure that stores key-value pairs and provides temporary
 * access to it.
 *
 * @param <K> the type of keys
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {

  /**
   * Gets an entry from the cache.
   *
   * @param key the key whose associated value is to be returned
   * @return the element, or <code>null</code>, if it does not exist.
   */
  V get(K key);

  /**
   * Associates the specified value with the specified key in the cache.
   *
   * @param key   key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @throws NullPointerException if key is <code>null</code> or if value is <code>null</code>
   */
  void put(K key, V value);

  /**
   * Clears the contents of the cache.
   */
  void clear();

  /**
   * Removes an entry from the cache.
   *
   * @param key key with which the specified value is to be associated.
   */
  void remove(K key);

  /**
   * Returns a Set view of the keys contained in this cache.
   */
  public Set<K> keySet();

  /**
   * @return the current size of the cache
   */
  public int size();

  /**
   * Returns <code>true</code> if this cache contains no key-value mappings.
   */
  public boolean isEmpty();

}