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

import org.camunda.bpm.engine.impl.core.variable.type.ObjectTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.BooleanTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.BytesTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.DateTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.DoubleTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.IntegerTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.LongTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.NullTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.ShortTypeImpl;
import org.camunda.bpm.engine.impl.core.variable.type.PrimitiveValueTypeImpl.StringTypeImpl;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 *
 * @author Thorben Lindhauer
 * @author Daniel Meyer
 *
 * @since 7.2
 */
public interface ValueType extends Serializable {

  public static final PrimitiveValueType NULL = new NullTypeImpl();

  public static final PrimitiveValueType BOOLEAN = new BooleanTypeImpl();

  public static final PrimitiveValueType SHORT = new ShortTypeImpl();

  public static final PrimitiveValueType LONG = new LongTypeImpl();

  public static final PrimitiveValueType DOUBLE = new DoubleTypeImpl();

  public static final PrimitiveValueType STRING = new StringTypeImpl();

  public static final PrimitiveValueType INTEGER = new IntegerTypeImpl();

  public static final PrimitiveValueType DATE = new DateTypeImpl();

  public static final PrimitiveValueType BYTES = new BytesTypeImpl();

  public static final SerializableValueType OBJECT = new ObjectTypeImpl();

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
   * Get the value info for a {@link TypedValue}.
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

}
