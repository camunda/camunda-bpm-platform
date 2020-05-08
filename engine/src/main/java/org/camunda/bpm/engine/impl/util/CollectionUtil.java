/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Iterator;
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
    Map<String, Object> map = new HashMap<>();
    map.put(key, value);
    return map;
  }

  /**
   * Arrays.asList cannot be reliably used for SQL parameters on MyBatis < 3.3.0
   */
  public static <T> List<T> asArrayList(T[] values) {
    ArrayList<T> result = new ArrayList<>();
    Collections.addAll(result, values);

    return result;
  }

  public static <T> Set<T> asHashSet(T... elements) {
    Set<T> set = new HashSet<>();
    Collections.addAll(set, elements);

    return set;
  }

  public static <S, T> void addToMapOfLists(Map<S, List<T>> map, S key, T value) {
    List<T> list = map.get(key);
    if (list == null) {
      list = new ArrayList<>();
      map.put(key, list);
    }
    list.add(value);
  }

  public static <S, T> void addToMapOfSets(Map<S, Set<T>> map, S key, T value) {
    Set<T> set = map.get(key);
    if (set == null) {
      set = new HashSet<>();
      map.put(key, set);
    }
    set.add(value);
  }

  public static <S, T> void addCollectionToMapOfSets(Map<S, Set<T>> map, S key, Collection<T> values) {
    Set<T> set = map.get(key);
    if (set == null) {
      set = new HashSet<>();
      map.put(key, set);
    }
    set.addAll(values);
  }

  /**
   * Chops a list into non-view sublists of length partitionSize. Note: the argument list
   * may be included in the result.
   */
  public static <T> List<List<T>> partition(List<T> list, final int partitionSize) {
    List<List<T>> parts = new ArrayList<>();

    final int listSize = list.size();

    if (listSize <= partitionSize) {
      // no need for partitioning
      parts.add(list);
    } else {
      for (int i = 0; i < listSize; i += partitionSize) {
        parts.add(new ArrayList<>(list.subList(i, Math.min(listSize, i + partitionSize))));
      }
    }

    return parts;
  }

  public static <T> List<T> collectInList(Iterator<T> iterator) {
    List<T> result = new ArrayList<>();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    return result;
  }

  public static boolean isEmpty(Collection<?> collection) {
    return collection == null || collection.isEmpty();
  }
}
