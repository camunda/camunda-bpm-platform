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
package org.camunda.bpm.engine.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * helper/convience methods for working with collections.
 *
 * @author Joram Barrez
 */
public class CollectionUtil {

  // No need to instantiate
  private CollectionUtil() {}

  /**
   * Helper method that creates a singleton map.
   *
   * Alternative for Collections.singletonMap(), since that method returns a
   * generic typed map <K,T> depending on the input type, but we often need a
   * <String, Object> map.
   */
  public static Map<String, Object> singletonMap(String key, Object value) {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put(key, value);
    return map;
  }

  /**
   * Arrays.asList cannot be reliably used for SQL parameters on MyBatis < 3.3.0
   */
  public static <T> List<T> asArrayList(T[] values) {
    ArrayList<T> result = new ArrayList<T>();
    Collections.addAll(result, values);

    return result;
  }

  public static <T> Set<T> asHashSet(T... elements) {
    Set<T> set = new HashSet<T>();
    Collections.addAll(set, elements);

    return set;
  }

  public static <S, T> void addToMapOfLists(Map<S, List<T>> map, S key, T value) {
    List<T> list = map.get(key);
    if (list == null) {
      list = new ArrayList<T>();
      map.put(key, list);
    }
    list.add(value);
  }

  public static <S, T> void addToMapOfSets(Map<S, Set<T>> map, S key, T value) {
    Set<T> set = map.get(key);
    if (set == null) {
      set = new HashSet<T>();
      map.put(key, set);
    }
    set.add(value);
  }

  public static <S, T> void addCollectionToMapOfSets(Map<S, Set<T>> map, S key, Collection<T> values) {
    Set<T> set = map.get(key);
    if (set == null) {
      set = new HashSet<T>();
      map.put(key, set);
    }
    set.addAll(values);
  }

  /**
   * Chops a list into non-view sublists of length partitionSize.
   */
  public static <T> List<List<T>> partition(List<T> list, final int partitionSize) {
    List<List<T>> parts = new ArrayList<List<T>>();
    final int listSize = list.size();
    for (int i = 0; i < listSize; i += partitionSize) {
      parts.add(new ArrayList<T>(list.subList(i, Math.min(listSize, i + partitionSize))));
    }
    return parts;
  }
}
