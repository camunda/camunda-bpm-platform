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
package org.camunda.bpm.engine.variable.type;

import java.util.Map;

import org.camunda.bpm.engine.variable.value.SerializableValue;

/**
 * @author Daniel Meyer
 * @since 7.2
 */
public interface SerializableValueType extends ValueType {

  /**
   * Identifies the object's java type name.
   */
  String VALUE_INFO_OBJECT_TYPE_NAME = "objectTypeName";

  /**
   * Identifies the format in which the object is serialized.
   */
  String VALUE_INFO_SERIALIZATION_DATA_FORMAT = "serializationDataFormat";


  /**
   * Creates a new TypedValue using this type.
   * @param serializedValue the value in serialized form
   * @return the typed value for the value
   */
  SerializableValue createValueFromSerialized(String serializedValue, Map<String, Object> valueInfo);

}
