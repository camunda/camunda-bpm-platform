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
package org.camunda.bpm.engine.impl.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;

/**
 * Properties that maps property keys to values. The properties cannot contain
 * duplicate property names; each property name can map to at most one value.
 *
 * @author Philipp Ossler
 *
 */
public class Properties {

  protected final Map<String, Object> properties;

  public Properties() {
    this(new HashMap<String, Object>());
  }

  public Properties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /**
   * Returns the value to which the specified property key is mapped, or
   * <code>null</code> if this properties contains no mapping for the property key.
   *
   * @param property
   *          the property key whose associated value is to be returned
   * @return the value to which the specified property key is mapped, or
   *         <code>null</code> if this properties contains no mapping for the property key
   */
  @SuppressWarnings("unchecked")
  public <T> T get(PropertyKey<T> property) {
    return (T) properties.get(property.getName());
  }

  /**
   * Returns the list to which the specified property key is mapped, or
   * an empty list if this properties contains no mapping for the property key.
   * Note that the empty list is not mapped to the property key.
   *
   * @param property
   *          the property key whose associated list is to be returned
   * @return the list to which the specified property key is mapped, or
   *         an empty list if this properties contains no mapping for the property key
   *
   * @see #addListItem(PropertyListKey, Object)
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> get(PropertyListKey<T> property) {
    if (contains(property)) {
      return (List<T>) properties.get(property.getName());
    } else {
      return new ArrayList<T>();
    }
  }

  /**
   * Returns the map to which the specified property key is mapped, or
   * an empty map if this properties contains no mapping for the property key.
   * Note that the empty map is not mapped to the property key.
   *
   * @param property
   *          the property key whose associated map is to be returned
   * @return the map to which the specified property key is mapped, or
   *         an empty map if this properties contains no mapping for the property key
   *
   * @see #putMapEntry(PropertyMapKey, Object, Object)
   */
  @SuppressWarnings("unchecked")
  public <K, V> Map<K, V> get(PropertyMapKey<K, V> property) {
    if (contains(property)) {
      return (Map<K, V>) properties.get(property.getName());
    } else {
      return new HashMap<K, V>();
    }
  }

  /**
   * Associates the specified value with the specified property key. If the properties previously contained a mapping for the property key, the old
   * value is replaced by the specified value.
   *
   * @param <T>
   *          the type of the value
   * @param property
   *          the property key with which the specified value is to be associated
   * @param value
   *          the value to be associated with the specified property key
   */
  public <T> void set(PropertyKey<T> property, T value) {
    properties.put(property.getName(), value);
  }

  /**
   * Associates the specified list with the specified property key. If the properties previously contained a mapping for the property key, the old
   * value is replaced by the specified list.
   *
   * @param <T>
   *          the type of elements in the list
   * @param property
   *          the property key with which the specified list is to be associated
   * @param value
   *          the list to be associated with the specified property key
   */
  public <T> void set(PropertyListKey<T> property, List<T> value) {
    properties.put(property.getName(), value);
  }

  /**
   * Associates the specified map with the specified property key. If the properties previously contained a mapping for the property key, the old
   * value is replaced by the specified map.
   *
   * @param <K>
   *          the type of keys maintained by the map
   * @param <V>
   *          the type of mapped values
   * @param property
   *          the property key with which the specified map is to be associated
   * @param value
   *          the map to be associated with the specified property key
   */
  public <K, V> void set(PropertyMapKey<K, V> property, Map<K, V> value) {
    properties.put(property.getName(), value);
  }

  /**
   * Append the value to the list to which the specified property key is mapped. If
   * this properties contains no mapping for the property key, the value append to
   * a new list witch is associate the the specified property key.
   *
   * @param <T>
   *          the type of elements in the list
   * @param property
   *          the property key whose associated list is to be added
   * @param value
   *          the value to be appended to list
   */
  public <T> void addListItem(PropertyListKey<T> property, T value) {
    List<T> list = get(property);
    list.add(value);

    if (!contains(property)) {
      set(property, list);
    }
  }

  /**
   * Insert the value to the map to which the specified property key is mapped. If
   * this properties contains no mapping for the property key, the value insert to
   * a new map witch is associate the the specified property key.
   *
   * @param <K>
   *          the type of keys maintained by the map
   * @param <V>
   *          the type of mapped values
   * @param property
   *          the property key whose associated list is to be added
   * @param value
   *          the value to be appended to list
   */
  public <K, V> void putMapEntry(PropertyMapKey<K, V> property, K key, V value) {
    Map<K, V> map = get(property);

    if (!property.allowsOverwrite() && map.containsKey(key)) {
      throw new ProcessEngineException("Cannot overwrite property key " + key + ". Key already exists");
    }

    map.put(key, value);

    if (!contains(property)) {
      set(property, map);
    }
  }

  /**
   * Returns <code>true</code> if this properties contains a mapping for the specified property key.
   *
   * @param property
   *            the property key whose presence is to be tested
   * @return <code>true</code> if this properties contains a mapping for the specified property key
   */
  public boolean contains(PropertyKey<?> property) {
    return properties.containsKey(property.getName());
  }

  /**
   * Returns <code>true</code> if this properties contains a mapping for the specified property key.
   *
   * @param property
   *            the property key whose presence is to be tested
   * @return <code>true</code> if this properties contains a mapping for the specified property key
   */
  public boolean contains(PropertyListKey<?> property) {
    return properties.containsKey(property.getName());
  }

  /**
   * Returns <code>true</code> if this properties contains a mapping for the specified property key.
   *
   * @param property
   *            the property key whose presence is to be tested
   * @return <code>true</code> if this properties contains a mapping for the specified property key
   */
  public boolean contains(PropertyMapKey<?, ?> property) {
    return properties.containsKey(property.getName());
  }

  /**
   * Returns a map view of this properties. Changes to the map are not reflected
   * to the properties.
   *
   * @return a map view of this properties
   */
  public Map<String, Object> toMap() {
    return new HashMap<String, Object>(properties);
  }

  @Override
  public String toString() {
    return "Properties [properties=" + properties + "]";
  }

}
