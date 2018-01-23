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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.Test;

/**
 * @author Daniel Meyer
 *
 */
public class VariableApiTest {

  private static final String DESERIALIZED_OBJECT_VAR_NAME = "deserializedObject";
  private static final ExampleObject DESERIALIZED_OBJECT_VAR_VALUE = new ExampleObject();

  private static final String SERIALIZATION_DATA_FORMAT_NAME = "data-format-name";

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
  public void testVariableMapWithoutCreateVariables() {
    VariableMap map1 = putValue("foo", true).putValue("bar", 20);
    VariableMap map2 = putValueTyped("foo", booleanValue(true)).putValue("bar", integerValue(20));

    assertEquals(map1, map2);
    assertTrue(map1.values().containsAll(map2.values()));
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

  @Test
  public void testEmptyVariableMapAsVariableContext() {
    VariableContext varContext = createVariables().asVariableContext();
    assertTrue(varContext.keySet().size() == 0);
    assertNull(varContext.resolve("nonExisting"));
    assertFalse(varContext.containsVariable("nonExisting"));
  }

  @Test
  public void testEmptyVariableContext() {
    VariableContext varContext = emptyVariableContext();
    assertTrue(varContext.keySet().size() == 0);
    assertNull(varContext.resolve("nonExisting"));
    assertFalse(varContext.containsVariable("nonExisting"));
  }

  @Test
  public void testVariableMapAsVariableContext() {
    VariableContext varContext = createVariables()
        .putValueTyped("someValue", integerValue(1)).asVariableContext();

    assertTrue(varContext.keySet().size() == 1);

    assertNull(varContext.resolve("nonExisting"));
    assertFalse(varContext.containsVariable("nonExisting"));

    assertEquals(1, varContext.resolve("someValue").getValue());
    assertTrue(varContext.containsVariable("someValue"));
  }

  @Test
  public void testTransientVariables() throws URISyntaxException {
    VariableMap variableMap = createVariables().putValueTyped("foo", doubleValue(10.0, true))
                     .putValueTyped("bar", integerValue(10, true))
                     .putValueTyped("aa", booleanValue(true, true))
                     .putValueTyped("bb", stringValue("bb", true))
                     .putValueTyped("test", byteArrayValue("test".getBytes(), true))
                     .putValueTyped("blob", fileValue(new File(this.getClass().getClassLoader().getResource("org/camunda/bpm/engine/test/variables/simpleFile.txt").toURI()), true))
                     .putValueTyped("val", dateValue(new Date(), true))
                     .putValueTyped("var", objectValue(new Integer(10), true).create())
                     .putValueTyped("short", shortValue((short)12, true))
                     .putValueTyped("long", longValue((long)10, true))
                     .putValueTyped("file", fileValue("org/camunda/bpm/engine/test/variables/simpleFile.txt").setTransient(true).create())
                     .putValueTyped("hi", untypedValue("stringUntyped", true))
                     .putValueTyped("null", untypedValue(null, true))
                     .putValueTyped("ser", serializedObjectValue("{\"name\" : \"foo\"}", true).create());

    for (Entry<String, Object> e : variableMap.entrySet()) {
      TypedValue value = (TypedValue) variableMap.getValueTyped(e.getKey());
      assertTrue(value.isTransient());
    }
  }
}
