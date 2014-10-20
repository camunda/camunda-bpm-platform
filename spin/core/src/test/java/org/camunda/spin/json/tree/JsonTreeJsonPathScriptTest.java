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

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPathException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class JsonTreeJsonPathScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetElementFromJsonPath() {
    SpinJsonNode node = script.getVariable("node");

    assertThat(node.isObject()).isTrue();
    assertThat(node.prop("article").isString()).isTrue();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetElementListFromJsonPath() {
    SpinList<SpinJsonNode> nodeList = script.getVariable("nodeList");

    assertThat(nodeList).hasSize(3);
    assertThat(nodeList.get(0).isObject()).isTrue();
    assertThat(nodeList.get(0).prop("name").isString()).isTrue();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetBooleanFromJsonPath() {
    Boolean bool = script.getVariable("booleanValue");

    assertThat(bool).isTrue();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetStringFromJsonPath() {
    String string = script.getVariable("stringValue");

    assertThat(string).isEqualTo("order1");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetNumberFromJsonPath() {
    Number number = script.getVariable("numberValue");

    assertThat(number.longValue()).isEqualTo(1234567890987654321L);
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetSingleArrayEntry() {
    SpinJsonNode node = script.getVariable("node");

    assertThat(node.isObject());
    assertThat(node.prop("name").isString());
    assertThat(node.prop("name").stringValue()).isEqualTo("Kermit");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetMultipleArrayEntries() {
    SpinList<SpinJsonNode> nodeList = script.getVariable("nodeList");

    assertThat(nodeList).hasSize(2);
    assertThat(nodeList.get(0).prop("name").stringValue()).isEqualTo("Kermit");
    assertThat(nodeList.get(1).prop("name").stringValue()).isEqualTo("Waldo");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldGetFilteredResult() {
    SpinList<SpinJsonNode> nodeList = script.getVariable("emptyList");

    assertThat(nodeList.size()).isEqualTo(0);

    SpinList<SpinJsonNode> nodeList2 = script.getVariable("nodeList");

    assertThat(nodeList2.size()).isEqualTo(1);
    assertThat(nodeList2.get(0).prop("name").stringValue()).isEqualTo("Waldo");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void souldGetMultipleArrayPropertyValues() {
    SpinList<SpinJsonNode> nodeList = script.getVariable("nodeList");

    assertThat(nodeList).hasSize(3);
    assertThat(nodeList.get(0).stringValue()).isEqualTo("Kermit");
    assertThat(nodeList.get(1).stringValue()).isEqualTo("Waldo");
    assertThat(nodeList.get(2).stringValue()).isEqualTo("Johnny");
  }

  @Test(expected = SpinJsonPathException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadingJsonPath() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonPathException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailAccessNonExistentProperty() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadingElementList() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadingString() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadingNumber() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadingBoolean() throws Throwable{
    failingWithException();
  }
}

