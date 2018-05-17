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
package org.camunda.bpm.engine.variable.impl.value;

import java.util.Date;

import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.LongValue;
import org.camunda.bpm.engine.variable.value.NumberValue;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;

/**
 * @author Daniel Meyer
 *
 */
public class PrimitiveTypeValueImpl<T> extends AbstractTypedValue<T> implements PrimitiveValue<T> {

  private static final long serialVersionUID = 1L;

  public PrimitiveTypeValueImpl(T value, PrimitiveValueType type) {
    super(value, type);
  }

  @Override
  public PrimitiveValueType getType() {
    return (PrimitiveValueType) super.getType();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + (isTransient ? 1 : 0);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PrimitiveTypeValueImpl<?> other = (PrimitiveTypeValueImpl<?>) obj;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    if (isTransient != other.isTransient()) {
      return false;
    }
    return true;
  }


  // value type implementations ////////////////////////////////////

  public static class BooleanValueImpl extends PrimitiveTypeValueImpl<Boolean> implements BooleanValue {

    private static final long serialVersionUID = 1L;

    public BooleanValueImpl(Boolean value) {
      super(value, ValueType.BOOLEAN);
    }

    public BooleanValueImpl(Boolean value, boolean isTransient) {
        this(value);
        this.isTransient = isTransient;
    }
  }

  public static class BytesValueImpl extends PrimitiveTypeValueImpl<byte[]> implements BytesValue {

    private static final long serialVersionUID = 1L;

    public BytesValueImpl(byte[] value) {
      super(value, ValueType.BYTES);
    }

    public BytesValueImpl(byte[] value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class DateValueImpl extends PrimitiveTypeValueImpl<Date> implements DateValue {

    private static final long serialVersionUID = 1L;

    public DateValueImpl(Date value) {
      super(value, ValueType.DATE);
    }

    public DateValueImpl(Date value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class DoubleValueImpl extends PrimitiveTypeValueImpl<Double> implements DoubleValue {

    private static final long serialVersionUID = 1L;

    public DoubleValueImpl(Double value) {
      super(value, ValueType.DOUBLE);
    }

    public DoubleValueImpl(Double value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class IntegerValueImpl extends PrimitiveTypeValueImpl<Integer> implements IntegerValue {

    private static final long serialVersionUID = 1L;

    public IntegerValueImpl(Integer value) {
      super(value, ValueType.INTEGER);
    }

    public IntegerValueImpl(Integer value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class LongValueImpl extends PrimitiveTypeValueImpl<Long> implements LongValue {

    private static final long serialVersionUID = 1L;

    public LongValueImpl(Long value) {
      super(value, ValueType.LONG);
    }

    public LongValueImpl(Long value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class ShortValueImpl extends PrimitiveTypeValueImpl<Short> implements ShortValue {

    private static final long serialVersionUID = 1L;

    public ShortValueImpl(Short value) {
      super(value, ValueType.SHORT);
    }

    public ShortValueImpl(Short value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class StringValueImpl extends PrimitiveTypeValueImpl<String> implements StringValue {

    private static final long serialVersionUID = 1L;

    public StringValueImpl(String value) {
      super(value, ValueType.STRING);
    }

    public StringValueImpl(String value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

  public static class NumberValueImpl extends PrimitiveTypeValueImpl<Number> implements NumberValue {

    private static final long serialVersionUID = 1L;

    public NumberValueImpl(Number value) {
      super(value, ValueType.NUMBER);
    }

    public NumberValueImpl(Number value, boolean isTransient) {
      this(value);
      this.isTransient = isTransient;
    }
  }

}
