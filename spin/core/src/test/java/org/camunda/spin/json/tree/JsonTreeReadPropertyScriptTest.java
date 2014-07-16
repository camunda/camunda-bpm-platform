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

import org.camunda.spin.Spin;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class JsonTreeReadPropertyScriptTest extends ScriptTest {

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldReadProperty",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldReadProperty() {
    SpinJsonNode property = script.getVariable("property");
    String value = script.getVariable("value");

    assertThat(property).isNotNull();
    assertThat(value).isEqualTo("order1");
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldReadTextValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldReadTextValue() {
    String value1 = script.getVariable("value1");
    String value2 = script.getVariable("value2");
    String value3 = script.getVariable("value3");

    assertThat(value1).isEqualTo("order1");
    assertThat(value2).isEqualTo("Kermit");
    assertThat(value3).isEqualTo("camundaBPM");
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldReadNumberValue",
    variables = {
        @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldReadNumberValue() {
    Number value1 = script.getVariable("value1");
    Number value2 = script.getVariable("value2");
    Number value3 = script.getVariable("value3");

    assertThat(value1.longValue()).isEqualTo(20150112L);

    // python returns bigInt instead of Long
    assertThat(value2.longValue()).isEqualTo(1234567890987654321L);
    assertThat(value3).isEqualTo(32000.45);
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldReadBooleanValue",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldReadBooleanValue() {
    Boolean value1 = script.getVariable("value1");

    assertThat(value1).isEqualTo(true);
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldReadChildNode",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldReadChildNode() {
    SpinJsonNode childNode = script.getVariable("childNode");
    String value = script.getVariable("value");

    assertThat(childNode).isNotNull();
    assertThat(value).isEqualTo("camundaBPM");
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldReadChildNodeProperty",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldReadChildNodeProperty() {
    SpinJsonNode property1 = script.getVariable("property1");
    SpinJsonNode property2 = script.getVariable("property2");

    Number value1 = script.getVariable("value1");
    String value2 = script.getVariable("value2");

    assertThat(property1).isNotNull();
    assertThat(property2).isNotNull();

    // Ruby casts this to long instead int
    assertThat(value1.intValue()).isEqualTo(32000);
    assertThat(value2).isEqualTo("dollar");
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldReadObjectInArrayChildNode",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldReadObjectInArrayChildNode() {
    SpinJsonNode property1 = script.getVariable("property1");
    SpinJsonNode property2 = script.getVariable("property2");

    String value1 = script.getVariable("value1");
    String value2 = script.getVariable("value2");

    assertThat(property1).isNotNull();
    assertThat(property2).isNotNull();

    assertThat(value1).isEqualTo("Kermit");
    assertThat(value2).isEqualTo("Waldo");
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldReadListOfNodes",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldReadListOfNodes() {
    String value1 = script.getVariable("value1");
    String value2 = script.getVariable("value2");
    String value3 = script.getVariable("value3");

    assertThat(value1).isEqualTo("order");
    assertThat(value2).isEqualTo("dueUntil");
    assertThat(value3).isEqualTo("orderDetails");
  }

  @Test
  @Script(
      name = "JsonTreeReadPropertyTest.shouldBeSameAsJavaValue",
      variables = {
          @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
      }
  )
  public void shouldBeSameAsJavaValue() {

    SpinJsonNode node = Spin.JSON(EXAMPLE_JSON);
    SpinJsonNode childNode = node.prop("orderDetails");

    SpinJsonNode property1 = node.prop("order");
    SpinJsonNode property2 = childNode.prop("price");
    SpinJsonNode property3 = node.prop("active");

    String javaVariable1 = property1.value();
    Number javaVariable2 = property2.numberValue();
    Boolean javaVariable3 = property3.boolValue();

    String scriptVariable1 = script.getVariable("stringValue");
    Number scriptVariable2 = script.getVariable("numberValue");
    Boolean scriptVariable3 = script.getVariable("boolValue");

    assertThat(javaVariable1).isEqualTo(scriptVariable1);
    assertThat(javaVariable2).isEqualTo(scriptVariable2);
    assertThat(javaVariable3).isEqualTo(scriptVariable3);
  }
}

