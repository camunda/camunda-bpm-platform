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
package org.camunda.spin.json;

import org.camunda.spin.impl.util.SpinIoUtil;
import org.camunda.spin.json.mapping.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class JsonTestConstants {

  public static final String EXAMPLE_JSON_FILE_NAME = "org/camunda/spin/json/example.json";

  public static final String EXAMPLE_JSON = SpinIoUtil.fileAsString(EXAMPLE_JSON_FILE_NAME);

  public static final String EXAMPLE_JSON_COLLECTION = "[" + EXAMPLE_JSON + "]";

  public static final String EXAMPLE_INVALID_JSON = "{\"invalid\":";

  public static final String EXAMPLE_EMPTY_STRING = "";

  /**
   * A json file that can only be parsed when configuring Jackson correctly.
   */
  public static final String EXAMPLE_JACKSON_READ_CONFIGURATION_JSON_FILE_NAME = "org/camunda/spin/json/example_jackson.json";

  public static final String EXAMPLE_JACKSON_READ_CONFIGURATION_JSON = SpinIoUtil.fileAsString(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON_FILE_NAME);

  public static final String EXAMPLE_JACKSON_TYPE_JSON_FILE_NAME = "org/camunda/spin/json/example_jackson_types.json";

  public static final String EXAMPLE_JACKSON_TYPE_JSON = SpinIoUtil.fileAsString(EXAMPLE_JACKSON_TYPE_JSON_FILE_NAME);

  public static Order createExampleOrder() {
    Order order = new Order();
    order.setId(1234567890987654321L);
    order.setOrder("order1");
    order.setDueUntil(20150112);
    order.setActive(true);

    OrderDetails orderDetails = new OrderDetails();
    orderDetails.setArticle("camundaBPM");
    orderDetails.setPrice(32000.45);
    orderDetails.setRoundedPrice(32000);

    List<String> currencies = new ArrayList<String>();
    currencies.add("euro");
    currencies.add("dollar");
    orderDetails.setCurrencies(currencies);

    order.setOrderDetails(orderDetails);

    List<RegularCustomer> customers = new ArrayList<RegularCustomer>();

    customers.add(new RegularCustomer("Kermit", 1354539722));
    customers.add(new RegularCustomer("Waldo", 1320325322));
    customers.add(new RegularCustomer("Johnny", 1286110922));

    order.setCustomers(customers);

    return order;

  }

  public static void assertIsExampleOrder(Order order) {
    assertThat(order.getId()).isEqualTo(1234567890987654321L);
    assertThat(order.getOrder()).isEqualTo("order1");
    assertThat(order.getDueUntil()).isEqualTo(20150112);
    assertThat(order.isActive()).isTrue();

    OrderDetails orderDetails = order.getOrderDetails();
    assertThat(orderDetails).isNotNull();
    assertThat(orderDetails.getArticle()).isEqualTo("camundaBPM");
    assertThat(orderDetails.getPrice()).isBetween(32000.44449, 32000.45001);
    assertThat(orderDetails.getRoundedPrice()).isEqualTo(32000);
    assertThat(orderDetails.getCurrencies()).containsExactly("euro", "dollar");

    List<RegularCustomer> customers = order.getCustomers();
    assertThat(customers).isNotNull();
    assertThat(customers.size()).isEqualTo(3);

    assertThat(customers).extracting("name", "contractStartDate")
    .contains(
        tuple("Kermit", 1354539722),
        tuple("Waldo", 1320325322),
        tuple("Johnny", 1286110922));
  }

  public static Invoice createExampleInvoice() {
    Invoice invoice = new Invoice();
    invoice.setAmount(2000);

    RegularCustomer customer = new RegularCustomer("Kermit", 20130505);
    invoice.setCustomer(customer);

    return invoice;
  }

  public static void assertIsExampleInvoice(Invoice invoice) {
    assertThat(invoice).isNotNull();
    assertThat(invoice.getAmount()).isEqualTo(2000);

    assertThat(invoice.getCustomer()).isNotNull();

    Customer customer = invoice.getCustomer();
    assertThat(customer).isInstanceOf(RegularCustomer.class);
    assertThat(customer.getName()).isEqualTo("Kermit");
    assertThat(customer.getContractStartDate()).isEqualTo(20130505);
  }

}
