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
import org.camunda.spin.json.SpinJsonTreePropertyException;
import org.camunda.spin.spi.SpinJsonDataFormatException;
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
    name = "JsonTreeReadPropertyTest.shouldCheckStringValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCheckStringValue() {
    Boolean value1 = script.getVariable("value1");
    Boolean value2 = script.getVariable("value2");
    Boolean value3 = script.getVariable("value3");
    Boolean value4 = script.getVariable("value4");
    Boolean value5 = script.getVariable("value5");

    assertThat(value1).isEqualTo(true);
    assertThat(value2).isEqualTo(false);
    assertThat(value3).isEqualTo(false);
    assertThat(value4).isEqualTo(false);
    assertThat(value5).isEqualTo(false);
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldCheckNumberValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCheckNumberValue() {
    Boolean value1 = script.getVariable("value1");
    Boolean value2 = script.getVariable("value2");
    Boolean value3 = script.getVariable("value3");
    Boolean value4 = script.getVariable("value4");
    Boolean value5 = script.getVariable("value5");

    assertThat(value1).isEqualTo(false);
    assertThat(value2).isEqualTo(true);
    assertThat(value3).isEqualTo(false);
    assertThat(value4).isEqualTo(false);
    assertThat(value5).isEqualTo(false);
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldCheckBooleanValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCheckBooleanValue() {
    Boolean value1 = script.getVariable("value1");
    Boolean value2 = script.getVariable("value2");
    Boolean value3 = script.getVariable("value3");
    Boolean value4 = script.getVariable("value4");
    Boolean value5 = script.getVariable("value5");

    assertThat(value1).isEqualTo(false);
    assertThat(value2).isEqualTo(false);
    assertThat(value3).isEqualTo(false);
    assertThat(value4).isEqualTo(false);
    assertThat(value5).isEqualTo(true);
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldCheckArrayValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCheckArrayValue() {
    Boolean value1 = script.getVariable("value1");
    Boolean value2 = script.getVariable("value2");
    Boolean value3 = script.getVariable("value3");
    Boolean value4 = script.getVariable("value4");
    Boolean value5 = script.getVariable("value5");

    assertThat(value1).isEqualTo(false);
    assertThat(value2).isEqualTo(false);
    assertThat(value3).isEqualTo(true);
    assertThat(value4).isEqualTo(false);
    assertThat(value5).isEqualTo(false);
  }

  @Test
  @Script(
    name = "JsonTreeReadPropertyTest.shouldCheckObjectValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCheckObjectValue() {
    Object value = script.getVariable("value");

    assertThat((String) value).isEqualTo("order1");
  }

  /**
   * One for array
   * @throws Throwable
   */
  @Test( expected = SpinJsonDataFormatException.class)
  @Script(
    name="JsonTreeReadPropertyTest.shouldFailToCheckObject",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToCheckObject() throws Throwable{
    failingWithException();
  }

  /**
   * One for child node
   * @throws Throwable
   */
  @Test(expected = SpinJsonDataFormatException.class)
  @Script(
    name="JsonTreeReadPropertyTest.shouldFailToCheckObject2",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToCheckObject2() throws Throwable{
    failingWithException();
  }

  /**
   * One for not existent property
   * @throws Throwable
   */
  @Test(expected = SpinJsonTreePropertyException.class)
  @Script(
    name="JsonTreeReadPropertyTest.shouldFailToReadProperty",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToReadProperty() throws Throwable{
    failingWithException();
  }

  /**
   * One for property argument equals null
   * @throws Throwable
   */
  @Test(expected = IllegalArgumentException.class)
  @Script(
    name="JsonTreeReadPropertyTest.shouldFailToReadProperty2",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToReadProperty2() throws Throwable{
    failingWithException();
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

  @Test(expected = SpinJsonDataFormatException.class)
   @Script(
     name = "JsonTreeReadPropertyTest.shouldFailToReadNumberValue",
     variables = {
       @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
     },
     execute = false
   )
   public void shouldFailToReadNumberValue() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(
    name = "JsonTreeReadPropertyTest.shouldFailToReadBooleanValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToReadBooleanValue() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(
    name = "JsonTreeReadPropertyTest.shouldFailToReadStringValue",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToReadStringValue() throws Throwable {
    failingWithException();
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

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(
    name = "JsonTreeReadPropertyTest.shouldFailToReadObjectInNonArray",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
    },
    execute = false
  )
  public void shouldFailToReadObjectInNonArray() throws Throwable{
    failingWithException();
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

    String javaVariable1 = property1.stringValue();
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

