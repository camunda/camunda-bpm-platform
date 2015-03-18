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
package org.camunda.spin.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.camunda.spin.impl.util.SpinIoUtil;
import org.camunda.spin.xml.mapping.Customer;
import org.camunda.spin.xml.mapping.Order;
import org.camunda.spin.xml.mapping.OrderDetails;
import org.xml.sax.SAXException;

/**
 * @author Daniel Meyer
 *
 */
public class XmlTestConstants {

  public static final String EXAMPLE_NAMESPACE = "http://camunda.org/example";

  public static final String EXAMPLE_XML_FILE_NAME = "org/camunda/spin/xml/example.xml";

  public static final String EXAMPLE_VALIDATION_XSD_FILE_NAME = "org/camunda/spin/xml/validation_schema.xsd";
  public static final String EXAMPLE_VALIDATION_EXTENSION_XSD_FILE_NAME = "org/camunda/spin/xml/validation_schema_extension.xsd";

  public static final String EXAMPLE_VALIDATION_XML_FILE_NAME = "org/camunda/spin/xml/validation_example.xml";

  public static final String EXAMPLE_XML = SpinIoUtil.fileAsString(EXAMPLE_XML_FILE_NAME);

  public static final String EXAMPLE_VALIDATION_XML = SpinIoUtil.fileAsString(EXAMPLE_VALIDATION_XML_FILE_NAME);

  public static final String EXAMPLE_INVALID_XML = "<invalid";

  public static final String EXAMPLE_EMPTY_STRING = "";

  public static final String NON_EXISTING = "nonExisting";

  public static Reader exampleXmlFileAsReader() {
    return SpinIoUtil.classpathResourceAsReader(EXAMPLE_XML_FILE_NAME);
  }

  public static Reader exampleMainXsdFileAsStream() {
    return SpinIoUtil.classpathResourceAsReader(EXAMPLE_VALIDATION_XSD_FILE_NAME);
  }

  public static Reader exampleExtensionXsdFileAsStream() {
    return SpinIoUtil.classpathResourceAsReader(EXAMPLE_VALIDATION_EXTENSION_XSD_FILE_NAME);
  }

  public static Schema createSchema() {
    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    Source main = new StreamSource(exampleMainXsdFileAsStream());
    Source ex = new StreamSource(exampleExtensionXsdFileAsStream());

    // Lesson learned: first imported schema extensions then main xsd in array
    Source[] sources = new Source[] {ex, main};
    try {
      return sf.newSchema(sources);
    } catch (SAXException e) {
      return null;
    }
  }

  public static void assertIsExampleOrder(Order order) {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    assertThat(order.getOrder()).isEqualTo("order1");
    try {
      assertThat(order.getDueUntil()).isEqualTo(format.parse("20150112"));
    } catch (ParseException e) {
      //
    }

    List<Customer> customers = order.getCustomer();
    assertThat(customers).isNotNull();
    assertThat(customers.size()).isEqualTo(3);

    assertThat(customers).extracting("name", "contractStartDate")
      .contains(
        tuple("Kermit", 1354539722),
        tuple("Waldo", 1320325322),
        tuple("Johnny", 1286110922));
  }

  public static Order createExampleOrder() {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

    Date detailsDate = new Date();
    Date dueUntilDate = new Date();

    try {
      detailsDate = dateFormat2.parse("2015-04-04");
      dueUntilDate = dateFormat.parse("20150112");
    } catch (ParseException e) {
      // will fail if date is not set
    }

    Order order = new Order();
    OrderDetails details = new OrderDetails();
    Customer customer1 = new Customer();
    Customer customer2 = new Customer();
    Customer customer3 = new Customer();

    LinkedList<Customer> customers = new LinkedList<Customer>();
    customers.add(customer1);
    customers.add(customer2);
    customers.add(customer3);

    customer1.setId("customer1");
    customer1.setName("Kermit");
    customer1.setContractStartDate(1354539722);

    customer2.setId("customer2");
    customer2.setName("Waldo");
    customer2.setContractStartDate(1320325322);

    customer3.setId("customer3");
    customer3.setName("Johnny");
    customer3.setContractStartDate(1286110922);

    details.setDate(detailsDate);
    details.setId(1234567890L);
    details.setProduct("camunda BPM");

    order.setOrder("order1");
    order.setDueUntil(dueUntilDate);
    order.setOrderDetails(details);
    order.setCustomer(customers);

    return order;
  }

}
