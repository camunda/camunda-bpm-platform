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

package org.camunda.spin.javascript.xml.dom;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptEngine;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.dom.XmlDomElementScriptTest;
import org.junit.Test;

import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML_FILE_NAME;

/**
 * @author Sebastian Menski
 */
@ScriptEngine("javascript")
public class XmlDomElementJavascriptTest extends XmlDomElementScriptTest {

  /**
   * The Nashorn scripting engine cannot determine the method to call if the
   * parameter is null.
   */

  @Test(expected = RuntimeException.class)
  @Script(
    name = "XmlDomElementScriptTest.appendChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child", isNull = true)
    },
    execute = false
  )
  public void cannotAppendNullChildElement() throws Throwable {
    failingWithException();
  }

  /**
   * The Nashorn scripting engine cannot determine the method to call if the
   * parameter is null.
   */
  @Test(expected = RuntimeException.class)
  @Script(
    name = "XmlDomElementScriptTest.removeChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child", isNull = true),
      @ScriptVariable(name = "child2", isNull = true)
    },
    execute = false
  )
  public void cannotRemoveANullChildElement() throws Throwable {
    failingWithException();
  }

}
