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

import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreePropertyException;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

/**
 * @author Stefan Hentschel
 */
public class JsonTreeSetPropertyTest {

  protected SpinJsonNode jsonNode;
  protected SpinJsonNode order;
  protected SpinJsonNode dueUntil;
  protected SpinJsonNode id;
  protected SpinJsonNode customers;
  protected SpinJsonNode orderDetails;
  protected SpinJsonNode active;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
    order = jsonNode.prop("order");
    dueUntil = jsonNode.prop("dueUntil");
    id = jsonNode.prop("id");
    customers = jsonNode.prop("customers");
    orderDetails = jsonNode.prop("orderDetails");
    active = jsonNode.prop("active");
  }

  @Test
  public void setStringProperty() {
    String newComment = "42!";

    jsonNode.prop("comment", newComment);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isString()).isTrue();
    assertThat(comment.stringValue()).isEqualTo(newComment);
  }

  @Test
  public void replaceStringProperty() {
    String value = "new Order";
    String oldValue = order.stringValue();

    assertThat(order.isString()).isTrue();
    assertThat(customers.isArray()).isTrue();

    // set new values
    jsonNode.prop("order", value);
    jsonNode.prop("customers", value);
    String newValue = jsonNode.prop("order").stringValue();
    SpinJsonNode newValue2 = jsonNode.prop("customers");

    assertThat(newValue).isNotEqualTo(oldValue);
    assertThat(newValue).isEqualTo(value);

    assertThat(newValue2.isArray()).isFalse();
    assertThat(newValue2.stringValue()).isEqualTo(value);
  }

  @Test
  public void setIntegerProperty() {
    Integer newComment = 42;
    jsonNode.prop("comment", newComment);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isNumber()).isTrue();
    assertThat(comment.numberValue()).isEqualTo(newComment);
  }

  @Test
  public void replaceIntegerProperty() {
    Integer value = 42;
    Integer oldValue = dueUntil.numberValue().intValue();

    assertThat(customers.isArray()).isTrue();
    assertThat(dueUntil.isNumber());

    // set new values
    jsonNode.prop("dueUntil", value);
    jsonNode.prop("customers", value);
    SpinJsonNode newValue1 = jsonNode.prop("dueUntil");
    SpinJsonNode newValue2 = jsonNode.prop("customers");

    assertThat(newValue1.numberValue()).isNotEqualTo(oldValue);

    assertThat(newValue2.isArray()).isFalse();
    assertThat(newValue2.isNumber()).isTrue();
    assertThat(newValue2.numberValue()).isEqualTo(value);
  }

  @Test
  public void setFloatProperty() {
    Float floatValue = 42.00F;
    jsonNode.prop("comment", floatValue);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isNumber()).isTrue();
    assertThat(comment.numberValue()).isEqualTo(floatValue);
  }

  @Test
  public void replaceFloatProperty() {
    SpinJsonNode price = orderDetails.prop("price");
    Float value = 42.00F;

    Float oldValue = price.numberValue().floatValue();
    SpinList list = customers.elements();

    assertThat(customers.isArray()).isTrue();
    assertThat(price.isNumber());

    // set new values
    orderDetails.prop("price", value);
    jsonNode.prop("customers", value);
    SpinJsonNode newValue1 = orderDetails.prop("price");
    SpinJsonNode newValue2 = jsonNode.prop("customers");

    assertThat(newValue1.numberValue()).isNotEqualTo(oldValue);

    assertThat(newValue2.isArray()).isFalse();
    assertThat(newValue2.isNumber()).isTrue();
    assertThat(newValue2.numberValue()).isEqualTo(value);
  }

  @Test
  public void setLongProperty() {
    Long longValue = 4200000000L;
    jsonNode.prop("comment", longValue);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isNumber()).isTrue();
    assertThat(comment.numberValue().longValue()).isEqualTo(longValue);
  }

  @Test
  public void replaceLongProperty() {
    Long value = 4200000000L;

    Long oldValue = id.numberValue().longValue();
    SpinList list = customers.elements();

    assertThat(customers.isArray()).isTrue();
    assertThat(id.isNumber());

    // set new values
    jsonNode.prop("id", value);
    jsonNode.prop("customers", value);
    SpinJsonNode newValue1 = jsonNode.prop("id");
    SpinJsonNode newValue2 = jsonNode.prop("customers");

    assertThat(newValue1.numberValue()).isNotEqualTo(oldValue);

    assertThat(newValue2.isArray()).isFalse();
    assertThat(newValue2.isNumber()).isTrue();
    assertThat(newValue2.numberValue()).isEqualTo(value);
  }

  @Test
  public void setBooleanProperty() {
    jsonNode.prop("comment", true);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isBoolean()).isTrue();
    assertThat(comment.boolValue()).isTrue();
  }

  @Test
  public void replaceBooleanProperty() {
    SpinJsonNode active = jsonNode.prop("active");

    Boolean oldValue = active.boolValue();

    assertThat(customers.isArray()).isTrue();
    assertThat(active.isBoolean());

    // set new values
    jsonNode.prop("active", false);
    jsonNode.prop("customers", true);
    SpinJsonNode newValue1 = jsonNode.prop("active");
    SpinJsonNode newValue2 = jsonNode.prop("customers");

    assertThat(newValue1.boolValue()).isNotEqualTo(oldValue);

    assertThat(newValue2.isArray()).isFalse();
    assertThat(newValue2.isBoolean()).isTrue();
    assertThat(newValue2.boolValue()).isTrue();
  }

  @Test
  public void setArrayProperty() {
    ArrayList<Object> list1 = new ArrayList<Object>();
    ArrayList<Object> list2 = new ArrayList<Object>();
    Map<String, Object> map = new HashMap<String, Object>();

    map.put("id1", "object1");
    map.put("id2", 1337);

    list2.add("n");
    list2.add(32);

    list1.add("test");
    list1.add(42);
    list1.add(list2);
    list1.add(map);


    jsonNode.prop("comment", list1);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isArray()).isTrue();
    assertThat(comment.elements()).hasSize(4);
    assertThat(comment.elements().get(0).stringValue()).isEqualTo("test");
    assertThat(comment.elements().get(2).elements().get(1).numberValue()).isEqualTo(32);
    assertThat(comment.elements().get(3).isObject()).isTrue();
    assertThat(comment.elements().get(3).hasProp("id2")).isTrue();
  }

  @Test
  public void replaceArrayProperty() {

    assertThat(customers.isArray()).isTrue();
    assertThat(customers.elements()).hasSize(3);
    assertThat(active.isBoolean()).isTrue();
    assertThat(active.boolValue()).isTrue();

    // Build new values
    ArrayList<Object> list1 = new ArrayList<Object>();
    ArrayList<Object> list2 = new ArrayList<Object>();
    Map<String, Object> map = new HashMap<String, Object>();

    map.put("id1", "object1");
    map.put("id2", 1337);

    list2.add("n");
    list2.add(32);

    list1.add("test");
    list1.add(42);
    list1.add("filler");
    list1.add(list2);
    list1.add(map);

    jsonNode.prop("customers", list1);
    jsonNode.prop("active", list1);
    SpinJsonNode customers = jsonNode.prop("customers");
    SpinJsonNode active = jsonNode.prop("active");

    assertThat(customers.isArray()).isTrue();
    assertThat(customers.elements()).hasSize(5);
    assertThat(customers.elements().get(0).stringValue()).isEqualTo("test");
    assertThat(customers.elements().get(3).elements().get(1).numberValue()).isEqualTo(32);
    assertThat(customers.elements().get(4).isObject()).isTrue();
    assertThat(customers.elements().get(4).hasProp("id2")).isTrue();

    assertThat(active.isArray()).isTrue();
    assertThat(active.elements()).hasSize(5);
    assertThat(active.elements().get(0).stringValue()).isEqualTo("test");
    assertThat(active.elements().get(3).elements().get(1).numberValue()).isEqualTo(32);
    assertThat(active.elements().get(4).isObject()).isTrue();
    assertThat(active.elements().get(4).hasProp("id2")).isTrue();
  }

  @Test
  public void setObjectProperty() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id1", "object1");
    map.put("id2", 1337);

    jsonNode.prop("comment", map);
    SpinJsonNode comment = jsonNode.prop("comment");

    assertThat(comment.isObject()).isTrue();
    assertThat(comment.hasProp("id1")).isTrue();
    assertThat(comment.prop("id2").isNumber()).isTrue();
    assertThat(comment.prop("id2").numberValue()).isEqualTo(1337);
  }

  @Test
  public void replaceObjectProperty() {
    SpinJsonNode childNode = orderDetails;
    String oldValue = childNode.prop("article").stringValue();

    assertThat(oldValue).isEqualTo("camundaBPM");

    Map<String, Object> map = new HashMap<String, Object>();
    map.put("id1", "object1");
    map.put("id2", 1337);

    jsonNode.prop("orderDetails", map);
    SpinJsonNode comment = jsonNode.prop("orderDetails");

    assertThat(comment.isObject()).isTrue();
    assertThat(comment.hasProp("id1")).isTrue();
    assertThat(comment.prop("id2").isNumber()).isTrue();
    assertThat(comment.prop("id2").numberValue()).isEqualTo(1337);
  }

  @Test
  public void setPropertyWithJSON() {
    String json = "{\"agent\":\"Smith\"}";

    jsonNode.prop("name", JSON(json));
    assertThat(jsonNode.prop("name").isObject()).isTrue();
    assertThat(jsonNode.prop("name").prop("agent").stringValue()).isEqualTo("Smith");
  }

  @Test
  public void replacePropertyWithJSON() {
    String json = "{\"agent\":\"Smith\"}";

    assertThat(active.isBoolean()).isTrue();

    jsonNode.prop("active", JSON(json));
    assertThat(jsonNode.prop("active").isBoolean()).isFalse();
    assertThat(jsonNode.prop("active").isObject()).isTrue();
    assertThat(jsonNode.prop("active").prop("agent").stringValue()).isEqualTo("Smith");
  }

  @Test
  public void failWhileSettingObjectWithMap() {
    Date date = new Date();
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("date", date);

    try {
      jsonNode.prop("test",map);
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonTreePropertyException e) {
      // expected
    }
  }

  @Test
  public void failWhileSettingObjectWithList() {
    Date date = new Date();
    ArrayList<Object> list = new ArrayList<Object>();
    list.add(date);

    try {
      jsonNode.prop("test",list);
      fail("Expected SpinJsonTreePropertyException");
    } catch(SpinJsonTreePropertyException e) {
      // expected
    }
  }
}
