/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.spin.xml;

import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

import javax.script.ScriptException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
public abstract class SpinXmlScriptTest extends ScriptTest {

  @Test
  @Script("shouldWrapXml")
  @ScriptVariable(name="input", value=SpinXmlTest.TEST_STRING)
  public void shouldWrapXmlString() throws ScriptException {
    SpinXml xml = (SpinXml) script.variables.get("xml");
    assertThat(xml).isNotNull();
  }

  @Test
  @Script(name="shouldWrapXml", execute = false)
  public void shouldWrapXmlInputStream() throws ScriptException {
    Map<String, Object> variables = Collections.singletonMap("input", (Object) SpinXmlTest.TEST_INPUT_STREAM);
    SpinXml xml = (SpinXml) script.execute(variables).get("xml");
    assertThat(xml).isNotNull();
  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name="xml", file="customers.xml"),
      @ScriptVariable(name="bar", value="{}")
    }
  ) // File: org/camunda/spin/python/SpinXml<LANGUAGE>Test.shouldEatXml.<ENDING>
  public void shouldEatXml() {
    assertThat(script.variables.keySet())
      .doesNotContain("xml");
  }

}
