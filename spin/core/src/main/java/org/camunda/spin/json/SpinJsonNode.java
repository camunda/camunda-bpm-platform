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

/**
 * A json node.
 * 
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public abstract class SpinJsonNode extends Spin<SpinJsonNode> {

  // FIXME: add isBoolean, isValue, isObject, isContainer etc. methods

  /**
   * fetches a property by name
   *
   * @param name Name of the property
   * @return property SpinJsonNode representation of the property
   */
  public abstract SpinJsonNode prop(String name);

  /**
   * fetch boolean value of a property
   *
   * @return propertyValue value of type Boolean
   */
  public abstract Boolean boolValue();

  /**
   * fetch number value of a property
   *
   * @return propertyValue value of type Number
   */
  public abstract Number numberValue();

  /**
   * FIXME: should return value and add method stringValue() for String value
   * fetch string value of a property
   *
   * @return propertyValue value of type String
   */
  public abstract String value();

  /**
   * fetch data for json array
   *
   * @return list list of child nodes
   */
  public abstract SpinList<SpinJsonNode> elements();

  /**
   * fetch a list of field names for all child nodes of a node
   *
   * @return list list of field names
   */
  public abstract List<String> fieldNames();

}
