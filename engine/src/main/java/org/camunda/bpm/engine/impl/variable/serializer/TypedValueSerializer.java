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
package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * A {@link TypedValueSerializer} persists {@link TypedValue TypedValues} of a given
 * {@link ValueType} to provided {@link ValueFields}.
 *<p>
 * Replaces the "VariableType" interface in previous versions.
 *
 * @author Daniel Meyer
 *
 * @since 7.2
 */
public interface TypedValueSerializer<T extends TypedValue> {

  /**
   * The name of this serializer. The name is used when persisting the ValueFields populated by this serializer.
   *
   * @return the name of this serializer.
   */
  String getName();

  /**
   * The {@link ValueType VariableType} supported
   * @return the VariableType supported
   */
  ValueType getType();

  /**
   * Serialize a {@link TypedValue} to the {@link ValueFields}.
   *
   * @param value the {@link TypedValue} to persist
   * @param valueFields the {@link ValueFields} to which the value should be persisted
   */
  void writeValue(T value, ValueFields valueFields);

  /**
   * Retrieve a {@link TypedValue} from the provided {@link ValueFields}.
   *
   * @param valueFields the {@link ValueFields} to retrieve the value from
   * @param deserializeValue indicates whether a {@link SerializableValue} should be deserialized.
   *
   * @return the {@link TypedValue}
   */
  T readValue(ValueFields valueFields, boolean deserializeValue, boolean isTransient);

  /**
   * Used for auto-detecting the value type of a variable.
   * An implementation must return true if it is able to write values of the provided type.
   *
   * @param value the value
   * @return true if this {@link TypedValueSerializer} is able to handle the provided value
   */
  boolean canHandle(TypedValue value);

  /**
   * Returns a typed value for the provided untyped value. This is used on cases where the user sets an untyped
   * value which is then detected to be handled by this {@link TypedValueSerializer} (by invocation of {@link #canHandle(TypedValue)}).
   *
   * @param untypedValue the untyped value
   * @return the corresponding typed value
   */
  T convertToTypedValue(UntypedValueImpl untypedValue);

  /**
   *
   * @return the dataformat used by the serializer or null if this is not an object serializer
   */
  String getSerializationDataformat();

  /**
   * @return whether values serialized by this serializer can be mutable and
   * should be re-serialized if changed
   */
  boolean isMutableValue(T typedValue);

}
