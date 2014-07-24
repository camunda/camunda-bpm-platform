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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JACKSON_TYPE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.createExampleInvoice;
import static org.camunda.spin.json.JsonTestConstants.createExampleOrder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.camunda.spin.json.mapping.DateObject;
import org.camunda.spin.json.mapping.Invoice;
import org.camunda.spin.json.mapping.Order;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

public class JsonJacksonMapJavaToJsonTest {

  @Test
  public void shouldMapJavaObjectToJson() {
    Order exampleOrder = createExampleOrder();

    String json = JSON(exampleOrder).toString();

    assertThatJson(json).isEqualTo(EXAMPLE_JSON);
  }

  @Test
  public void shouldMapJavaObjectToJsonWithDefaultTypeInformation() {
    Invoice exampleInvoice = createExampleInvoice();

    String json = JSON(exampleInvoice,
        jsonTree().mapper().enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY).done())
        .toString();

    assertThatJson(json).isEqualTo(EXAMPLE_JACKSON_TYPE_JSON);
  }

  @Test
  public void shouldConfigureDateFormatting() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    DateObject dateObject = new DateObject();

    Calendar calendar = dateFormat.getCalendar();
    calendar.set(2012, 9, 10, 10, 20, 42);
    Date date = calendar.getTime();
    dateObject.setDate(date);

    String json = JSON(dateObject, jsonTree().mapper().dateFormat(dateFormat).done()).toString();

    String expectedJson = "{\"date\":\"2012-10-10T10:20:42\"}";

    assertThat(json).isEqualTo(expectedJson);
  }
}
