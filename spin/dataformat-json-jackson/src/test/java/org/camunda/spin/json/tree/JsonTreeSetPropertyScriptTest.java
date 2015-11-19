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
package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class JsonTreeSetPropertyScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetStringProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    String value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    assertThat(value).isEqualTo("42!");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceStringProperty() {
    String oldValue = script.getVariable("oldValue");
    String newValue = script.getVariable("newValue");

    assertThat(newValue).isNotEqualTo(oldValue);
    assertThat(newValue).isEqualTo("new Order");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetIntegerProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Number value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    // Ruby casts Number to long
    assertThat(value.intValue()).isEqualTo(42);
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceIntegerProperty() {
    String oldValue = script.getVariable("oldValue");
    Number newValue = script.getVariable("newValue");

    assertThat(newValue).isNotEqualTo(oldValue);
    // Ruby casts Number to long
    assertThat(newValue.intValue()).isEqualTo(42);
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetFloatProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Number value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();

    // python returns Double, needs to cast to Float
    assertThat(value.floatValue()).isEqualTo(42.00F);
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceFloatProperty() {
    String oldValue = script.getVariable("oldValue");
    Number newValue = script.getVariable("newValue");

    assertThat(newValue).isNotEqualTo(oldValue);
    // Python returns a double instead a float
    assertThat(newValue.floatValue()).isEqualTo(42.00F);
  }

  @Test
   @Script
   @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
   public void shouldSetLongProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Number value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();

    // python returns BigInt, needs to cast to Long
    assertThat(value.floatValue()).isEqualTo(4200000000L);
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceLongProperty() {
    String oldValue = script.getVariable("oldValue");
    Number newValue = script.getVariable("newValue");

    assertThat(newValue).isNotEqualTo(oldValue);
    // python returns a BigInt, needs to cast it
    assertThat(newValue.longValue()).isEqualTo(4200000000L);
  }

  /**
   * Please note: in jython there is a known issue that Boolean
   * and boolean values are casted to long if a matching method
   * is found first. See script source code for workaround.
   */
  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetBooleanProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Boolean value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    assertThat(value).isFalse();
  }

  /**
   * Please note: in jython there is a known issue that Boolean
   * and boolean values are casted to long if a matching method
   * is found first. See script source code for workaround.
   */
  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceBooleanProperty() {
    String oldValue = script.getVariable("oldValue");
    Boolean newValue = script.getVariable("newValue");

    assertThat(newValue).isNotEqualTo(oldValue);
    assertThat(newValue).isFalse();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetArrayProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    String value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    assertThat(propertyNode.isArray()).isTrue();
    assertThat(value).isEqualTo("test2");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceArrayProperty() {
    SpinJsonNode oldValue = script.getVariable("oldValue");
    SpinJsonNode newValue = script.getVariable("newValue");

    assertThat(oldValue.isString()).isTrue();
    assertThat(newValue).isNotEqualTo(oldValue);
    assertThat(newValue.isArray()).isTrue();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetObjectProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    String value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    assertThat(propertyNode.isObject()).isTrue();
    assertThat(value).isEqualTo("42!");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceObjectProperty() {

    SpinJsonNode oldValue = script.getVariable("oldValue");
    SpinJsonNode newValue = script.getVariable("newValue");

    assertThat(oldValue.isString()).isTrue();
    assertThat(newValue).isNotEqualTo(oldValue);
    assertThat(newValue.isObject()).isTrue();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldSetNullProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Object value = script.getVariable("value");

    assertThat(propertyNode).isNotNull();
    assertThat(propertyNode.isNull()).isTrue();
    assertThat(value).isNull();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReplaceNullProperty() {
    SpinJsonNode propertyNode = script.getVariable("propertyNode");
    Object newValue = script.getVariable("newValue");

    assertThat(propertyNode).isNotNull();
    assertThat(propertyNode.isNull()).isTrue();
    assertThat(newValue).isNull();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailWhileSettingObject() throws Throwable{
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("date", new Date());
    failingWithException(variables);
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailWhileSettingArray() throws Throwable{
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("date", new Date());
    failingWithException(variables);
  }

}

