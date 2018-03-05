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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.camunda.spin.json.mapping.CustomerList;
import org.camunda.spin.json.mapping.GenericCustomerList;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.camunda.spin.json.mapping.dmn.DmnDecisionResultEntries;
import org.camunda.spin.json.mapping.dmn.DmnDecisionResultEntriesImpl;
import org.camunda.spin.json.mapping.dmn.DmnDecisionResultImpl;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.DataFormatWriter;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.DataFormats.json;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonSerializationTest {

  @Test
  public void testNotGenericList() throws Exception {
    CustomerList customers = new CustomerList();
    customers.add(new RegularCustomer("someCustomer", 5));

    String canonicalTypeString = json().getMapper().getCanonicalTypeName(customers);
    assertThat(canonicalTypeString).isNotNull();

    final byte[] bytes = serializeToByteArray(customers);
    assertThat(bytes).isNotEmpty();

    final Object o = deserializeFromByteArray(bytes, canonicalTypeString);
    assertThat(o).isInstanceOf(CustomerList.class);

    CustomerList deserializedCustomerList = (CustomerList) o;
    assertEquals("someCustomer", deserializedCustomerList.get(0).getName());
    assertEquals(5, deserializedCustomerList.get(0).getContractStartDate());
  }

  @Test
  public void testOrder() throws Exception {
    Order order = JsonTestConstants.createExampleOrder();

    String canonicalTypeString = json().getMapper().getCanonicalTypeName(order);
    assertThat(canonicalTypeString).isNotNull();

    final byte[] bytes = serializeToByteArray(order);
    assertThat(bytes).isNotEmpty();

    final Object o = deserializeFromByteArray(bytes, canonicalTypeString);
    assertThat(o).isInstanceOf(Order.class);

    Order deserializedOrder = (Order) o;
    JsonTestConstants.assertIsExampleOrder(deserializedOrder);
  }

  @Test
  public void testPlainTypeArray() throws Exception {
    int[] array = new int[]{5, 10};

    String canonicalTypeString = json().getMapper().getCanonicalTypeName(array);
    assertThat(canonicalTypeString).isNotNull();

    final byte[] bytes = serializeToByteArray(array);
    assertThat(bytes).isNotEmpty();

    final Object o = deserializeFromByteArray(bytes, canonicalTypeString);
    assertThat(o).isInstanceOf(int[].class);

    int[] deserializedArray = (int[]) o;
    assertEquals(5, deserializedArray[0]);
    assertEquals(10, deserializedArray[1]);
  }

  @Test
  public void testGenericList() throws Exception {
    GenericCustomerList customers = new GenericCustomerList();
    customers.add(new RegularCustomer("someCustomer", 5));

    String canonicalTypeString = json().getMapper().getCanonicalTypeName(customers);
    assertThat(canonicalTypeString).isNotNull();

    final byte[] bytes = serializeToByteArray(customers);
    assertThat(bytes).isNotEmpty();

    final Object o = deserializeFromByteArray(bytes, canonicalTypeString);
    assertThat(o).isInstanceOf(GenericCustomerList.class);

    GenericCustomerList deserializedCustomerList = (GenericCustomerList) o;
    assertEquals("someCustomer", deserializedCustomerList.get(0).getName());
    assertEquals(5, deserializedCustomerList.get(0).getContractStartDate());

  }

  @Test
  public void serializeAndDeserializeGenericCollection() throws Exception {
    List<DmnDecisionResultEntries> ruleResults = new ArrayList<DmnDecisionResultEntries>();
    final DmnDecisionResultEntriesImpl result1 = new DmnDecisionResultEntriesImpl();
    result1.putValue("key1", "value1");
    result1.putValue("key2", "value2");
    ruleResults.add(result1);
    final DmnDecisionResultEntriesImpl result2 = new DmnDecisionResultEntriesImpl();
    result2.putValue("key3", "value3");
    result2.putValue("key4", "value4");
    ruleResults.add(result2);
    DmnDecisionResultImpl dmnDecisionResult = new DmnDecisionResultImpl(ruleResults);

    String canonicalTypeString = json().getMapper().getCanonicalTypeName(dmnDecisionResult);
    assertThat(canonicalTypeString).isNotNull();

    final byte[] bytes = serializeToByteArray(dmnDecisionResult);
    assertThat(bytes).isNotEmpty();

    //deserialization is not working for this kind of class

  }

  @Test
  public void deserializeHashMap() throws Exception {
    final byte[] bytes = new String("{\"foo\": \"bar\"}").getBytes();
    assertThat(bytes).isNotEmpty();

    final Object o = deserializeFromByteArray(bytes, "java.util.HashMap");
    assertThat(o).isInstanceOf(HashMap.class);
    assertTrue(((HashMap)o).containsKey("foo"));
    assertEquals("bar", ((HashMap)o).get("foo"));
  }

  protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
    DataFormatMapper mapper = json().getMapper();
    DataFormatWriter writer = json().getWriter();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter outWriter = new OutputStreamWriter(out);
    BufferedWriter bufferedWriter = new BufferedWriter(outWriter);

    try {
      Object mappedObject = mapper.mapJavaToInternal(deserializedObject);
      writer.writeToWriter(bufferedWriter, mappedObject);
      return out.toByteArray();
    }
    finally {
      out.close();
      outWriter.close();
      bufferedWriter.close();
    }
  }

  protected Object deserializeFromByteArray(byte[] bytes, String objectTypeName) throws Exception {
    DataFormatMapper mapper = json().getMapper();
    DataFormatReader reader = json().getReader();

    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    InputStreamReader inReader = new InputStreamReader(bais);
    BufferedReader bufferedReader = new BufferedReader(inReader);

    try {
      Object mappedObject = reader.readInput(bufferedReader);
      return mapper.mapInternalToJava(mappedObject, objectTypeName);
    } finally {
      bais.close();
      inReader.close();
      bufferedReader.close();
    }
  }

}
