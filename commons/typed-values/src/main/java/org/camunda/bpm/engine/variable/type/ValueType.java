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

import java.io.Serializable;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.FileValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.ObjectTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.BooleanTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.BytesTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.DateTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.DoubleTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.IntegerTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.LongTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.NullTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.NumberTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.ShortTypeImpl;
import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl.StringTypeImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 *
 * @since 7.2
 */
public interface ValueType extends Serializable {

  PrimitiveValueType NULL = new NullTypeImpl();

  PrimitiveValueType BOOLEAN = new BooleanTypeImpl();

  PrimitiveValueType SHORT = new ShortTypeImpl();

  PrimitiveValueType LONG = new LongTypeImpl();

  PrimitiveValueType DOUBLE = new DoubleTypeImpl();

  PrimitiveValueType STRING = new StringTypeImpl();

  PrimitiveValueType INTEGER = new IntegerTypeImpl();

  PrimitiveValueType DATE = new DateTypeImpl();

  PrimitiveValueType BYTES = new BytesTypeImpl();

  PrimitiveValueType NUMBER = new NumberTypeImpl();

  SerializableValueType OBJECT = new ObjectTypeImpl();

  FileValueType FILE = new FileValueTypeImpl();

  String VALUE_INFO_TRANSIENT = "transient";
  /**
   * Returns the name of the variable type
   */
  String getName();

  /**
   * Indicates whether this type is primitive valued. Primitive valued types can be handled
   * natively by the process engine.
   *
   * @return true if this is a primitive valued type. False otherwise
   */
  boolean isPrimitiveValueType();

  /**
   * Get the value info (meta data) for a {@link TypedValue}.
   * The keys of the returned map for a {@link TypedValue} are available
   * as constants in the value's {@link ValueType} interface.
   *
   * @param typedValue
   * @return
   */
  Map<String, Object> getValueInfo(TypedValue typedValue);

  /**
   * Creates a new TypedValue using this type.
   * @param value the value
   * @return the typed value for the value
   */
  public TypedValue createValue(Object value, Map<String, Object> valueInfo);

  /**
   * <p>Gets the parent value type.</p>
   *
   * <p>Value type hierarchy is only relevant for queries and has the
   * following meaning: When a value query is made
   * (e.g. all tasks with a certain variable value), a "child" type's value
   * also matches a parameter value of the parent type. This is only
   * supported when the parent value type's implementation of {@link #isAbstract()}
   * returns <code>true</code>.</p>
   */
  ValueType getParent();

  /**
   * Determines whether the argument typed value can be converted to a
   * typed value of this value type.
   */
  boolean canConvertFromTypedValue(TypedValue typedValue);

  /**
   * Converts a typed value to a typed value of this type.
   * This does not suceed if {@link #canConvertFromTypedValue(TypedValue)}
   * returns <code>false</code>.
   */
  TypedValue convertFromTypedValue(TypedValue typedValue);

  /**
   * <p>Returns whether the value type is abstract. This is <b>not related
   * to the term <i>abstract</i> in the Java language.</b></p>
   *
   * Abstract value types cannot be used as types for variables but only used for querying.
   */
  boolean isAbstract();


}
