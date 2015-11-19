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
import org.camunda.spin.impl.util.SpinIoUtil;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.camunda.spin.spi.SpinDataFormatException;
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

  protected SpinJsonNode jsonNode;
  protected SpinJsonNode order;
  protected SpinJsonNode dueUntil;
  protected SpinJsonNode id;
  protected SpinJsonNode customers;
  protected SpinJsonNode orderDetails;
  protected SpinJsonNode active;
  protected SpinJsonNode nullValue;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
    order = jsonNode.prop("order");
    dueUntil = jsonNode.prop("dueUntil");
    id = jsonNode.prop("id");
    customers = jsonNode.prop("customers");
    orderDetails = jsonNode.prop("orderDetails");
    active = jsonNode.prop("active");
    nullValue = jsonNode.prop("nullValue");
  }

  @Test
  public void checkForProperty() {
    assertThat(jsonNode.hasProp("order")).isTrue();
    assertThat(order.hasProp("order")).isFalse();
    assertThat(dueUntil.hasProp("order")).isFalse();
    assertThat(id.hasProp("order")).isFalse();
    assertThat(customers.hasProp("order")).isFalse();
    assertThat(orderDetails.hasProp("price")).isTrue();
    assertThat(active.hasProp("order")).isFalse();
    assertThat(jsonNode.hasProp("nullValue")).isTrue();
  }

  @Test
  public void shouldReadProperty() {
    assertThat(jsonNode).isNotNull();

    SpinJsonNode property = jsonNode.prop("order");
    assertThat(property).isNotNull();
  }

  @Test
  public void shouldFailToReadNonProperty() {
    try {
      jsonNode.prop("nonExisting");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }

    try {
      order.prop("nonExisting");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }

    try {
      customers.prop("nonExisting");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }

    try {
      jsonNode.prop(null);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      order.prop("null");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }

    try {
      customers.prop("null");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void checkForObjectValue() {
    assertThat(jsonNode.isObject()).isTrue();
    assertThat(order.isObject()).isFalse();
    assertThat(dueUntil.isObject()).isFalse();
    assertThat(id.isObject()).isFalse();
    assertThat(customers.isObject()).isFalse();
    assertThat(orderDetails.isObject()).isTrue();
    assertThat(active.isObject()).isFalse();
  }

  @Test
  public void shouldReadObjectProperty() {
    assertThat(jsonNode.prop("order")).isNotNull();
    assertThat(orderDetails.prop("article")).isNotNull();
  }

  @Test
  public void shouldFailToReadNonObject() {
    try {
      jsonNode.prop("order").prop("nonExisting");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }

    try {
      orderDetails.prop("roundedPrice").prop("nonExisting");
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void checkForStringValue() {
    assertThat(jsonNode.isString()).isFalse();
    assertThat(order.isString()).isTrue();
    assertThat(dueUntil.isString()).isFalse();
    assertThat(id.isString()).isFalse();
    assertThat(customers.isString()).isFalse();
    assertThat(orderDetails.isString()).isFalse();
    assertThat(active.isString()).isFalse();
  }

  @Test
  public void shouldReadStringValue() {
    assertThat(order.stringValue()).isEqualTo("order1");
    assertThat(orderDetails.prop("article").stringValue()).isEqualTo("camundaBPM");
    assertThat(customers.elements().get(0).prop("name").stringValue()).isEqualTo("Kermit");
  }

  @Test
  public void shouldFailToReadNonStringValue() {
    try {
      jsonNode.stringValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      dueUntil.stringValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      orderDetails.prop("currencies").stringValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void checkForNumberValue() {
    assertThat(jsonNode.isNumber()).isFalse();
    assertThat(order.isNumber()).isFalse();
    assertThat(dueUntil.isNumber()).isTrue();
    assertThat(id.isNumber()).isTrue();
    assertThat(customers.isNumber()).isFalse();
    assertThat(orderDetails.isNumber()).isFalse();
    assertThat(active.isNumber()).isFalse();
  }

  @Test
  public void shouldReadNumberValue() {
    assertThat(dueUntil.numberValue()).isEqualTo(20150112);
    assertThat(id.numberValue()).isEqualTo(1234567890987654321L);
  }

  @Test
  public void shouldFailToReadNonNumberValue() {
    try {
      jsonNode.numberValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      order.numberValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      customers.numberValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void checkForBooleanValue() {
    assertThat(jsonNode.isBoolean()).isFalse();
    assertThat(order.isBoolean()).isFalse();
    assertThat(dueUntil.isBoolean()).isFalse();
    assertThat(id.isBoolean()).isFalse();
    assertThat(customers.isBoolean()).isFalse();
    assertThat(orderDetails.isBoolean()).isFalse();
    assertThat(active.isBoolean()).isTrue();
  }

  @Test
  public void shouldReadBooleanValue() {
    assertThat(active.boolValue()).isTrue();
    assertThat(orderDetails.prop("paid").boolValue()).isFalse();
  }

  @Test
  public void shouldFailToReadNonBooleanValue() {
    try {
      jsonNode.boolValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      order.boolValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      customers.boolValue();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void checkForValue() {
    assertThat(jsonNode.isValue()).isFalse();
    assertThat(order.isValue()).isTrue();
    assertThat(dueUntil.isValue()).isTrue();
    assertThat(id.isValue()).isTrue();
    assertThat(customers.isValue()).isFalse();
    assertThat(orderDetails.isValue()).isFalse();
    assertThat(active.isValue()).isTrue();
  }

  @Test
  public void shouldReadValue() {
    assertThat(order.value())
      .isInstanceOf(String.class)
      .isEqualTo("order1");

    assertThat(dueUntil.value())
      .isInstanceOf(Number.class)
      .isEqualTo(20150112);

    assertThat(active.value())
      .isInstanceOf(Boolean.class)
      .isEqualTo(true);
  }

  @Test
  public void shouldFailToReadNonValue() {
    try {
      jsonNode.value();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      customers.value();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      orderDetails.value();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void checkForArrayValue() {
    assertThat(jsonNode.isArray()).isFalse();
    assertThat(order.isArray()).isFalse();
    assertThat(dueUntil.isArray()).isFalse();
    assertThat(id.isArray()).isFalse();
    assertThat(customers.isArray()).isTrue();
    assertThat(orderDetails.isArray()).isFalse();
    assertThat(active.isArray()).isFalse();
  }

  @Test
  public void shouldReadArrayValue() {
    SpinList<SpinJsonNode> customerElements = customers.elements();
    SpinList<SpinJsonNode> currenciesElements = orderDetails.prop("currencies").elements();

    assertThat(customerElements).hasSize(3);
    assertThat(currenciesElements).hasSize(2);

    assertThat(customerElements.get(0).prop("name").stringValue()).isEqualTo("Kermit");
    assertThat(currenciesElements.get(0).stringValue()).isEqualTo("euro");
  }

  @Test
  public void shouldFailToReadNonArrayValue() {
    try {
      jsonNode.elements();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      order.elements();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      id.elements();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void checkForNullValue() {
    assertThat(nullValue.isNull()).isTrue();
    assertThat(jsonNode.isNull()).isFalse();
    assertThat(order.isNull()).isFalse();
    assertThat(dueUntil.isNull()).isFalse();
    assertThat(id.isNull()).isFalse();
    assertThat(customers.isNull()).isFalse();
    assertThat(orderDetails.isNull()).isFalse();
    assertThat(active.isNull()).isFalse();
  }

  @Test
  public void shouldReadNullValue() {
    assertThat(nullValue.isValue()).isTrue();
    assertThat(nullValue.value()).isNull();
  }

  @Test
  public void shouldReadFieldNames() {
    assertThat(jsonNode.fieldNames()).contains("order", "dueUntil", "id", "customers", "orderDetails", "active");
    assertThat(customers.fieldNames()).isEmpty();
    assertThat(orderDetails.fieldNames()).contains("article", "price", "roundedPrice", "currencies", "paid");
  }

  @Test
  public void shouldFailToReadNonFieldNames() {
    try {
      order.fieldNames();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      dueUntil.fieldNames();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinJsonDataFormatException e) {
      // expected
    }

    try {
      active.fieldNames();
      fail("Expected SpinJsonDataFormatException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }


  /**
   * Tests an issue with Jackson 2.4.1
   *
   * The test contains a negative float at character position 8000 which is important
   * to provoke Jackson bug #146.
   * See also <a href="https://github.com/FasterXML/jackson-core/issues/146">the Jackson bug report</a>.
   */
  @Test
  public void shouldNotFailWithJackson146Bug() {
    // this should not fail
    SpinJsonNode node = JSON(SpinIoUtil.fileAsString("org/camunda/spin/json/jackson146.json"));

    // file has 4000 characters in length a
    // 20 characters per repeated JSON object
    assertThat(node.prop("abcdef").elements()).hasSize(200);
  }

}
