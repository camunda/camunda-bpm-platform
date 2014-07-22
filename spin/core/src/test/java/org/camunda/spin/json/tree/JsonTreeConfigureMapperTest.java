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

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.mapping.Customer;
import org.camunda.spin.json.mapping.DateObject;
import org.camunda.spin.json.mapping.Invoice;
import org.camunda.spin.json.mapping.Order;
import org.camunda.spin.json.mapping.RegularCustomer;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class JsonTreeConfigureMapperTest {

  @Test
  public void shouldImportAndMapWithTypeData() {
    SpinJsonNode json = JSON(EXAMPLE_JACKSON_TYPE_JSON, 
        jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done());
    
    Invoice invoice = json.mapTo(Invoice.class);
    
    assertThat(invoice).isNotNull();
    assertThat(invoice.getAmount()).isEqualTo(2000);
    
    assertThat(invoice.getCustomer()).isNotNull();

    Customer customer = invoice.getCustomer();
    assertThat(customer).isInstanceOf(RegularCustomer.class);
    assertThat(customer.getName()).isEqualTo("Kermit");
    assertThat(customer.getContractStartDate()).isEqualTo(20130505);
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
  
  @Test
  public void shouldPassConfigurationToNewInstance() {
    DateFormat dateFormat = new SimpleDateFormat();
    
    JsonJacksonTreeDataFormat jsonDataFormat = new JsonJacksonTreeDataFormat();
    jsonDataFormat.mapper().config("aKey", "aValue");
    jsonDataFormat.mapper().dateFormat(dateFormat);
    jsonDataFormat.mapper().enableDefaultTyping(DefaultTyping.JAVA_LANG_OBJECT, As.PROPERTY);
    
    JsonJacksonTreeDataFormat jsonDataFormatInstance = 
        jsonDataFormat.newInstance().mapper().config("anotherKey", "anotherValue").done();
    
    assertThat(jsonDataFormat.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormat.mapper().getValue("anotherKey")).isNull();
    
    assertThat(jsonDataFormatInstance.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormatInstance.mapper().getValue("anotherKey")).isEqualTo("anotherValue");
    
    assertThat(jsonDataFormatInstance.mapper().getDateFormat()).isSameAs(dateFormat);
    assertThat(jsonDataFormatInstance.mapper().getDefaultTyping()).isEqualTo(DefaultTyping.JAVA_LANG_OBJECT);
    assertThat(jsonDataFormatInstance.mapper().getDefaultTypingFormat()).isEqualTo(As.PROPERTY);
  }
  
  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);
    
    return result;
  }
  
}
