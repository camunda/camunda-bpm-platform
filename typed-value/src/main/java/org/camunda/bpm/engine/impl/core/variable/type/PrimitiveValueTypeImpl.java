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
package org.camunda.bpm.engine.impl.core.variable.type;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.lang.model.type.NullType;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.NumberValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Implementation of the primitive variable value types
 *
 * @author Daniel Meyer
 *
 */
public abstract class PrimitiveValueTypeImpl extends AbstractValueTypeImpl implements PrimitiveValueType {

  private static final long serialVersionUID = 1L;

  protected Class<?> javaType;

  public PrimitiveValueTypeImpl(Class<?> javaType) {
    this(javaType.getSimpleName().toLowerCase(), javaType);
  }

  public PrimitiveValueTypeImpl(String name, Class<?> javaType) {
    super(name);
    this.javaType = javaType;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public boolean isPrimitiveValueType() {
    return true;
  }

  public String toString() {
    return "PrimitiveValueType["+getName()+"]";
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    return Collections.emptyMap();
  }

  // concrete types ///////////////////////////////////////////////////

  public static class BooleanTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BooleanTypeImpl() {
      super(Boolean.class);
    }

    public BooleanValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.booleanValue((Boolean) value);
    }

  }

  public static class BytesTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BytesTypeImpl() {
      super("bytes", byte[].class);
    }

    public BytesValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.byteArrayValue((byte[]) value);
    }

  }

  public static class DateTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DateTypeImpl() {
      super(Date.class);
    }

    public DateValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.dateValue((Date) value);
    }

  }

  public static class DoubleTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DoubleTypeImpl() {
      super(Double.class);
    }

    public DoubleValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.doubleValue((Double) value);
    }

    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    public boolean canConvertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        return false;
      }

      return true;
    }

    public DoubleValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        return Variables.doubleValue(numberValue.getValue().doubleValue());
      } else {
        return Variables.doubleValue(null);
      }
    }
  }

  public static class IntegerTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public IntegerTypeImpl() {
      super(Integer.class);
    }

    public IntegerValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.integerValue((Integer) value);
    }

    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    public boolean canConvertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        return false;
      }

      if (typedValue.getValue() != null) {
        NumberValue numberValue = (NumberValue) typedValue;
        double doubleValue = numberValue.getValue().doubleValue();

        // returns false if the value changes due to conversion (e.g. by overflows
        // or by loss in precision)
        if (numberValue.getValue().intValue() != doubleValue) {
          return false;
        }
      }

      return true;
    }

    public IntegerValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        return Variables.integerValue(numberValue.getValue().intValue());
      } else {
        return Variables.integerValue(null);
      }
    }
  }

  public static class LongTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public LongTypeImpl() {
      super(Long.class);
    }

    public LongValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.longValue((Long) value);
    }

    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    public boolean canConvertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        return false;
      }

      if (typedValue.getValue() != null) {
        NumberValue numberValue = (NumberValue) typedValue;
        double doubleValue = numberValue.getValue().doubleValue();

        // returns false if the value changes due to conversion (e.g. by overflows
        // or by loss in precision)
        if (numberValue.getValue().longValue() != doubleValue) {
          return false;
        }
      }

      return true;
    }

    public LongValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      NumberValue numberValue = (NumberValue) typedValue;

      if (numberValue.getValue() != null) {
        return Variables.longValue(numberValue.getValue().longValue());
      } else {
        return Variables.longValue(null);
      }
    }
  }

  public static class NullTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public NullTypeImpl() {
      super("null", NullType.class);
    }

    public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.untypedNullValue();
    }

  }

  public static class ShortTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public ShortTypeImpl() {
      super(Short.class);
    }

    public ShortValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.shortValue((Short) value);
    }

    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    public ShortValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        return Variables.shortValue(numberValue.getValue().shortValue());
      } else {
        return Variables.shortValue(null);
      }
    }

    public boolean canConvertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        return false;
      }

      if (typedValue.getValue() != null) {
        NumberValue numberValue = (NumberValue) typedValue;
        double doubleValue = numberValue.getValue().doubleValue();

        // returns false if the value changes due to conversion (e.g. by overflows
        // or by loss in precision)
        if (numberValue.getValue().shortValue() != doubleValue) {
          return false;
        }
      }

      return true;
    }
  }

  public static class StringTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public StringTypeImpl() {
      super(String.class);
    }

    public StringValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.stringValue((String) value);
    }
  }

  public static class NumberTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public NumberTypeImpl() {
      super(Number.class);
    }

    public NumberValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.numberValue((Number) value);
    }

    public boolean isAbstract() {
      return true;
    }
  }


}
