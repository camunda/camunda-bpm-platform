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
package org.camunda.bpm.engine.variable.impl.type;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.type.NullType;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.DoubleValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.IntegerValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.LongValueImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl.ShortValueImpl;
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

  @Override
  public String toString() {
    return "PrimitiveValueType["+getName()+"]";
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (typedValue.isTransient())
      result.put(VALUE_INFO_TRANSIENT, typedValue.isTransient());
    return result;
  }

  // concrete types ///////////////////////////////////////////////////

  public static class BooleanTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BooleanTypeImpl() {
      super(Boolean.class);
    }

    public BooleanValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.booleanValue((Boolean) value, isTransient(valueInfo));
    }

  }

  public static class BytesTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public BytesTypeImpl() {
      super("bytes", byte[].class);
    }

    public BytesValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.byteArrayValue((byte[]) value, isTransient(valueInfo));
    }

  }

  public static class DateTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DateTypeImpl() {
      super(Date.class);
    }

    public DateValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.dateValue((Date) value, isTransient(valueInfo));
    }

  }

  public static class DoubleTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public DoubleTypeImpl() {
      super(Double.class);
    }

    public DoubleValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.doubleValue((Double) value, isTransient(valueInfo));
    }

    @Override
    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    @Override
    public boolean canConvertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        return false;
      }

      return true;
    }

    @Override
    public DoubleValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }
      DoubleValueImpl doubleValue = null;
      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        doubleValue = (DoubleValueImpl) Variables.doubleValue(numberValue.getValue().doubleValue());
      } else {
        doubleValue = (DoubleValueImpl) Variables.doubleValue(null);
      }
      doubleValue.setTransient(numberValue.isTransient());
      return doubleValue;
    }
  }

  public static class IntegerTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public IntegerTypeImpl() {
      super(Integer.class);
    }

    public IntegerValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.integerValue((Integer) value, isTransient(valueInfo));
    }

    @Override
    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    @Override
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

    @Override
    public IntegerValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      IntegerValueImpl integerValue = null;
      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        integerValue = (IntegerValueImpl) Variables.integerValue(numberValue.getValue().intValue());
      } else {
        integerValue = (IntegerValueImpl) Variables.integerValue(null);
      }
      integerValue.setTransient(numberValue.isTransient());
      return integerValue;
    }
  }

  public static class LongTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public LongTypeImpl() {
      super(Long.class);
    }

    public LongValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.longValue((Long) value, isTransient(valueInfo));
    }

    @Override
    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    @Override
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

    @Override
    public LongValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      LongValueImpl longvalue = null;
      NumberValue numberValue = (NumberValue) typedValue;

      if (numberValue.getValue() != null) {
        longvalue = (LongValueImpl) Variables.longValue(numberValue.getValue().longValue());
      } else {
        longvalue =  (LongValueImpl) Variables.longValue(null);
      }
      longvalue.setTransient(numberValue.isTransient());
      return longvalue;
    }
  }

  public static class NullTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public NullTypeImpl() {
      super("null", NullType.class);
    }

    public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.untypedNullValue(isTransient(valueInfo));
    }

  }

  public static class ShortTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public ShortTypeImpl() {
      super(Short.class);
    }

    public ShortValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.shortValue((Short) value, isTransient(valueInfo));
    }

    @Override
    public ValueType getParent() {
      return ValueType.NUMBER;
    }

    @Override
    public ShortValue convertFromTypedValue(TypedValue typedValue) {
      if (typedValue.getType() != ValueType.NUMBER) {
        throw unsupportedConversion(typedValue.getType());
      }

      ShortValueImpl shortValue = null;
      NumberValue numberValue = (NumberValue) typedValue;
      if (numberValue.getValue() != null) {
        shortValue = (ShortValueImpl) Variables.shortValue(numberValue.getValue().shortValue());
      } else {
        shortValue =  (ShortValueImpl) Variables.shortValue(null);
      }
      shortValue.setTransient(numberValue.isTransient());
      return shortValue;
    }

    @Override
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
      return Variables.stringValue((String) value, isTransient(valueInfo));
    }
  }

  public static class NumberTypeImpl extends PrimitiveValueTypeImpl {

    private static final long serialVersionUID = 1L;

    public NumberTypeImpl() {
      super(Number.class);
    }

    public NumberValue createValue(Object value, Map<String, Object> valueInfo) {
      return Variables.numberValue((Number) value, isTransient(valueInfo));
    }

    @Override
    public boolean isAbstract() {
      return true;
    }
  }


}
