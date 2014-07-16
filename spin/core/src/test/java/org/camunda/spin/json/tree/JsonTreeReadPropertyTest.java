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
import org.camunda.spin.json.SpinJsonTreePropertyException;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

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

  @Test
  public void shouldReadProperty() {
    assertThat(jsonNode).isNotNull();

    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();
  }

  @Test
  public void shouldCheckStringValue() {
    SpinJsonNode property = jsonNode.prop("order");
    SpinJsonNode property2 = jsonNode.prop("id");
    SpinJsonNode property3 = jsonNode.prop("customers");
    SpinJsonNode property4 = jsonNode.prop("orderDetails");
    SpinJsonNode property5 = jsonNode.prop("active");

    assertThat(property.isString()).isEqualTo(true);
    assertThat(property2.isString()).isEqualTo(false);
    assertThat(property3.isString()).isEqualTo(false);
    assertThat(property4.isString()).isEqualTo(false);
    assertThat(property5.isString()).isEqualTo(false);
  }

  @Test
  public void shouldCheckNumberValue() {
      SpinJsonNode property = jsonNode.prop("order");
      SpinJsonNode property2 = jsonNode.prop("id");
      SpinJsonNode property3 = jsonNode.prop("customers");
      SpinJsonNode property4 = jsonNode.prop("orderDetails");
      SpinJsonNode property5 = jsonNode.prop("active");

      assertThat(property.isNumber()).isEqualTo(false);
      assertThat(property2.isNumber()).isEqualTo(true);
      assertThat(property3.isNumber()).isEqualTo(false);
      assertThat(property4.isNumber()).isEqualTo(false);
      assertThat(property5.isNumber()).isEqualTo(false);
  }

  @Test
  public void shouldCheckBooleanValue() {
    SpinJsonNode property = jsonNode.prop("order");
    SpinJsonNode property2 = jsonNode.prop("id");
    SpinJsonNode property3 = jsonNode.prop("customers");
    SpinJsonNode property4 = jsonNode.prop("orderDetails");
    SpinJsonNode property5 = jsonNode.prop("active");

    assertThat(property.isBoolean()).isEqualTo(false);
    assertThat(property2.isBoolean()).isEqualTo(false);
    assertThat(property3.isBoolean()).isEqualTo(false);
    assertThat(property4.isBoolean()).isEqualTo(false);
    assertThat(property5.isBoolean()).isEqualTo(true);
  }

  @Test
  public void shouldCheckArrayValue() {
    SpinJsonNode property = jsonNode.prop("order");
    SpinJsonNode property2 = jsonNode.prop("id");
    SpinJsonNode property3 = jsonNode.prop("customers");
    SpinJsonNode property4 = jsonNode.prop("orderDetails");
    SpinJsonNode property5 = jsonNode.prop("active");

    assertThat(property.isArray()).isEqualTo(false);
    assertThat(property2.isArray()).isEqualTo(false);
    assertThat(property3.isArray()).isEqualTo(true);
    assertThat(property4.isArray()).isEqualTo(false);
    assertThat(property5.isArray()).isEqualTo(false);
  }

  @Test
  public void shouldCheckObjectValue() {
    SpinJsonNode property = jsonNode.prop("order");

    assertThat(property.value()).isInstanceOf(Object.class);
    assertThat((String) property.value()).isEqualTo("order1");
  }

  @Test
  public void shouldFailToCheckObject() {
    SpinJsonNode property = jsonNode.prop("orderDetails");
    SpinJsonNode property2 = jsonNode.prop("customers");

    try {
      property.value();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      property2.value();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldFailToReadProperty() {
    try {
      jsonNode.prop("42");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonTreePropertyException e) {
      // expected
    }

    try {
      jsonNode.prop(null);
      fail("Exptected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void shouldReadTextValue() {
    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();

    String value = property.stringValue();

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
      property2.stringValue();
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
    assertThat(node1.stringValue()).isEqualTo("euro");
    assertThat(node2.stringValue()).isEqualTo("dollar");
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

    assertThat(customer1.stringValue()).isEqualTo("Kermit");
    assertThat(customer2.stringValue()).isEqualTo("Waldo");
  }

  @Test
  public void shouldFailToReadObjectInNonArray() {
    try {
      SpinJsonNode property = jsonNode.prop("order");
      property.elements();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldReadListOfNodes() {
    List<String> names = jsonNode.fieldNames();

    assertThat(names).contains("order", "dueUntil", "orderDetails");
  }

}
