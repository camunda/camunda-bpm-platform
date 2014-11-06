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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.createExampleOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.mapping.Order;
import org.junit.Test;

public class JsonTreeMapJavaToJsonTest {

  @Test
  public void shouldMapJavaObjectToJson() {
    Order exampleOrder = createExampleOrder();

    String json = JSON(exampleOrder).toString();

    assertThatJson(json).isEqualTo(EXAMPLE_JSON);
  }

  @Test
  public void shouldMapListToJson() {
    List<String> names = new ArrayList<String>();
    names.add("Waldo");
    names.add("Hugo");
    names.add("Kermit");

    String json = JSON(names).toString();

    String expectedJson = "[\"Waldo\", \"Hugo\", \"Kermit\"]";
    assertThatJson(json).isEqualTo(expectedJson);
  }

  @Test
  public void shouldMapArrayToJson() {
    String[] names = new String[] { "Waldo", "Hugo", "Kermit" };

    String json = JSON(names).toString();

    String expectedJson = "[\"Waldo\", \"Hugo\", \"Kermit\"]";
    assertThatJson(json).isEqualTo(expectedJson);
  }

  @Test
  public void shouldMapMapToJson() {
    Map<String, Object> javaMap = new HashMap<String, Object>();
    javaMap.put("aKey", "aValue");
    javaMap.put("anotherKey", 42);

    String json = JSON(javaMap).toString();

    String expectedJson = "{\"aKey\" : \"aValue\", \"anotherKey\" : 42}";
    assertThatJson(json).isEqualTo(expectedJson);
  }

  @Test
  public void shouldFailWithNull() {
    try {
      JSON(null).toString();
      fail("expected exception");
    } catch (IllegalArgumentException e) {
      // happy path
    }
  }

  @Test
  public void shouldMapPrimitiveBooleanToJson() {
    SpinJsonNode node = JSON(true);
    assertThat(node.isBoolean()).isTrue();
    assertThat(node.isValue()).isTrue();
    assertThat(node.boolValue()).isTrue();
  }

  @Test
  public void shouldMapPrimitiveNumberToJson() {
    SpinJsonNode node = JSON(42);
    assertThat(node.isNumber()).isTrue();
    assertThat(node.isValue()).isTrue();
    assertThat(node.numberValue()).isEqualTo(42);
  }

  @Test
  public void shouldMapListOfPrimitiveStrings() {
    List<String> inputList = new ArrayList<String>();
    inputList.add("Waldo");
    inputList.add("Hugo");
    inputList.add("Kermit");

    SpinJsonNode node = JSON(inputList);
    assertThat(node.isArray()).isTrue();

    SpinList<SpinJsonNode> elements = node.elements();
    assertThat(elements.get(0).stringValue()).isEqualTo("Waldo");
    assertThat(elements.get(1).stringValue()).isEqualTo("Hugo");
    assertThat(elements.get(2).stringValue()).isEqualTo("Kermit");
  }

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);

    return result;
  }
}
