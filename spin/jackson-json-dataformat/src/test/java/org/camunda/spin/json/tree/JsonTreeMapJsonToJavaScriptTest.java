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
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;
import static org.camunda.spin.json.JsonTestConstants.assertIsExampleOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

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

  @Test(expected = SpinJsonException.class)
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

  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);

    return result;
  }

}
