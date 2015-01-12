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

import static org.camunda.bpm.engine.variable.Variables.*;
import static org.camunda.bpm.engine.variable.type.ValueType.BOOLEAN;
import static org.camunda.bpm.engine.variable.type.ValueType.BYTES;
import static org.camunda.bpm.engine.variable.type.ValueType.DATE;
import static org.camunda.bpm.engine.variable.type.ValueType.DOUBLE;
import static org.camunda.bpm.engine.variable.type.ValueType.INTEGER;
import static org.camunda.bpm.engine.variable.type.ValueType.NULL;
import static org.camunda.bpm.engine.variable.type.ValueType.SHORT;
import static org.camunda.bpm.engine.variable.type.ValueType.STRING;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.BooleanValue;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.camunda.bpm.engine.variable.value.DoubleValue;
import org.camunda.bpm.engine.variable.value.IntegerValue;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.ShortValue;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class VariableApiOfflineTest {

  private static final String STRING_VAR_NAME = "stringVariable";
  private static final String STRING_VAR_VALUE = "someString";

  private static final String INTEGER_VAR_NAME = "integerVariable";
  private static final Integer INTEGER_VAR_VALUE = 1;

  private static final String BOOLEAN_VAR_NAME = "booleanVariable";
  private static final Boolean BOOLEAN_VAR_VALUE = true;

  private static final String NULL_VAR_NAME = "nullVariable";

  private static final String SHORT_VAR_NAME = "shortVariable";
  private static final Short SHORT_VAR_VALUE = 1;

  private static final String DOUBLE_VAR_NAME = "doubleVariable";
  private static final Double DOUBLE_VAR_VALUE = 1d;

  private static final String DATE_VAR_NAME = "dateVariable";
  private static final Date DATE_VAR_VALUE = new Date(0);

  private static final String BYTES_VAR_NAME = "bytesVariable";
  private static final byte[] BYTES_VAR_VALUE = "a".getBytes();

  private static final String DESERIALIZED_OBJECT_VAR_NAME = "deserializedObject";
  private static final ExampleObject DESERIALIZED_OBJECT_VAR_VALUE = new ExampleObject();

  private static final String SERIALIZATION_DATA_FORMAT_NAME = "data-format-name";

  @Test
  public void testCreatePrimitiveVariablesUntyped() {

    VariableMap variables = createVariables()
      .putValue(STRING_VAR_NAME, STRING_VAR_VALUE)
      .putValue(INTEGER_VAR_NAME, INTEGER_VAR_VALUE)
      .putValue(BOOLEAN_VAR_NAME, BOOLEAN_VAR_VALUE)
      .putValue(NULL_VAR_NAME, null)
      .putValue(SHORT_VAR_NAME, SHORT_VAR_VALUE)
      .putValue(DOUBLE_VAR_NAME, DOUBLE_VAR_VALUE)
      .putValue(DATE_VAR_NAME, DATE_VAR_VALUE)
      .putValue(BYTES_VAR_NAME, BYTES_VAR_VALUE);

    assertEquals(STRING_VAR_VALUE, variables.get(STRING_VAR_NAME));
    assertEquals(INTEGER_VAR_VALUE, variables.get(INTEGER_VAR_NAME));
    assertEquals(BOOLEAN_VAR_VALUE, variables.get(BOOLEAN_VAR_NAME));
    assertEquals(null, variables.get(NULL_VAR_NAME));
    assertEquals(SHORT_VAR_VALUE, variables.get(SHORT_VAR_NAME));
    assertEquals(DOUBLE_VAR_VALUE, variables.get(DOUBLE_VAR_NAME));
    assertEquals(DATE_VAR_VALUE, variables.get(DATE_VAR_NAME));
    assertEquals(BYTES_VAR_VALUE, variables.get(BYTES_VAR_NAME));

    assertEquals(STRING_VAR_VALUE, variables.getValueTyped(STRING_VAR_NAME).getValue());
    assertEquals(INTEGER_VAR_VALUE, variables.getValueTyped(INTEGER_VAR_NAME).getValue());
    assertEquals(BOOLEAN_VAR_VALUE, variables.getValueTyped(BOOLEAN_VAR_NAME).getValue());
    assertEquals(null, variables.getValueTyped(NULL_VAR_NAME).getValue());
    assertEquals(SHORT_VAR_VALUE, variables.getValueTyped(SHORT_VAR_NAME).getValue());
    assertEquals(DOUBLE_VAR_VALUE, variables.getValueTyped(DOUBLE_VAR_NAME).getValue());
    assertEquals(DATE_VAR_VALUE, variables.getValueTyped(DATE_VAR_NAME).getValue());
    assertEquals(BYTES_VAR_VALUE, variables.getValueTyped(BYTES_VAR_NAME).getValue());

    // type for untyped null is untyped null
    TypedValue untypedNullVariable = variables.getValueTyped(NULL_VAR_NAME);
    assertNotNull(untypedNullVariable);
    assertEquals(NULL, untypedNullVariable.getType());
    variables.remove(NULL_VAR_NAME);

    // no type information present
    for (String varName : variables.keySet()) {
      TypedValue typedValue = variables.getValueTyped(varName);
      assertNull(typedValue.getType());
      assertEquals(variables.get(varName), typedValue.getValue());
    }

  }

  @Test
  public void testCreatePrimitiveVariablesTyped() {

    VariableMap variables = createVariables()
        .putValue(STRING_VAR_NAME, stringValue(STRING_VAR_VALUE))
        .putValue(INTEGER_VAR_NAME, integerValue(INTEGER_VAR_VALUE))
        .putValue(BOOLEAN_VAR_NAME, booleanValue(BOOLEAN_VAR_VALUE))
        .putValue(NULL_VAR_NAME, untypedNullValue())
        .putValue(SHORT_VAR_NAME, shortValue(SHORT_VAR_VALUE))
        .putValue(DOUBLE_VAR_NAME, doubleValue(DOUBLE_VAR_VALUE))
        .putValue(DATE_VAR_NAME, dateValue(DATE_VAR_VALUE))
        .putValue(BYTES_VAR_NAME, byteArrayValue(BYTES_VAR_VALUE));

    // get returns values

    assertEquals(STRING_VAR_VALUE, variables.get(STRING_VAR_NAME));
    assertEquals(INTEGER_VAR_VALUE, variables.get(INTEGER_VAR_NAME));
    assertEquals(BOOLEAN_VAR_VALUE, variables.get(BOOLEAN_VAR_NAME));
    assertEquals(null, variables.get(NULL_VAR_NAME));
    assertEquals(SHORT_VAR_VALUE, variables.get(SHORT_VAR_NAME));
    assertEquals(DOUBLE_VAR_VALUE, variables.get(DOUBLE_VAR_NAME));
    assertEquals(DATE_VAR_VALUE, variables.get(DATE_VAR_NAME));
    assertEquals(BYTES_VAR_VALUE, variables.get(BYTES_VAR_NAME));

    // types are not lost

    assertEquals(STRING, variables.getValueTyped(STRING_VAR_NAME).getType());
    assertEquals(INTEGER, variables.getValueTyped(INTEGER_VAR_NAME).getType());
    assertEquals(BOOLEAN, variables.getValueTyped(BOOLEAN_VAR_NAME).getType());
    assertEquals(NULL, variables.getValueTyped(NULL_VAR_NAME).getType());
    assertEquals(SHORT, variables.getValueTyped(SHORT_VAR_NAME).getType());
    assertEquals(DOUBLE, variables.getValueTyped(DOUBLE_VAR_NAME).getType());
    assertEquals(DATE, variables.getValueTyped(DATE_VAR_NAME).getType());
    assertEquals(BYTES, variables.getValueTyped(BYTES_VAR_NAME).getType());

    // get wrappers

    String stringValue = variables.<StringValue>getValueTyped(STRING_VAR_NAME).getValue();
    assertEquals(STRING_VAR_VALUE, stringValue);
    Integer integerValue = variables.<IntegerValue>getValueTyped(INTEGER_VAR_NAME).getValue();
    assertEquals(INTEGER_VAR_VALUE, integerValue);
    Boolean booleanValue = variables.<BooleanValue>getValueTyped(BOOLEAN_VAR_NAME).getValue();
    assertEquals(BOOLEAN_VAR_VALUE, booleanValue);
    Short shortValue = variables.<ShortValue>getValueTyped(SHORT_VAR_NAME).getValue();
    assertEquals(SHORT_VAR_VALUE, shortValue);
    Double doubleValue = variables.<DoubleValue>getValueTyped(DOUBLE_VAR_NAME).getValue();
    assertEquals(DOUBLE_VAR_VALUE, doubleValue);
    Date dateValue = variables.<DateValue>getValueTyped(DATE_VAR_NAME).getValue();
    assertEquals(DATE_VAR_VALUE, dateValue);
    byte[] bytesValue = variables.<BytesValue>getValueTyped(BYTES_VAR_NAME).getValue();
    assertEquals(BYTES_VAR_VALUE, bytesValue);

  }

  @Test
  public void testCreatePrimitiveVariablesNull() {

    VariableMap variables = createVariables()
      .putValue(STRING_VAR_NAME, stringValue(null))
      .putValue(INTEGER_VAR_NAME, integerValue(null))
      .putValue(BOOLEAN_VAR_NAME, booleanValue(null))
      .putValue(NULL_VAR_NAME, untypedNullValue())
      .putValue(SHORT_VAR_NAME, shortValue(null))
      .putValue(DOUBLE_VAR_NAME, doubleValue(null))
      .putValue(DATE_VAR_NAME, dateValue(null))
      .putValue(BYTES_VAR_NAME, byteArrayValue(null));

    // get returns values

    assertEquals(null, variables.get(STRING_VAR_NAME));
    assertEquals(null, variables.get(INTEGER_VAR_NAME));
    assertEquals(null, variables.get(BOOLEAN_VAR_NAME));
    assertEquals(null, variables.get(NULL_VAR_NAME));
    assertEquals(null, variables.get(SHORT_VAR_NAME));
    assertEquals(null, variables.get(DOUBLE_VAR_NAME));
    assertEquals(null, variables.get(DATE_VAR_NAME));
    assertEquals(null, variables.get(BYTES_VAR_NAME));

    // types are not lost

    assertEquals(STRING, variables.getValueTyped(STRING_VAR_NAME).getType());
    assertEquals(INTEGER, variables.getValueTyped(INTEGER_VAR_NAME).getType());
    assertEquals(BOOLEAN, variables.getValueTyped(BOOLEAN_VAR_NAME).getType());
    assertEquals(NULL, variables.getValueTyped(NULL_VAR_NAME).getType());
    assertEquals(SHORT, variables.getValueTyped(SHORT_VAR_NAME).getType());
    assertEquals(DOUBLE, variables.getValueTyped(DOUBLE_VAR_NAME).getType());
    assertEquals(DATE, variables.getValueTyped(DATE_VAR_NAME).getType());
    assertEquals(BYTES, variables.getValueTyped(BYTES_VAR_NAME).getType());

    // get wrappers

    String stringValue = variables.<StringValue>getValueTyped(STRING_VAR_NAME).getValue();
    assertEquals(null, stringValue);
    Integer integerValue = variables.<IntegerValue>getValueTyped(INTEGER_VAR_NAME).getValue();
    assertEquals(null, integerValue);
    Boolean booleanValue = variables.<BooleanValue>getValueTyped(BOOLEAN_VAR_NAME).getValue();
    assertEquals(null, booleanValue);
    Short shortValue = variables.<ShortValue>getValueTyped(SHORT_VAR_NAME).getValue();
    assertEquals(null, shortValue);
    Double doubleValue = variables.<DoubleValue>getValueTyped(DOUBLE_VAR_NAME).getValue();
    assertEquals(null, doubleValue);
    Date dateValue = variables.<DateValue>getValueTyped(DATE_VAR_NAME).getValue();
    assertEquals(null, dateValue);
    byte[] bytesValue = variables.<BytesValue>getValueTyped(BYTES_VAR_NAME).getValue();
    assertEquals(null, bytesValue);

  }

  @Test
  public void testCreateObjectVariables() {

    VariableMap variables = createVariables()
      .putValue(DESERIALIZED_OBJECT_VAR_NAME, objectValue(DESERIALIZED_OBJECT_VAR_VALUE));

    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.get(DESERIALIZED_OBJECT_VAR_NAME));
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.getValue(DESERIALIZED_OBJECT_VAR_NAME, ExampleObject.class));

    Object untypedValue = variables.getValueTyped(DESERIALIZED_OBJECT_VAR_NAME).getValue();
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, untypedValue);

    ExampleObject typedValue = variables.<ObjectValue>getValueTyped(DESERIALIZED_OBJECT_VAR_NAME).getValue(ExampleObject.class);
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, typedValue);

    // object type name is not yet available
    assertNull(variables.<ObjectValue>getValueTyped(DESERIALIZED_OBJECT_VAR_NAME).getObjectTypeName());
    // class is available
    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE.getClass(), variables.<ObjectValue>getValueTyped(DESERIALIZED_OBJECT_VAR_NAME).getObjectType());


    variables = createVariables()
        .putValue(DESERIALIZED_OBJECT_VAR_NAME, objectValue(DESERIALIZED_OBJECT_VAR_VALUE).serializationDataFormat(SERIALIZATION_DATA_FORMAT_NAME));

    assertEquals(DESERIALIZED_OBJECT_VAR_VALUE, variables.get(DESERIALIZED_OBJECT_VAR_NAME));


  }

  @Test
  public void testVariableMapCompatibility() {

    // test compatibility with Map<String, Object>
    VariableMap map1 = createVariables()
        .putValue("foo", 10)
        .putValue("bar", 20);

    // assert the map is assignable to Map<String,Object>
    @SuppressWarnings("unused")
    Map<String, Object> assignable = map1;

    VariableMap map2 = createVariables()
        .putValueTyped("foo", integerValue(10))
        .putValueTyped("bar", integerValue(20));

    Map<String, Object> map3 = new HashMap<String, Object>();
    map3.put("foo", 10);
    map3.put("bar", 20);

    // equals()
    assertEquals(map1, map2);
    assertEquals(map2, map3);
    assertEquals(map1, Variables.fromMap(map1));
    assertEquals(map1, Variables.fromMap(map3));

    // hashCode()
    assertEquals(map1.hashCode(), map2.hashCode());
    assertEquals(map2.hashCode(), map3.hashCode());

    // values()
    Collection<Object> values1 = map1.values();
    Collection<Object> values2 = map2.values();
    Collection<Object> values3 = map3.values();
    assertTrue(values1.containsAll(values2));
    assertTrue(values2.containsAll(values1));
    assertTrue(values2.containsAll(values3));
    assertTrue(values3.containsAll(values2));

    // entry set
    assertEquals(map1.entrySet(), map2.entrySet());
    assertEquals(map2.entrySet(), map3.entrySet());
  }

  @Test
  public void testSerializationDataFormats() {
    ObjectValue objectValue = objectValue(DESERIALIZED_OBJECT_VAR_VALUE).serializationDataFormat(SerializationDataFormats.JAVA).create();
    assertEquals(SerializationDataFormats.JAVA.getName(), objectValue.getSerializationDataFormat());

    objectValue = objectValue(DESERIALIZED_OBJECT_VAR_VALUE).serializationDataFormat(SerializationDataFormats.JSON).create();
    assertEquals(SerializationDataFormats.JSON.getName(), objectValue.getSerializationDataFormat());

    objectValue = objectValue(DESERIALIZED_OBJECT_VAR_VALUE).serializationDataFormat(SerializationDataFormats.XML).create();
    assertEquals(SerializationDataFormats.XML.getName(), objectValue.getSerializationDataFormat());
  }

}
