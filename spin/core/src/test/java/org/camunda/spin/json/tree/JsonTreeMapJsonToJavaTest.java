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
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JACKSON_TYPE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;
import static org.camunda.spin.json.JsonTestConstants.assertIsExampleInvoice;
import static org.camunda.spin.json.JsonTestConstants.assertIsExampleOrder;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.mapping.DateObject;
import org.camunda.spin.json.mapping.Invoice;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonTreeMapJsonToJavaTest {

  @Test
  public void shouldMapJsonObjectToJavaObject() {
    Order order = JSON(EXAMPLE_JSON).mapTo(Order.class);
    assertIsExampleOrder(order);
  }

  @Test
  public void shouldFailMappingToMismatchingClass() {
    try {
      JSON(EXAMPLE_JSON).mapTo(RegularCustomer.class);
      fail("Expected SpinJsonTreeNodeException");
    } catch (SpinJsonTreeNodeException e) {
      // happy path
    }
  }

  @Test
  public void shouldMapByCanonicalString() {
    Order order = JSON(EXAMPLE_JSON).mapTo(Order.class.getCanonicalName());
    assertIsExampleOrder(order);
  }

  @Test
  public void shouldMapListByCanonicalString() throws JsonProcessingException {
    JavaType desiredType =
        TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Order.class);

    List<Order> orders = JSON(EXAMPLE_JSON_COLLECTION).mapTo(desiredType.toCanonical());

    assertThat(orders.size()).isEqualTo(1);
    assertIsExampleOrder(orders.get(0));
  }

  @Test
  public void shouldFailForMalformedTypeString() {
    try {
      JSON(EXAMPLE_JSON_COLLECTION).mapTo("rubbish");
      fail("Expected SpinJsonTreeNodeException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }
  }

  @Test
  public void shouldImportAndMapWithTypeData() {
    SpinJsonNode json = JSON(EXAMPLE_JACKSON_TYPE_JSON,
        jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done());

    Invoice invoice = json.mapTo(Invoice.class);
    assertIsExampleInvoice(invoice);
  }

  @Test
  public void shouldDisableDefaultTyping() {
    JsonJacksonTreeDataFormat dataFormat =
        jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done();

    try {
      JSON(EXAMPLE_JSON, dataFormat).mapTo(Order.class);
      fail("Expected SpinJsonTreeNodeException");
    } catch (SpinJsonTreeNodeException e) {
      // happy path
    }

    dataFormat.mapper().disableDefaultTyping();

    Order order = JSON(EXAMPLE_JSON, dataFormat).mapTo(Order.class);
    assertThat(order).isNotNull();
  }

  @Test
  public void shouldConfigDeserializationByMap() {
    Order order = JSON(EXAMPLE_JSON).mapTo(Order.class);
    assertThat(order.getDueUntil()).isNotInstanceOf(BigInteger.class);

    Map<String, Object> configuration = newMap(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS.name(), Boolean.TRUE);

    order = JSON(EXAMPLE_JSON, jsonTree().mapper().config(configuration).done()).mapTo(Order.class);
    assertThat(order.getDueUntil()).isInstanceOf(BigInteger.class);
  }

  @Test
  public void shouldConfigureDateFormatting() {
    String dateJson = "{\"date\": \"2012-10-10T10:20:42\"}";

    try {
      JSON(dateJson).mapTo(DateObject.class);
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonTreeNodeException e) {
      // happy path
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    DateObject dateObject =
        JSON(dateJson, jsonTree().mapper().dateFormat(dateFormat).done()).mapTo(DateObject.class);

    assertThat(dateObject).isNotNull();

    Calendar calendar = dateFormat.getCalendar();
    calendar.set(2012, 9, 10, 10, 20, 42);
    Date expectedDate = calendar.getTime();

    assertThat(dateObject.getDate()).isEqualToIgnoringMillis(expectedDate);

  }

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);

    return result;
  }

  // TODO test mapping with map configuration on Spin instantiation

}
