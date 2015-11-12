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
package org.camunda.spin.plugin.variable.type;

import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.spin.plugin.variable.type.impl.JsonValueTypeImpl;
import org.camunda.spin.plugin.variable.type.impl.XmlValueTypeImpl;

/**
 * @author Roman Smirnov
 *
 */
public interface SpinValueType extends SerializableValueType {

  /**
   * Identifies the Spin data format a value is an instance of.
   */
  static final String VALUE_INFO_SERIALIZATION_DATA_FORMAT = "serializationDataFormat";

  static final SpinValueType JSON = new JsonValueTypeImpl();

  static final SpinValueType XML = new XmlValueTypeImpl();

}
