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

import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public abstract class JsonTreeCreateScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name="input", value = "{\"root\": 1}")
  public void shouldCreateForString() {

    JacksonJsonNode json1 = script.getVariable("json1");
    assertThat(json1).isNotNull();

    JacksonJsonNode json2 = script.getVariable("json2");
    assertThat(json2).isNotNull();

    JacksonJsonNode json3 = script.getVariable("json3");
    assertThat(json3).isNotNull();

  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name="input1", file = EXAMPLE_JSON_FILE_NAME),
      @ScriptVariable(name="input2", file = EXAMPLE_JSON_FILE_NAME),
      @ScriptVariable(name="input3", file = EXAMPLE_JSON_FILE_NAME)
    }
  )
  public void shouldCreateForReader() {

    JacksonJsonNode json1 = script.getVariable("json1");
    assertThat(json1).isNotNull();

    JacksonJsonNode json2 = script.getVariable("json2");
    assertThat(json2).isNotNull();

    JacksonJsonNode json3 = script.getVariable("json3");
    assertThat(json3).isNotNull();
  }

  @Test
  @Script
  @ScriptVariable(name="input", file = EXAMPLE_JSON_FILE_NAME)
  public void shouldBeIdempotent() {

    JacksonJsonNode json = script.getVariable("json");
    assertThat(json).isNotNull();

  }

}

