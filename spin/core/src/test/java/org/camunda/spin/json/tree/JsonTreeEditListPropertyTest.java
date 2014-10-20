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

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

/**
 * Index:
 * 1) indexOf
 * 2) lastIndexOf
 * 3) append
 * 4) insertAt
 * 5) insertBefore
 * 6) insertAfter
 * 7) remove
 * 8) removeLast
 * 9) removeAt
 *
 * @author Stefan Hentschel
 */
public class JsonTreeEditListPropertyTest {

  protected SpinJsonNode jsonNode;
  protected SpinJsonNode customers;
  protected SpinJsonNode currencies;

  @Before
  public void readJson() {
    jsonNode = JSON(EXAMPLE_JSON);
    customers = jsonNode.prop("customers");
    currencies = jsonNode.prop("orderDetails").prop("currencies");
  }

  // ----------------- 1) indexOf ----------------------

  @Test
  public void readIndexOfNonArray() {
    try {
      jsonNode.indexOf("n");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void readIndexOfWithoutSearchNode() {
    try {
      jsonNode.indexOf(null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void readIndexOfNonExistentValue() {
    try {
      customers.indexOf("n");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void readIndexOfExistentValue() {
    Integer i = currencies.indexOf("euro");

    assertThat(i).isEqualTo(0);
  }

  // ----------------- 2) lastIndexOf ----------------------

  @Test
  public void readLastIndexOfNonArray() {
    try {
      jsonNode.lastIndexOf("n");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void readLastIndexOfWithoutSearchNode() {
    try {
      jsonNode.lastIndexOf(null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void readLastIndexOfNonExistentValue() {
    try {
      customers.lastIndexOf("n");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void readLastIndexOfExistentValue() {
    Integer i = currencies.lastIndexOf("dollar");

    assertThat(i).isEqualTo(1);
  }

  // ----------------- 3) append ----------------------

  @Test
  public void appendNodeToArray() {
    Integer oldSize = customers.elements().size();
    customers.append("Testcustomer");
    Integer newSize = customers.elements().size();

    assertThat(oldSize).isNotEqualTo(newSize);
    assertThat(oldSize + 1).isEqualTo(newSize);
    assertThat(customers.elements().get(oldSize).isString()).isTrue();
    assertThat(customers.elements().get(oldSize).stringValue()).isEqualTo("Testcustomer");
  }

  @Test
  public void appendNodeToNonArray() {
    try {
      jsonNode.append("testcustomer");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      //expected
    }
  }

  @Test
  public void appendWrongNode() {
    try {
      jsonNode.append(new Date());
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void appendNullNode() {
    try {
      jsonNode.append(null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  // ----------------- 4) insertAt ----------------------

  @Test
  public void insertAtWithIndex() {
    Integer oldSize = currencies.elements().size();
    Integer oldPosition = currencies.indexOf("dollar");
    SpinJsonNode oldNode = currencies.elements().get(1);

    currencies.insertAt(1, "test1");

    Integer newSize = currencies.elements().size();
    Integer newPosition = currencies.indexOf("dollar");
    SpinJsonNode newNode = currencies.elements().get(1);

    assertThat(oldSize + 1).isEqualTo(newSize);
    assertThat(newNode.equals(oldNode)).isFalse();
    assertThat(newNode.stringValue()).isEqualTo("test1");
    assertThat(oldPosition + 1).isEqualTo(newPosition);
  }

  @Test
  public void insertAtNonArray() {
    try {
      jsonNode.insertAt(1, "test");
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void insertAtWithNegativeIndex() {
    Integer oldSize = currencies.elements().size();
    Integer oldPosition = currencies.indexOf("dollar");
    SpinJsonNode oldNode = currencies.elements().get(0);

    currencies.insertAt(-2, "test1");

    Integer newSize = currencies.elements().size();
    Integer newPosition = currencies.indexOf("dollar");
    SpinJsonNode newNode = currencies.elements().get(0);

    assertThat(oldSize + 1).isEqualTo(newSize);
    assertThat(newNode.equals(oldNode)).isFalse();
    assertThat(newNode.stringValue()).isEqualTo("test1");
    assertThat(oldPosition + 1).isEqualTo(newPosition);
  }

  @Test
  public void insertAtWithIndexOutOfBounds() {
    try {
      currencies.insertAt(6, "string");
      fail("Expected: IndexOutOfBoundsException");
    } catch(IndexOutOfBoundsException e) {
      // expected
    }
  }

  @Test
  public void insertAtWithNegativeIndexOutOfBounds() {
    try {
      currencies.insertAt(-6, "string");
      fail("Expected: IndexOutOfBoundsException");
    } catch(IndexOutOfBoundsException e) {
      // expected
    }
  }

  @Test
  public void insertAtWithWrongObject() {
    try {
      currencies.insertAt(1, new Date());
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void insertAtWithNullObject() {
    try {
      currencies.insertAt(1, null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  // ----------------- 5) insertBefore ----------------------

  @Test
  public void insertBeforeNonExistentSearchObject() {
    try {
      currencies.insertBefore(1, "test");
      fail("Expected: SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void insertBeforeWithNullAsSearchObject() {
    try {
      currencies.insertBefore(null, "test");
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void insertBeforeSearchObjectOnBeginning() {
    SpinJsonNode oldNode = currencies.elements().get(0);
    Integer size = currencies.elements().size();

    currencies.insertBefore("euro", "Test");

    SpinJsonNode newNode = currencies.elements().get(0);
    SpinJsonNode oldNodeNewPosition = currencies.elements().get(1);
    Integer newSize = currencies.elements().size();

    assertThat(oldNode.equals(newNode)).isFalse();
    assertThat(newNode.stringValue()).isEqualTo("Test");
    assertThat(oldNode.stringValue()).isEqualTo(oldNodeNewPosition.stringValue());
    assertThat(size).isNotEqualTo(newSize);
  }

  @Test
  public void insertBeforeSearchObject() {
    SpinJsonNode oldNode = currencies.elements().get(1);
    Integer size = currencies.elements().size();

    currencies.insertBefore("dollar", "Test");

    SpinJsonNode newNode = currencies.elements().get(1);
    SpinJsonNode oldNodeNewPosition = currencies.elements().get(2);
    Integer newSize = currencies.elements().size();

    assertThat(oldNode).isNotEqualTo(newNode);
    assertThat(newNode.stringValue()).isEqualTo("Test");
    assertThat(oldNode.stringValue()).isEqualTo(oldNodeNewPosition.stringValue());
    assertThat(size).isNotEqualTo(newSize);
  }

  @Test
  public void insertNullObjectBeforeSearchObject() {
    try {
      currencies.insertBefore("euro", null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void insertWrongObjectBeforeSearchObject() {
    try {
      currencies.insertBefore("euro", new Date());
      fail("Expected: SpinJsonTreePropertyException");
    } catch(SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void insertObjectBeforeWrongSearchObject() {
    try {
      currencies.insertBefore(new Date(), "test");
      fail("Expected: SpinJsonTreePropertyException");
    } catch (SpinJsonPropertyException e) {
      // expected
    }
  }

  @Test
  public void insertBeforeOnNonArray() {
    try {
      jsonNode.insertBefore("test", "test");
      fail("Expected: SpinJsonTreeNodeException");
    } catch (SpinJsonException e) {
      // expected
    }
  }

  // ----------------- 6) insertAfter ----------------------

  @Test
  public void insertAfterNonExistentSearchObject() {
    try {
      currencies.insertBefore("test", "test");
      fail("Expected: SpinJsonTreeNodeException");
    } catch (SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void insertAfterSearchObjectOnEnding() {
    SpinJsonNode oldNode = currencies.elements().get(1);
    Integer size = currencies.elements().size();

    currencies.insertAfter("dollar", "Test");

    SpinJsonNode newNode = currencies.elements().get(2);
    SpinJsonNode oldNodeNewPosition = currencies.elements().get(1);
    Integer newSize = currencies.elements().size();

    assertThat(oldNode.equals(newNode)).isFalse();
    assertThat(oldNode.stringValue()).isEqualTo(oldNodeNewPosition.stringValue());
    assertThat(size).isNotEqualTo(newSize);
  }

  @Test
  public void insertAfterSearchObject() {
    SpinJsonNode oldNode = currencies.elements().get(0);
    Integer size = currencies.elements().size();

    currencies.insertAfter("dollar", "Test");

    SpinJsonNode newNode = currencies.elements().get(1);
    SpinJsonNode oldNodeNewPosition = currencies.elements().get(0);
    Integer newSize = currencies.elements().size();

    assertThat(oldNode.equals(newNode)).isFalse();
    assertThat(oldNode.stringValue()).isEqualTo(oldNodeNewPosition.stringValue());
    assertThat(size).isNotEqualTo(newSize);
  }

  @Test
  public void insertNullObjectAfterSearchObject() {
    try {
      currencies.insertBefore("euro", null);
      fail("Expected: IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void insertWrongObjectAfterSearchObject() {
    try {
      currencies.insertBefore("euro", new Date());
      fail("Expected: SpinJsonTreeNodeException");
    } catch (SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void insertAfterOnNonArray() {
    try {
      jsonNode.insertBefore("euro", "test");
      fail("Expected: SpinJsonTreeNodeException");
    } catch (SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void insertObjectAfterWrongSearchObject() {
    try {
      currencies.insertAfter(new Date(), "test");
      fail("Expected: SpinJsonTreePropertyException");
    } catch (SpinJsonPropertyException e) {
      // expected
    }
  }

  // ----------------- 7) remove ----------------------

  @Test
  public void removeObject() {
    Integer oldSize = currencies.elements().size();

    currencies.remove("euro");

    Integer newSize = currencies.elements().size();
    SpinJsonNode node = currencies.elements().get(0);

    assertThat(oldSize).isNotEqualTo(newSize);
    assertThat(oldSize - 1).isEqualTo(newSize);
    assertThat(node.stringValue()).isEqualTo("dollar");
  }

  @Test
  public void removeNonExistentObject() {
    try {
      currencies.remove("test");
      fail("Expected: SpinJsonTreePropertyException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void removeObjectInNonExistentArray() {
    try {
      jsonNode.remove("test");
      fail("Expected: SpinJsonTreePropertyException");
    } catch(SpinJsonException e) {
      // expected
    }
  }
  
  @Test
  public void removeNullObject() {
    try {
      jsonNode.remove(null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  // ----------------- 8) removeLast ----------------------

  @Test
  public void removeLastNullObject() {
    try {
      currencies.removeLast(null);
      fail("Expected: IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void removeLastWrongObject() {
    try {
      currencies.removeLast(new Date());
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void removeLast() {
    String testJson = "[\"test\",\"test\",\"new value\",\"test\"]";
    SpinJsonNode testNode = JSON(testJson);
    Integer size = testNode.elements().size();

    testNode.removeLast("test");

    Integer newSize = testNode.elements().size();
    SpinJsonNode node = testNode.elements().get(newSize - 1);

    assertThat(newSize).isNotEqualTo(size);
    assertThat(newSize + 1).isEqualTo(size);
    assertThat(node.stringValue()).isEqualTo("new value");
  }
  
  @Test
  public void removeLastNonExistentArray() {
    try {
      jsonNode.removeLast(1);
      fail("Expected: SpinJsonTreeNodeException ");
    } catch(SpinJsonException  e) {
      // expected
    }
  }

  // ----------------- 9) removeAt ----------------------

  @Test
  public void removeAtWithIndex() {
    Integer oldSize = currencies.elements().size();

    currencies.removeAt(1);

    Integer newSize = currencies.elements().size();
    SpinJsonNode node = currencies.elements().get(newSize - 1);

    assertThat(newSize).isEqualTo(1);
    assertThat(newSize + 1).isEqualTo(oldSize);
    assertThat(node.stringValue()).isEqualTo("euro");
  }

  @Test
  public void removeAtNonArray() {
    try {
      jsonNode.removeAt(1);
      fail("Expected: SpinJsonTreeNodeException");
    } catch(SpinJsonException e) {
      // expected
    }
  }

  @Test
  public void removeAtWithNegativeIndex() {
    Integer oldSize = currencies.elements().size();

    currencies.removeAt(-2);

    Integer newSize = currencies.elements().size();
    SpinJsonNode node = currencies.elements().get(newSize - 1);

    assertThat(newSize).isEqualTo(1);
    assertThat(newSize + 1).isEqualTo(oldSize);
    assertThat(node.stringValue()).isEqualTo("dollar");
  }

  @Test
  public void removeAtWithIndexOutOfBounds() {
    try {
      currencies.removeAt(6);
      fail("Expected: IndexOutOfBoundsException");
    } catch(IndexOutOfBoundsException e){
      // expected
    }
  }

  @Test
  public void removeAtWithNegativeIndexOutOfBounds() {
    try {
      currencies.removeAt(-6);
      fail("Expected: IndexOutOfBoundsException");
    } catch(IndexOutOfBoundsException e){
      // expected
    }
  }
}
