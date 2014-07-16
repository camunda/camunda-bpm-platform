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

import java.util.List;

import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
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
public class JsonTreeReadPropertyTest {

  private SpinJsonNode jsonNode;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
  }

  // FIXME: test missing for null property name and non existing property
  // FIXME: test missing for non container property elements() call

  @Test
  public void shouldReadProperty() {
    assertThat(jsonNode).isNotNull();

    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();
  }

  @Test
  public void shouldReadTextValue() {
    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();

    String value = property.value();

    assertThat(value)
      .isNotNull()
      .isEqualTo("order1");

  }

  @Test
  public void shouldReadNumberValue() {
    SpinJsonNode property = jsonNode.prop("dueUntil");
    SpinJsonNode property2 = jsonNode.prop("id");

    Number value = property.numberValue();
    Number value2 = property2.numberValue();

    assertThat(value)
      .isNotNull()
      .isEqualTo(20150112);
    assertThat(value2)
      .isNotNull()
      .isEqualTo(1234567890987654321L);
  }

  @Test
  public void shouldGetReadException() {
    SpinJsonNode property = jsonNode.prop("dueUntil");
    SpinJsonNode property2 = jsonNode.prop("active");
    SpinJsonNode property3 = jsonNode.prop("order");

    try {
      property.boolValue();
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // expected
    }

    try {
      property2.value();
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // expected
    }

    try {
      property3.numberValue();
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldReadBooleanValue() {
    SpinJsonNode property = jsonNode.prop("active");

    Boolean value = property.boolValue();

    assertThat(value)
      .isNotNull()
      .isEqualTo(true);
  }

  @Test
  public void shouldReadChildNode() {
    SpinJsonNode childNode = jsonNode.prop("orderDetails");

    assertThat(childNode).isNotNull();
  }

  @Test
  public void shouldReadChildNodeProperty() {
    // Object Node
    SpinJsonNode childNode = jsonNode.prop("orderDetails");
    SpinJsonNode property = childNode.prop("roundedPrice");
    Number propertyValue = property.numberValue();

    assertThat(property).isNotNull();
    assertThat(propertyValue)
      .isNotNull()
      .isEqualTo(32000);

    // Array Node
    SpinJsonNode childNode2 = childNode.prop("currencies");
    SpinList<SpinJsonNode> list = childNode2.elements();

    SpinJsonNode node1 = list.get(0);
    SpinJsonNode node2 = list.get(1);
    assertThat(node1.value()).isEqualTo("euro");
    assertThat(node2.value()).isEqualTo("dollar");
  }

  @Test
  public void shouldReadObjectInArrayChildNode() {
    // Object Node
    SpinJsonNode childNode = jsonNode.prop("customers");
    SpinList<SpinJsonNode> list = childNode.elements();

    SpinJsonNode node1 = list.get(0);
    SpinJsonNode node2 = list.get(1);

    SpinJsonNode customer1 = node1.prop("name");
    SpinJsonNode customer2 = node2.prop("name");

    assertThat(customer1.value()).isEqualTo("Kermit");
    assertThat(customer2.value()).isEqualTo("Waldo");
  }

  @Test
  public void shouldReadListOfNodes() {
    List<String> names = jsonNode.fieldNames();

    assertThat(names).contains("order", "dueUntil", "orderDetails");
  }

}
