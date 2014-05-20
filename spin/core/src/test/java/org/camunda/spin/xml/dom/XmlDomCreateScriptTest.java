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
package org.camunda.spin.xml.dom;

import org.camunda.spin.impl.xml.dom.SpinXmlDomElement;
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

import static org.camunda.spin.xml.XmlTestConstants.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Daniel Meyer
 *
 */
public abstract class XmlDomCreateScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name="input", value=EXAMPLE_XML)
  public void shouldCreateForString() {

    SpinXmlDomElement xml1 = script.getVariable("xml1");
    assertThat(xml1).isNotNull();

    SpinXmlDomElement xml2 = script.getVariable("xml2");
    assertThat(xml2).isNotNull();

    SpinXmlDomElement xml3 = script.getVariable("xml3");
    assertThat(xml3).isNotNull();

  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name="input1", file=EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name="input2", file=EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name="input3", file=EXAMPLE_XML_FILE_NAME)
    }
  )
  public void shouldCreateForInputStream() {

    SpinXmlDomElement xml1 = script.getVariable("xml1");
    assertThat(xml1).isNotNull();

    SpinXmlDomElement xml2 = script.getVariable("xml2");
    assertThat(xml2).isNotNull();

    SpinXmlDomElement xml3 = script.getVariable("xml3");
    assertThat(xml3).isNotNull();

  }

  @Test
  @Script
  @ScriptVariable(name="input", value=EXAMPLE_XML)
  public void shouldBeIdempotent() {

    SpinXmlDomElement xml = script.getVariable("xml");
    assertThat(xml).isNotNull();

  }

}

