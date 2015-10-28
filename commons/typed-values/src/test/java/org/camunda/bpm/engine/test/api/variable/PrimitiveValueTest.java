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
package org.camunda.bpm.engine.test.api.variable;

import static org.camunda.bpm.engine.variable.Variables.booleanValue;
import static org.camunda.bpm.engine.variable.Variables.byteArrayValue;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.dateValue;
import static org.camunda.bpm.engine.variable.Variables.doubleValue;
import static org.camunda.bpm.engine.variable.Variables.integerValue;
import static org.camunda.bpm.engine.variable.Variables.shortValue;
import static org.camunda.bpm.engine.variable.Variables.stringValue;
import static org.camunda.bpm.engine.variable.Variables.untypedNullValue;
import static org.camunda.bpm.engine.variable.type.ValueType.BOOLEAN;
import static org.camunda.bpm.engine.variable.type.ValueType.BYTES;
import static org.camunda.bpm.engine.variable.type.ValueType.DATE;
import static org.camunda.bpm.engine.variable.type.ValueType.DOUBLE;
import static org.camunda.bpm.engine.variable.type.ValueType.INTEGER;
import static org.camunda.bpm.engine.variable.type.ValueType.NULL;
import static org.camunda.bpm.engine.variable.type.ValueType.SHORT;
import static org.camunda.bpm.engine.variable.type.ValueType.STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Philipp Ossler *
 */
@RunWith(Parameterized.class)
public class PrimitiveValueTest {

  protected static final Date DATE_VALUE = new Date();
  protected static final String LOCAL_DATE_VALUE = "2015-09-18";
  protected static final String LOCAL_TIME_VALUE = "10:00:00";
  protected static final String PERIOD_VALUE = "P14D";
  protected static final byte[] BYTES_VALUE = "a".getBytes();

  @Parameters(name = "{index}: {0} = {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { STRING, "someString", stringValue("someString"), stringValue(null) },
        { INTEGER, 1, integerValue(1), integerValue(null) },
        { BOOLEAN, true, booleanValue(true), booleanValue(null) },
        { NULL, null, untypedNullValue(), untypedNullValue() },
        { SHORT, (short) 1, shortValue((short) 1), shortValue(null) },
        { DOUBLE, 1d, doubleValue(1d), doubleValue(null) },
        { DATE, DATE_VALUE, dateValue(DATE_VALUE), dateValue(null) },
        { BYTES, BYTES_VALUE, byteArrayValue(BYTES_VALUE), byteArrayValue(null) }
      });
  }

  @Parameter(0)
  public ValueType valueType;

  @Parameter(1)
  public Object value;

  @Parameter(2)
  public TypedValue typedValue;

  @Parameter(3)
  public TypedValue nullValue;

  protected String variableName = "variable";

  @Test
  public void testCreatePrimitiveVariableUntyped() {
    VariableMap variables = createVariables().putValue(variableName, value);

    assertEquals(value, variables.get(variableName));
    assertEquals(value, variables.getValueTyped(variableName).getValue());

    // no type information present
    TypedValue typedValue = variables.getValueTyped(variableName);
    if (!(typedValue instanceof NullValueImpl)) {
      assertNull(typedValue.getType());
      assertEquals(variables.get(variableName), typedValue.getValue());
    } else {
      assertEquals(NULL, typedValue.getType());
    }
  }

  @Test
  public void testCreatePrimitiveVariableTyped() {
    VariableMap variables = createVariables().putValue(variableName, typedValue);

    // get return value
    assertEquals(value, variables.get(variableName));

    // type is not lost
    assertEquals(valueType, variables.getValueTyped(variableName).getType());

    // get wrapper
    Object stringValue = variables.getValueTyped(variableName).getValue();
    assertEquals(value, stringValue);
  }

  @Test
  public void testCreatePrimitiveVariableNull() {
    VariableMap variables = createVariables().putValue(variableName, nullValue);

    // get return value
    assertEquals(null, variables.get(variableName));

    // type is not lost
    assertEquals(valueType, variables.getValueTyped(variableName).getType());

    // get wrapper
    Object stringValue = variables.getValueTyped(variableName).getValue();
    assertEquals(null, stringValue);
  }

}
