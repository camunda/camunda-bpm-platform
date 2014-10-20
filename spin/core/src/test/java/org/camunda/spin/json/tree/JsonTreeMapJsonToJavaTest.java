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
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;
import static org.camunda.spin.json.JsonTestConstants.assertIsExampleOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
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
    } catch (SpinJsonException e) {
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

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);

    return result;
  }

}
