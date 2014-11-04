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
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.junit.Test;

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
 *
 */
public abstract class JsonTreeEditListPropertyScriptTest extends ScriptTest {

  // ----------------- 1) indexOf ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadIndexOfNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadIndexOfWithoutSearchNode() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadIndexOfNonExistentValue() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReadIndexOf() {
    Number i = script.getVariable("value");

    // Casts to int because ruby returns long instead of int values!
    assertThat(i.intValue()).isEqualTo(1);
  }

  // ----------------- 2) lastIndexOf ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadLastIndexOfNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadLastIndexOfWithoutSearchNode() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailReadLastIndexOfNonExistentValue() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldReadLastIndexOf() {
    Number i = script.getVariable("value");

    // Casts to int because ruby returns long instead of int values!
    assertThat(i.intValue()).isEqualTo(1);
  }

  // ----------------- 3) append ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailAppendToNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailAppendWrongNode() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailAppendNullNode() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldAppendNodeToArray() {
    Number oldSize = script.getVariable("oldSize");
    Number newSize = script.getVariable("newSize");
    String value    = script.getVariable("value");

    // casts to int because ruby returns long instead of int values!
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(value).isEqualTo("Testcustomer");
  }

  // ----------------- 4) insertAt ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAtNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = IndexOutOfBoundsException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAtWithIndexOutOfBounds() throws Throwable {
    failingWithException();
  }

  @Test(expected = IndexOutOfBoundsException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAtWithNegativeIndexOutOfBounds() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAtWithWrongObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAtWithNullObject() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldInsertAtWithIndex() {
    Number oldSize     = script.getVariable("oldSize");
    Number oldPosition = script.getVariable("oldPosition");
    Number newSize     = script.getVariable("newSize");
    Number newPosition = script.getVariable("newPosition");
    String value        = script.getVariable("value");

    // Casts to int because ruby returns long instead of int!
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(oldPosition.intValue() + 1).isEqualTo(newPosition.intValue());
    assertThat(value).isEqualTo("test1");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldInsertAtWithNegativeIndex() {
    Number oldSize     = script.getVariable("oldSize");
    Number oldPosition = script.getVariable("oldPosition");
    Number newSize     = script.getVariable("newSize");
    Number newPosition = script.getVariable("newPosition");
    String value        = script.getVariable("value");

    // Casts to Int because Ruby returns long values instead of int
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(oldPosition.intValue() + 1).isEqualTo(newPosition.intValue());
    assertThat(value).isEqualTo("test1");
  }

  // ----------------- 5) insertBefore ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertBeforeNonExistentSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertBeforeWithNullAsSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertNullObjectBeforeSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertWrongObjectBeforeSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertBeforeWrongSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertBeforeOnNonArray() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldInsertBeforeSearchObjectOnBeginning() {
    Number oldSize               = script.getVariable("oldSize");
    Number newSize               = script.getVariable("newSize");
    String oldValue              = script.getVariable("oldValue");
    String newValue              = script.getVariable("newValue");
    String oldValueOnNewPosition  = script.getVariable("oldValueOnNewPosition");

    // casts to int because ruby returns long instead of int
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(oldValue).isEqualTo("euro");
    assertThat(oldValue).isEqualTo(oldValueOnNewPosition);
    assertThat(newValue).isEqualTo("Test");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldInsertBeforeSearchObject() {
    Number oldSize = script.getVariable("oldSize");
    String oldValue = script.getVariable("oldValue");
    String oldValueOnNewPosition = script.getVariable("oldValueOnNewPosition");

    Number newSize = script.getVariable("newSize");
    String newValue = script.getVariable("newValue");

    // casts to int because ruby returns long instead of int
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(oldValue).isEqualTo("dollar");
    assertThat(oldValue).isEqualTo(oldValueOnNewPosition);
    assertThat(newValue).isEqualTo("Test");
  }

  // ----------------- 6) insertAfter ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAfterNonExistentSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAfterWithNullAsSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertNullObjectAfterSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertWrongObjectAfterSearchObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAfterOnNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailInsertAfterWrongSearchObject() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldInsertAfterSearchObjectOnEnding() {
    Number oldSize               = script.getVariable("oldSize");
    Number newSize               = script.getVariable("newSize");
    String oldValue              = script.getVariable("oldValue");
    String newValue              = script.getVariable("newValue");
    String oldValueOnNewPosition  = script.getVariable("oldValueOnNewPosition");

    // casts to int because ruby returns long instead of int
    assertThat(oldSize.intValue() + 1).isEqualTo(newSize.intValue());
    assertThat(oldValue).isEqualTo("dollar");
    assertThat(oldValue).isEqualTo(oldValueOnNewPosition);
    assertThat(newValue).isEqualTo("Test");
  }

  // ----------------- 7) remove ----------------------

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveNonExistentObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveNullObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveWrongObject() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldRemoveObject() {
    Number oldSize = script.getVariable("oldSize");
    String oldValue = script.getVariable("oldValue");
    Number newSize = script.getVariable("newSize");
    String newValue = script.getVariable("newValue");

    assertThat(oldValue.equals(newValue)).isFalse();

    // Casts to int because ruby returns long instead of int values!
    assertThat(oldSize.intValue() - 1).isEqualTo(newSize.intValue());
  }

  // ----------------- 8) removeLast ----------------------

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveLastNullObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveLastWrongObject() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveLastNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveLastNonExistentObject() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", value = "[\"test\",\"test\",\"new value\",\"test\"]")
  public void shouldRemoveLast() {
    Number oldSize = script.getVariable("oldSize");
    Number newSize = script.getVariable("newSize");
    String oldValue = script.getVariable("oldValue");
    String value   = script.getVariable("newValue");

    // casts to int because ruby returns long instead of int
    assertThat(oldSize.intValue() - 1).isEqualTo(newSize.intValue());
    assertThat(oldValue).isEqualTo("test");
    assertThat(value).isEqualTo("new value");
  }

  // ----------------- 9) removeAt ----------------------

  @Test(expected = SpinJsonException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveAtNonArray() throws Throwable {
    failingWithException();
  }

  @Test(expected = IndexOutOfBoundsException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveAtWithIndexOutOfBounds() throws Throwable {
    failingWithException();
  }

  @Test(expected = IndexOutOfBoundsException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailRemoveAtWithNegativeIndexOutOfBounds() throws Throwable {
    failingWithException();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldRemoveAtWithIndex() {
    Number oldSize = script.getVariable("oldSize");
    Number newSize = script.getVariable("newSize");
    String value   = script.getVariable("value");


    // casts to int because ruby returns long instead of int
    assertThat(newSize.intValue()).isEqualTo(1);
    assertThat(oldSize.intValue() - 1).isEqualTo(newSize.intValue());
    assertThat(value).isEqualTo("euro");
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldRemoveAtWithNegativeIndex() {
    Number oldSize = script.getVariable("oldSize");
    Number newSize = script.getVariable("newSize");
    String value   = script.getVariable("value");

    // casts to int because ruby returns long instead of int
    assertThat(newSize.intValue()).isEqualTo(1);
    assertThat(oldSize.intValue() - 1).isEqualTo(newSize.intValue());
    assertThat(value).isEqualTo("dollar");
  }

}
