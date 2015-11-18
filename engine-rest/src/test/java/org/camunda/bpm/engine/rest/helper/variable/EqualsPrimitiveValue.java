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
package org.camunda.bpm.engine.rest.helper.variable;

import java.util.Arrays;
import java.util.Date;

import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.hamcrest.Description;


/**
 * @author Thorben Lindhauer
 *
 */
public class EqualsPrimitiveValue extends EqualsTypedValue<EqualsPrimitiveValue> {

  protected Object value;

  public EqualsPrimitiveValue value(Object value) {
    this.value = value;
    return this;
  }

  public boolean matches(Object argument) {
    if (!super.matches(argument)) {
      return false;
    }

    if (!PrimitiveValue.class.isAssignableFrom(argument.getClass())) {
      return false;
    }

    PrimitiveValue<?> primitveValue = (PrimitiveValue<?>) argument;

    if (value == null) {
      if (primitveValue.getValue() != null) {
        return false;
      }
    } else {
      if (!matchesValues(primitveValue.getValue())) {
        return false;
      }
    }

    return true;
  }

  protected boolean matchesValues(Object otherValue) {
    // explicit matching for byte[]
    if (value instanceof byte[]) {
      if (!(otherValue instanceof byte[])) {
        return false;
      }

      byte[] byteValue = (byte[]) value;
      byte[] otherByteValue = (byte[]) otherValue;

      return Arrays.equals(byteValue, otherByteValue);
    }

    if (type == ValueType.NUMBER) {
      if (!(otherValue instanceof Number)) {
        return false;
      }

      Number thisNumer = (Number) value;
      Number otherNumber = (Number) otherValue;

      return thisNumer.doubleValue() == otherNumber.doubleValue();
    }

    return value.equals(otherValue);

  }

  public static EqualsPrimitiveValue primitiveValueMatcher() {
    return new EqualsPrimitiveValue();
  }

  public static EqualsPrimitiveValue integerValue(Integer value) {
    return new EqualsPrimitiveValue().type(ValueType.INTEGER).value(value);
  }

  public static EqualsPrimitiveValue stringValue(String value) {
    return new EqualsPrimitiveValue().type(ValueType.STRING).value(value);
  }

  public static EqualsPrimitiveValue booleanValue(Boolean value) {
    return new EqualsPrimitiveValue().type(ValueType.BOOLEAN).value(value);
  }

  public static EqualsPrimitiveValue shortValue(Short value) {
    return new EqualsPrimitiveValue().type(ValueType.SHORT).value(value);
  }

  public static EqualsPrimitiveValue doubleValue(Double value) {
    return new EqualsPrimitiveValue().type(ValueType.DOUBLE).value(value);
  }

  public static EqualsPrimitiveValue longValue(Long value) {
    return new EqualsPrimitiveValue().type(ValueType.LONG).value(value);
  }

  public static EqualsPrimitiveValue bytesValue(byte[] value) {
    return new EqualsPrimitiveValue().type(ValueType.BYTES).value(value);
  }

  public static EqualsPrimitiveValue dateValue(Date value) {
    return new EqualsPrimitiveValue().type(ValueType.DATE).value(value);
  }

  public static EqualsPrimitiveValue numberValue(Number value) {
    return new EqualsPrimitiveValue().type(ValueType.NUMBER).value(value);
  }

  public void describeTo(Description description) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getClass().getSimpleName());
    sb.append(": ");
    sb.append("value=");
    sb.append(value);
    sb.append(", type=");
    sb.append(type);

    description.appendText(sb.toString());
  }

}
