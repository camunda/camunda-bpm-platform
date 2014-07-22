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
import static org.assertj.core.api.Assertions.tuple;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;

import java.util.ArrayList;
import java.util.List;

import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.OrderDetails;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JsonJacksonTreeNodeMappingTest {
  
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

  protected void assertIsExampleOrder(Order order) {
    assertThat(order.getId()).isEqualTo(1234567890987654321L);
    assertThat(order.getOrder()).isEqualTo("order1");
    assertThat(order.getDueUntil()).isEqualTo(20150112);
    assertThat(order.isActive()).isTrue();
    
    OrderDetails orderDetails = order.getOrderDetails();
    assertThat(orderDetails).isNotNull();
    assertThat(orderDetails.getArticle()).isEqualTo("camundaBPM");
    assertThat(orderDetails.getPrice()).isBetween(32000.44449, 32000.45001);
    assertThat(orderDetails.getRoundedPrice()).isEqualTo(32000);
    
    List<RegularCustomer> customers = order.getCustomers();
    assertThat(customers).isNotNull();
    assertThat(customers.size()).isEqualTo(3);
    
    assertThat(customers).extracting("name", "contractStartDate")
    .contains(
        tuple("Kermit", 1354539722),
        tuple("Waldo", 1320325322),
        tuple("Johnny", 1286110922));
  }
  
}
