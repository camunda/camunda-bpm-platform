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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPathException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Stefan Hentschel
 */
public class JsonTreeJsonPathTest {

  protected SpinJsonNode jsonNode;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
  }

  @Test
  public void shouldGetElementFromJsonPath() {
    SpinJsonNode node = jsonNode.jsonPath("$.orderDetails").element();

    assertThat(node.isObject()).isTrue();
    assertThat(node.prop("article").isString()).isTrue();
  }

  @Test
  public void shouldGetElementListFromJsonPath() {
    SpinList<SpinJsonNode> node = jsonNode.jsonPath("$.customers").elementList();

    assertThat(node).hasSize(3);
    assertThat(node.get(0).isObject()).isTrue();
    assertThat(node.get(0).prop("name").isString()).isTrue();
  }

  @Test
  public void shouldGetBooleanFromJsonPath() {
    Boolean active = jsonNode.jsonPath("$.active").boolValue();

    assertThat(active).isTrue();
  }

  @Test
  public void shouldGetStringFromJsonPath() {
    String order = jsonNode.jsonPath("$.order").stringValue();

    assertThat(order).isEqualTo("order1");
  }

  @Test
  public void shouldGetNumberFromJsonPath() {
    Number order = jsonNode.jsonPath("$.id").numberValue();

    assertThat(order.longValue()).isEqualTo(1234567890987654321L);
  }
  
  @Test
  public void shouldGetNullNode() {
	  SpinJsonNode node = jsonNode.jsonPath("$.nullValue").element();
	  assertThat(node.isNull()).isTrue();
  }

  @Test
  public void shouldGetSingleArrayEntry() {
    SpinJsonNode node = jsonNode.jsonPath("$.customers[0]").element();

    assertThat(node.isObject());
    assertThat(node.prop("name").isString());
    assertThat(node.prop("name").stringValue()).isEqualTo("Kermit");
  }

  @Test
  public void shouldGetMultipleArrayEntries() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[0:2]").elementList();

    assertThat(nodeList).hasSize(2);
    assertThat(nodeList.get(0).prop("name").stringValue()).isEqualTo("Kermit");
    assertThat(nodeList.get(1).prop("name").stringValue()).isEqualTo("Waldo");
  }

  @Test
  public void shouldGetFilteredResult() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[?(@.name == 'Klo')]").elementList();

    assertThat(nodeList.size()).isEqualTo(0);

    nodeList = jsonNode.jsonPath("$.customers[?(@.name == 'Waldo')]").elementList();

    assertThat(nodeList.size()).isEqualTo(1);
    assertThat(nodeList.get(0).prop("name").stringValue()).isEqualTo("Waldo");
  }

  @Test
  public void shouldGetMultipleArrayPropertyValues() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[*].name").elementList();

    assertThat(nodeList).hasSize(3);
    assertThat(nodeList.get(0).stringValue()).isEqualTo("Kermit");
    assertThat(nodeList.get(1).stringValue()).isEqualTo("Waldo");
    assertThat(nodeList.get(2).stringValue()).isEqualTo("Johnny");
  }

  @Test
  public void failReadingJsonPath() {
    try {
      jsonNode.jsonPath("$.....").element();
      fail("Expected: SpinJsonTreePathException");
    } catch(SpinJsonPathException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("").element();
      fail("Expected: SpinJsonTreePathException");
    } catch(SpinJsonPathException ex) {
      // expected
    }
  }

  @Test
  public void failAccessNonExistentProperty() {
    try {
      jsonNode.jsonPath("$.order.test").element();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonPathException ex) {
      // expected
    }
  }

  @Test
  public void failReadingElementList() {
    try {
      jsonNode.jsonPath("$.order").elementList();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.id").elementList();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.active").elementList();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").elementList();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test
  public void failReadingStringProperty() {
    try {
      jsonNode.jsonPath("$.customers").stringValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.active").stringValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.id").stringValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").stringValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test
   public void failReadingNumberProperty() {
    try {
      jsonNode.jsonPath("$.customers").numberValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.active").numberValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.order").numberValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").numberValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test
  public void failReadingBooleanProperty() {
    try {
      jsonNode.jsonPath("$.customers").boolValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.id").boolValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.order").boolValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").boolValue();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test(expected = SpinJsonPathException.class)
  public void failOnNonExistingJsonPath() {
    SpinJsonNode json = JSON("{\"a\": {\"id\": \"a\"}, \"b\": {\"id\": \"b\"}}");
    json.jsonPath("$.c?(@.id)").element();
  }

}
