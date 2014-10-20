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

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.json.SpinJsonPropertyException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_FILE_NAME;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class JsonTreeRemovePropertyScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldRemovePropertyByName() {
    Boolean value = script.getVariable("value");

    assertThat(value).isFalse();
  }

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldRemovePropertyByList() {
    Boolean value1 = script.getVariable("value1");
    Boolean value2 = script.getVariable("value2");

    assertThat(value1).isFalse();
    assertThat(value2).isFalse();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailWhileRemovingPropertyByName() throws Throwable{
    failingWithException();
  }

  @Test(expected = SpinJsonPropertyException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldFailWhileRemovingPropertyByList() throws Throwable{
    failingWithException();
  }

}

