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
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JACKSON_TYPE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;
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
import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.mapping.DateObject;
import org.camunda.spin.json.mapping.Invoice;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public abstract class JsonTreeMapJsonToJavaScriptTest extends ScriptTest {

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToType",
    execute = false
  )
  @ScriptVariable(name = "input", file=EXAMPLE_JSON_FILE_NAME)
  public void shouldMapJsonObjectToJavaObject() {
    Map<String, Object> variables = newMap("mapToType", Order.class);
    Order order = script.execute(variables).getVariable("result");
    assertIsExampleOrder(order);
  }

  @Test(expected = SpinJsonTreeNodeException.class)
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToType",
    execute = false
  )
  @ScriptVariable(name = "input", file=EXAMPLE_JSON_FILE_NAME)
  public void shouldFailMappingToMismatchingClass() throws Throwable {
    Map<String, Object> variables = newMap("mapToType", RegularCustomer.class);
    failingWithException(variables);
  }

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToType",
    execute = false
  )
  @ScriptVariable(name = "input", file=EXAMPLE_JSON_FILE_NAME)
  public void shouldMapByCanonicalString() {
    Map<String, Object> variables = newMap("mapToType", Order.class.getCanonicalName());
    Order order = script.execute(variables).getVariable("result");
    assertIsExampleOrder(order);
  }

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToCollection",
    execute = false
  )
  public void shouldMapListByCanonicalString() throws JsonProcessingException {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", EXAMPLE_JSON_COLLECTION);
    variables.put("collectionType", ArrayList.class);
    variables.put("mapToType", Order.class);

    List<Order> orders = script.execute(variables).getVariable("result");

    assertThat(orders.size()).isEqualTo(1);
    assertIsExampleOrder(orders.get(0));
  }

  @Test(expected = SpinJsonDataFormatException.class)
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToType",
    variables = {
      @ScriptVariable(name = "input", file=EXAMPLE_JSON_FILE_NAME),
      @ScriptVariable(name = "mapToType", value = "rubbish")
    },
    execute = false
  )
  public void shouldFailForMalformedTypeString() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToTypeByDataFormat",
    execute = false
  )
  public void shouldImportAndMapWithTypeData() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", EXAMPLE_JACKSON_TYPE_JSON);
    variables.put("dataFormat", jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done());
    variables.put("mapToType", Invoice.class);

    Invoice invoice = script.execute(variables).getVariable("result");
    assertIsExampleInvoice(invoice);
  }

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToTypeByDataFormat",
    execute = false
  )
  public void shouldDisableDefaultTyping() {
    JsonJacksonTreeDataFormat dataFormat = jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input", EXAMPLE_JSON);
    variables.put("dataFormat", dataFormat);
    variables.put("mapToType", Order.class);
    try {
      failingWithException(variables);
      fail("Expected SpinJsonTreeNodeException");
    } catch (Throwable throwable) {
      assertThat(throwable).isInstanceOf(SpinJsonTreeNodeException.class);
    }


    dataFormat.mapper().disableDefaultTyping();
    variables.put("dataFormat", dataFormat);

    Order order = script.execute(variables).getVariable("result");
    assertThat(order).isNotNull();
  }

  @Test
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldConfigDeserializationByMap() {
    Map<String, Object> variables = newMap("mapToType", Order.class);

    Order order = script.execute(variables).getVariable("result");
    assertThat(order.getDueUntil()).isInstanceOf(BigInteger.class);
  }

  @Test
  @Script(
    name = "JsonTreeMapJsonToJavaScriptTest.mapToTypeByDataFormat",
    execute = false
  )
  @ScriptVariable(name = "input", value = "{\"date\": \"2012-10-10T10:20:42\"}")
  public void shouldConfigureDateFormatting() {
    JsonJacksonTreeDataFormat dataFormat = jsonTree();
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("dataFormat", dataFormat);
    variables.put("mapToType", DateObject.class);
    try {
      failingWithException(variables);
      fail("Expected SpinJsonDataFormatException");
    } catch (Throwable throwable) {
      assertThat(throwable).isInstanceOf(SpinJsonTreeNodeException.class);
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dataFormat = dataFormat.mapper().dateFormat(dateFormat).done();
    variables.put("dataFormat", dataFormat);
    DateObject dateObject = script.execute(variables).getVariable("result");

    assertThat(dateObject).isNotNull();

    Calendar calendar = dateFormat.getCalendar();
    calendar.set(2012, Calendar.OCTOBER, 10, 10, 20, 42);
    Date expectedDate = calendar.getTime();

    assertThat(dateObject.getDate()).isEqualToIgnoringMillis(expectedDate);

  }

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);

    return result;
  }

}
