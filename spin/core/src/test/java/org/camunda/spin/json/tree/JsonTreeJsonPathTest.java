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
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreePathException;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

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
  public void getElementFromJsonPath() {
    SpinJsonNode node = jsonNode.jsonPath("$.orderDetails").element();

    assertThat(node.isObject()).isTrue();
    assertThat(node.prop("article").isString()).isTrue();
  }

  @Test
  public void getElementListFromJsonPath() {
    SpinList<SpinJsonNode> node = jsonNode.jsonPath("$.customers").elementList();

    assertThat(node.get(0).isObject()).isTrue();
    assertThat(node.get(0).prop("name").isString()).isTrue();
  }

  @Test
  public void getBooleanFromJsonPath() {
    Boolean active = jsonNode.jsonPath("$.active").bool();

    assertThat(active).isTrue();
  }

  @Test
  public void getStringFromJsonPath() {
    String order = jsonNode.jsonPath("$.order").string();

    assertThat(order).isEqualTo("order1");
  }

  @Test
  public void getNumberFromJsonPath() {
    Number order = jsonNode.jsonPath("$.id").number();

    assertThat(order.longValue()).isEqualTo(1234567890987654321L);
  }

  @Test
  public void getSingleArrayEntry() {
    SpinJsonNode node = jsonNode.jsonPath("$.customers[0]").element();

    assertThat(node.isObject());
    assertThat(node.prop("name").isString());
    assertThat(node.prop("name").stringValue()).isEqualTo("Kermit");
  }

  @Test
  public void getMultipleArrayEntries() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[0:2]").elementList();

    assertThat(nodeList.get(0).prop("name").isString()).isTrue();
    assertThat(nodeList.get(1).prop("contractStartDate").isNumber()).isTrue();
  }

  @Test
  public void getFilteredResult() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[?(@.name == 'Klo')]").elementList();

    assertThat(nodeList.size()).isEqualTo(0);

    nodeList = jsonNode.jsonPath("$.customers[?(@.name == 'Waldo')]").elementList();

    assertThat(nodeList.size()).isEqualTo(1);
    assertThat(nodeList.get(0).prop("name").stringValue()).isEqualTo("Waldo");
  }

  @Test
  public void getMultipleArrayPropertyValues() {
    SpinList<SpinJsonNode> nodeList = jsonNode.jsonPath("$.customers[*].name").elementList();

    assertThat(nodeList.get(0).stringValue()).isEqualTo("Kermit");
    assertThat(nodeList.get(2).stringValue()).isEqualTo("Johnny");
  }

  @Test
  public void failReadingJsonPath() {
    try {
      jsonNode.jsonPath("$.....").element();
      fail("Expected: SpinJsonTreePathException");
    } catch(SpinJsonTreePathException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("").element();
      fail("Expected: SpinJsonTreePathException");
    } catch(SpinJsonTreePathException ex) {
      // expected
    }
  }

  @Test
  public void failAccessNonExistentProperty() {
    try {
      SpinJsonNode node = jsonNode.jsonPath("$.order.test").element();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonTreePathException ex) {
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
      jsonNode.jsonPath("$.customers").string();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.active").string();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.id").string();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").string();
      fail("Expected: SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test
   public void failReadingNumberProperty() {
    try {
      jsonNode.jsonPath("$.customers").number();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.active").number();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.order").number();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").number();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }
  }

  @Test
  public void failReadingBooleanProperty() {
    try {
      jsonNode.jsonPath("$.customers").bool();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.id").bool();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$.order").bool();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }

    try {
      jsonNode.jsonPath("$").bool();
      fail("Expected: SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException ex) {
      // expected
    }
  }
}
