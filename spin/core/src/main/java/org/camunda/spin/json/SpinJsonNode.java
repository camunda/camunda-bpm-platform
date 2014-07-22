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

import java.util.List;

import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.spi.SpinDataFormatException;

/**
 * A json node.
 * 
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public abstract class SpinJsonNode extends Spin<SpinJsonNode> {

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
   * Check if this node is a value.
   *
   * @return true if this node is a value, false otherwise
   */
  public abstract Boolean isValue();

  /**
   * Get this node as a number value.
   *
   * @return the string value of this node
   * @throws SpinDataFormatException if this node is not a string value
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
}
