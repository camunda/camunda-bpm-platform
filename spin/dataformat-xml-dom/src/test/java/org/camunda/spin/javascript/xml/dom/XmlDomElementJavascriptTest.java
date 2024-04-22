/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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

import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML_FILE_NAME;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptEngine;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.dom.XmlDomElementScriptTest;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
@ScriptEngine("graal.js")
public class XmlDomElementJavascriptTest extends XmlDomElementScriptTest {

  /**
   * The Graal.js scripting engine cannot determine the method to call if the
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
   * The Graal.js scripting engine cannot determine the method to call if the
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
