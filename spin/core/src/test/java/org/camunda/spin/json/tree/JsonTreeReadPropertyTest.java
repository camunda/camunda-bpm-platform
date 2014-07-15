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
import org.camunda.spin.impl.json.tree.JsonJacksonTreeLogger;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

/**
 * @author Stefan Hentschel
 */
public class JsonTreeReadPropertyTest {

  private static final JsonJacksonTreeLogger LOG = SpinLogger.JSON_TREE_LOGGER;

  @Test
  public void shouldReadProperty() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    assertThat(jsonNode).isNotNull();

    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();
  }

  @Test
  public void shouldReadTextValue() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);

    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();

    String value = property.value();

    assertThat(value).isNotNull();
    assertThat(value).isInstanceOf(String.class);
    assertThat(value).isEqualTo("order1");

  }

  @Test
  public void shouldReadNumberValue() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    SpinJsonNode property = jsonNode.prop("dueUntil");
    SpinJsonNode property2 = jsonNode.prop("id");

    Number value = property.numberValue();
    Number value2 = property2.numberValue();

    assertThat(value).isInstanceOf(Number.class);
    assertThat(value2).isInstanceOf(Number.class);

    assertThat(value).isNotNull();
    assertThat(value2).isNotNull();


    assertThat(value).isEqualTo(20150112);
    assertThat(value2).isEqualTo(1234567890987654321L);
  }

  @Test
  public void shouldGetReadException() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    SpinJsonNode property = jsonNode.prop("dueUntil");
    SpinJsonNode property2 = jsonNode.prop("active");
    SpinJsonNode property3 = jsonNode.prop("order");
    try {
      property.boolValue();
      property2.value();
      property2.numberValue();
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldReadBooleanValue() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    SpinJsonNode property = jsonNode.prop("active");

    Boolean value = property.boolValue();

    assertThat(value).isInstanceOf(Boolean.class);
    assertThat(value).isNotNull();
    assertThat(value).isEqualTo(true);
  }

  @Test
  public void shouldReadChildNode() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    SpinJsonNode childNode = jsonNode.prop("orderDetails");
    assertThat(childNode).isInstanceOf(SpinJsonNode.class);
  }

  @Test
  public void shouldReadChildNodeProperty() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);

    // Object Node
    SpinJsonNode childNode = jsonNode.prop("orderDetails");
    SpinJsonNode property = childNode.prop("roundedPrice");
    Number propertyValue = property.numberValue();

    assertThat(property).isNotNull();
    assertThat(propertyValue).isNotNull();
    assertThat(propertyValue).isInstanceOf(Number.class);
    assertThat(propertyValue).isEqualTo(32000);

    // Array Node
    SpinJsonNode childNode2 = childNode.prop("currencies");
    SpinList list = childNode2.elements();

    SpinJsonNode node1 = (SpinJsonNode) list.get(0);
    SpinJsonNode node2 = (SpinJsonNode) list.get(1);
    assertThat(node1.value()).isEqualTo("euro");
    assertThat(node2.value()).isEqualTo("dollar");
  }

  @Test
  public void shouldReadObjectInArrayChildNode() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);

    // Object Node
    SpinJsonNode childNode = jsonNode.prop("customers");
    SpinList list = childNode.elements();

    SpinJsonNode node1 = (SpinJsonNode) list.get(0);
    SpinJsonNode node2 = (SpinJsonNode) list.get(1);

    SpinJsonNode customer1 = node1.prop("name");
    SpinJsonNode customer2 = node2.prop("name");

    assertThat(customer1.value()).isEqualTo("Kermit");
    assertThat(customer2.value()).isEqualTo("Waldo");
  }

  @Test
  public void shouldReadListOfNodes() {
    SpinJsonNode jsonNode = JSON(EXAMPLE_JSON);
    ArrayList<String> names = jsonNode.fieldNames();

    assertThat(names.get(0)).isEqualTo("order");
    assertThat(names.get(1)).isEqualTo("dueUntil");
    assertThat(names.get(4)).isEqualTo("orderDetails");
  }
}
