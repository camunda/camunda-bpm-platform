/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.spin.json;

import org.camunda.spin.SpinList;

/**
 * @author Stefan Hentschel
 */
public interface SpinJsonPathQuery {

  /**
   * Fetches the node from the result of JsonPath.
   *
   * @return {@link SpinJsonNode} representation of the found node
   * @throws SpinJsonPathException if node value is not a valid json path expression or the path is not found.
   */
  SpinJsonNode element();

  /**
   * Fetches the list of nodes from the result of JsonPath.
   *
   * @return {@link SpinList} list of found nodes
   * @throws SpinJsonDataFormatException if node value is not Array.
   */
  SpinList<SpinJsonNode> elementList();

  /**
   * Fetches the string value from the result of JsonPath.
   *
   * @return String value of found node
   * @throws SpinJsonDataFormatException if node value is not String.
   */
  String stringValue();

  /**
   * Fetches the number value from the result of JsonPath.
   *
   * @return Number value of found node
   * @throws SpinJsonDataFormatException if node value is not Number.
   */
  Number numberValue();

  /**
   * Fetches the boolean value from the result of JsonPath.
   *
   * @return Boolean value of found node
   * @throws SpinJsonDataFormatException if node value is not Boolean.
   */
  Boolean boolValue();
}
