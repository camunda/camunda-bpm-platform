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
package org.camunda.bpm.engine.variable;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.core.variable.value.NullValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.BooleanValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.BytesValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.DateValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.DoubleValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.IntegerValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.LongValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.ShortValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.PrimitiveTypeValueImpl.StringValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.ObjectVariableBuilderImpl;
import org.camunda.bpm.engine.impl.core.variable.value.builder.SerializedObjectValueBuilderImpl;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.TypedValueBuilder;

/**
 *
 * @author Daniel Meyer
 *
 */
public class Variables {

  public static VariableMap createVariables() {
    return new VariableMapImpl();
  }

  public static VariableMap fromMap(Map<String, Object> map) {
    if(map instanceof VariableMap) {
      return (VariableMap) map;
    }
    else {
      return new VariableMapImpl(map);
    }
  }

  public static ObjectValueBuilder objectValue(Object value) {
    return new ObjectVariableBuilderImpl(value);
  }

  public static SerializedObjectValueBuilder serializedObjectValue() {
    return new SerializedObjectValueBuilderImpl();
  }

  public static SerializedObjectValueBuilder serializedObjectValue(String value) {
    return serializedObjectValue().serializedValue(value);
  }

  public static IntegerValue integerValue(Integer integer) {
    return new IntegerValueImpl(integer);
  }

  public static StringValue stringValue(String stringValue) {
    return new StringValueImpl(stringValue);
  }

  public static BooleanValue booleanValue(Boolean booleanValue) {
    return new BooleanValueImpl(booleanValue);
  }

  public static BytesValue byteArrayValue(byte[] bytes) {
    return new BytesValueImpl(bytes);
  }

  public static DateValue dateValue(Date date) {
    return new DateValueImpl(date);
  }

  public static LongValue longValue(Long longValue) {
    return new LongValueImpl(longValue);
  }

  public static ShortValue shortValue(Short shortValue) {
    return new ShortValueImpl(shortValue);
  }

  public static DoubleValue doubleValue(Double doubleValue) {
    return new DoubleValueImpl(doubleValue);
  }

  public static TypedValue untypedNullValue() {
    return NullValueImpl.INSTANCE;
  }

  public static TypedValue untypedValue(Object value) {
    if(value == null) {
      return untypedNullValue();
    } else if (value instanceof TypedValueBuilder<?>) {
      return ((TypedValueBuilder<?>) value).create();
    }
    else if (value instanceof TypedValue) {
      return (TypedValue) value;
    }
    else {
      // unknown value
      return new UntypedValueImpl(value);
    }
  }
}
