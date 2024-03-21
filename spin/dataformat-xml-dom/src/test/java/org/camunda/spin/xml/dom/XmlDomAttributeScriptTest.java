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
package org.camunda.spin.xml.dom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML_FILE_NAME;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.SpinXmlAttributeException;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public abstract class XmlDomAttributeScriptTest extends ScriptTest {

  @Test
  @Script(
    name = "XmlDomAttributeScriptTest.testAttribute",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "attributeName", value = "order"),
      @ScriptVariable(name = "valueToSet", value = "order2")
    }
  )
  public void shouldGetInformationAndSetValue() {
    assertThat((String) script.getVariable("name")).isEqualTo("order");
    assertThat((String) script.getVariable("value")).isEqualTo("order1");
    assertThat((String) script.getVariable("namespace")).isNull();
    assertThat(script.<Boolean>getVariable("hasNullNamespace")).isTrue();
    assertThat((String) script.getVariable("newValue")).isEqualTo("order2");
  }

  @Test(expected = SpinXmlAttributeException.class)
  @Script(
    name = "XmlDomAttributeScriptTest.testAttribute",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "attributeName", value = "order"),
      @ScriptVariable(name = "valueToSet", isNull = true)
    },
    execute = false
  )
  public void setNullValue() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomAttributeScriptTest.removeAttribute",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "attributeName", value = "order")
    }
  )
  public void remove() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttr("order")).isFalse();
  }

}
