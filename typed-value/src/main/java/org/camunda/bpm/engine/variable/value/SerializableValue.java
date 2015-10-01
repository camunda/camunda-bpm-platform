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
package org.camunda.bpm.engine.variable.value;

import org.camunda.bpm.engine.variable.type.SerializableValueType;

/**
 * A {@link TypedValue} for which a serialized value can be obtained and specified
 *
 * @author Daniel Meyer
 *
 */
public interface SerializableValue extends TypedValue {

  /**
   * Returns true in case the value is deserialized. If this method returns true,
   * it is safe to call the {@link #getValue()} method
   *
   * @return true if the object is deserialized.
   */
  boolean isDeserialized();

  /**
   * Returns the value or null in case the value is null.
   *
   * @return the value represented by this TypedValue.
   * @throws IllegalStateException in case the value is not deserialized. See {@link #isDeserialized()}.
   */
  Object getValue();

  /**
   * Returns the serialized value. In case the serializaton data format
   * (as returned by {@link #getSerializationDataFormat()}) is not text based,
   * a base 64 encoded representation of the value is returned
   *
   * The serialized value is a snapshot of the state of the value as it is
   * serialized to the process engine database.
   */
  String getValueSerialized();

  /**
   * The serialization format used to serialize this value.
   *
   * @return the serialization format used to serialize this variable.
   */
  String getSerializationDataFormat();

  SerializableValueType getType();

}
