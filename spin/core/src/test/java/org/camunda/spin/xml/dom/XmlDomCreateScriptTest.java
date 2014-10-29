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

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML_FILE_NAME;

/**
 * @author Daniel Meyer
 *
 */
public abstract class XmlDomCreateScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name="input", value = "<root/>")
  public void shouldCreateForString() {

    SpinXmlElement xml1 = script.getVariable("xml1");
    assertThat(xml1).isNotNull();

    SpinXmlElement xml2 = script.getVariable("xml2");
    assertThat(xml2).isNotNull();

    SpinXmlElement xml3 = script.getVariable("xml3");
    assertThat(xml3).isNotNull();

  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name="input1", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name="input2", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name="input3", file = EXAMPLE_XML_FILE_NAME)
    }
  )
  public void shouldCreateForReader() {

    SpinXmlElement xml1 = script.getVariable("xml1");
    assertThat(xml1).isNotNull();

    SpinXmlElement xml2 = script.getVariable("xml2");
    assertThat(xml2).isNotNull();

    SpinXmlElement xml3 = script.getVariable("xml3");
    assertThat(xml3).isNotNull();

  }

  @Test
  @Script
  @ScriptVariable(name="input", file = EXAMPLE_XML_FILE_NAME)
  public void shouldBeIdempotent() {

    SpinXmlElement xml = script.getVariable("xml");
    assertThat(xml).isNotNull();

  }

}

