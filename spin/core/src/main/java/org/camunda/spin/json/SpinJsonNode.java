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
package org.camunda.spin.json;

import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.spi.SpinDataFormatException;

import java.util.List;
import java.util.Map;

/**
 * A json node.
 *
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public abstract class SpinJsonNode extends Spin<SpinJsonNode> {

  /**
   * Fetches the first index of the searched object in an array.
   *
   * @param searchObject Object for which the index should be searched.
   * @return {@link Integer} index of searchObject.
   * @throws SpinJsonException if the current node is not an array.
   * @throws SpinJsonPropertyException if object is not found.
   */
  public abstract Integer indexOf(Object searchObject);

  /**
   * Fetches the last index of the searched object in an array.
   *
   * @param searchObject Object for which the index should be searched.
   * @return {@link Integer} index of searchObject or -1 if object not found.
   * @throws SpinJsonException if the current node is not an array.
   */
  public abstract Integer lastIndexOf(Object searchObject);

  /**
   * Check if this node is an object node.
   *
   * @return true if the node is an object, false otherwise
   */
  public abstract boolean isObject();

  /**
   * Check if this node has a property with the given name.
   *
   * @param name the name of the property
   * @return true if this node has a property with this name, false otherwise
   */
  public abstract boolean hasProp(String name);

  /**
   * Get the property of this node with the given name.
   *
   * @param name the name of the property
   * @return {@link SpinJsonNode} representation of the property
   */
  public abstract SpinJsonNode prop(String name);

  /**
   * Set a new String property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new String property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, String newProperty);

  /**
   * Set a new Number property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new Number property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, Number newProperty);

  /**
   * Set a new int property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new int property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, int newProperty);

  /**
   * Set a new float property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new float property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, float newProperty);

  /**
   * Set a new long property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new long property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, long newProperty);

  /**
   * Set a new boolean property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new boolean property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, boolean newProperty);

  /**
   * Set a new Boolean property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new Boolean property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, Boolean newProperty);

  /**
   * Set a new List property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new List property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, List<Object> newProperty);

  /**
   * Set a new Map property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new Map property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, Map<String, Object> newProperty);

  /**
   * Set a new SpinJsonNode Object property in this node.
   *
   * @param name the name of the new property
   * @param newProperty the new SpinJsonNode Object property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode prop(String name, SpinJsonNode newProperty);

  /**
   * Remove a property of the given node by name.
   * @param name name of the property
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode deleteProp(String name);

  /**
   * Removes a number of properties by a given list of names.
   * @param names list of names
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode deleteProp(List<String> names);

  /**
   * Appends a object to the end of the current array node
   * @param property property which should be append
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode append(Object property);

  /**
   * Appends an object at a specific point in an array
   *
   * @param index Index in current node where the new property should be appended.
   * @param property Object which should be appended.
   * @return {@link SpinJsonNode} representation of the current node.
   * @throws IllegalArgumentException if index is out of bound.
   */
  public abstract SpinJsonNode insertAt(int index, Object property);

  /**
   * Inserts an object BEFORE an specific object in an array
   *
   * @param searchObject Object which is searched
   * @param insertObject Object which will be inserted
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode insertBefore(Object searchObject, Object insertObject);

  /**
   * Inserts an object AFTER an specific object in an array
   *
   * @param searchObject Object which is searched
   * @param insertObject Object which will be inserted
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode insertAfter(Object searchObject, Object insertObject);

  /**
   * Removes the first appearance of an object from the current array
   *
   * @param property object which should be deleted
   * @return {@link SpinJsonNode} representation of the current node
   */
  public abstract SpinJsonNode remove(Object property);

  /**
   * Removes the last appearance of an object from the current array
   *
   * @param property object which should be deleted
   * @return {@link SpinJsonNode} representation of the current node
   *
   */
  public abstract SpinJsonNode removeLast(Object property);

  /**
   * removes an object at the specific index of the current array
   *
   * @param index Index of the array
   * @return {@link SpinJsonNode} representation of the current node
   * @throws IllegalArgumentException if index is out of bound.
   */
  public abstract SpinJsonNode removeAt(int index);

  /**
   * Check if this node is a boolean value.
   *
   * @return true if this node is a boolean value, false otherwise
   */
  public abstract Boolean isBoolean();

  /**
   * Get this node as a boolean value.
   *
   * @return the boolean value of this node
   * @throws SpinDataFormatException if this node is not a boolean value
   */
  public abstract Boolean boolValue();

  /**
   * Check if this node is a number value.
   *
   * @return true if this node is a number value, false otherwise
   */
  public abstract Boolean isNumber();

  /**
   * Get this node as a number value.
   *
   * @return the number value of this node
   * @throws SpinDataFormatException if this node is not a number value
   */
  public abstract Number numberValue();

  /**
   * Check if this node is a string value.
   *
   * @return true if this node is a string value, false otherwise
   */
  public abstract Boolean isString();

  /**
   * Get this node as a number value.
   *
   * @return the string value of this node
   * @throws SpinDataFormatException if this node is not a string value
   */
  public abstract String stringValue();

  /**
   * Check if this node represents a null value.
   *
   * @return true if this node is a null value, false otherwise
   */
  public abstract Boolean isNull();

  /**
   * Check if this node is a value.
   *
   * @return true if this node is a value, false otherwise
   */
  public abstract Boolean isValue();

  /**
   * Gets the actual value of the node, in case it is a Boolean/String/Number/Null node.
   * In that case a Java Boolean/String/Number or null is returned.
   *
   * @return the value of this node
   * @throws SpinDataFormatException if this node is not a Boolean/String/Number/Nul value
   */
  public abstract Object value();

  /**
   * Check if this node is a array value.
   *
   * @return true if this node is a array value, false otherwise
   */
  public abstract Boolean isArray();

  /**
   * Get this node as list.
   *
   * @return the list value of this node
   * @throws SpinDataFormatException if this node is not a array value
   */
  public abstract SpinList<SpinJsonNode> elements();

  /**
   * Get the field names of this node (i.e. the property names).
   *
   * @return the list of field names
   * @throws SpinDataFormatException if this node is not a array value
   */
  public abstract List<String> fieldNames();

  /**
   * Creates a JsonPath query on this element.
   *
   * @param expression the JsonPath expression
   * @return the JsonPath query
   */
  public abstract SpinJsonPathQuery jsonPath(String expression);
}
