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

package org.camunda.spin.xml.dom;

import org.camunda.spin.SpinCollection;
import org.camunda.spin.SpinScriptException;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttribute;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttributeException;
import org.camunda.spin.impl.xml.dom.SpinXmlDomElement;
import org.camunda.spin.impl.xml.dom.SpinXmlDomElementException;
import org.camunda.spin.test.JavaScriptExceptionUnwrapper;
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.xml.XmlTestConstants.*;

/**
 * @author Sebastian Menski
 */
public abstract class XmlDomElementScriptTest extends ScriptTest {

  @Test
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "order"),
    },
    execute = false

  )
  public void canReadAttributeByName() {
    script.setVariable("variables", new HashMap<String, Object>());
    script.execute();
    String value = script.getVariable("value");
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotReadAttributeByNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotReadAttributeByNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "order")
    }
  )
  public void canReadAttributeByNamespaceAndName() {
    String value = script.getVariable("value");
    assertThat(value).isEqualTo("order1");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "order")
    }
  )
  public void canReadAttributeByNullNamespaceAndName() {
    String value = script.getVariable("value");
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = "order")
    },
    execute = false
  )
  public void cannotReadAttributeByNonExistingNamespaceAndName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotReadAttributeByNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotReadAttributeByNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotReadAttributeByNonExistingNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotReadAttributeByNullNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script("XmlDomElementScriptTest.getAllAttributesAndNames")
  @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME)
  public void canGetAllAttributes() {
    SpinCollection<SpinXmlDomAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlDomAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
      assertThat(attribute.stringValue()).isIn("order1", "20150112");
    }
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE)
    }
  )
  public void canGetAllAttributesByNamespace() {
    SpinCollection<SpinXmlDomAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlDomAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.stringValue()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
    }
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true)
    }
  )
  public void canGetAllAttributesByNullNamespace() {
    SpinCollection<SpinXmlDomAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlDomAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.stringValue()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
    }
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING)
    }
  )
  public void canGetAllAttributesByNonExistingNamespace() {
    SpinCollection<SpinXmlDomAttribute> attributes = script.getVariable("attributes");
    assertThat(attributes).isEmpty();
  }

  @Test
  @Script("XmlDomElementScriptTest.getAllAttributesAndNames")
  @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME)
  public void canGetAllAttributeNames() {
    List<String> names = script.getVariable("names");
    assertThat(names).containsOnly("order", "dueUntil");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE)
    }
  )
  public void canGetAllAttributeNamesByNamespace() {
    List<String> names = script.getVariable("names");
    assertThat(names).containsOnly("order", "dueUntil");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true)
    }
  )
  public void canGetAllAttributeNamesByNullNamespace() {
    List<String> names = script.getVariable("names");
    assertThat(names).containsOnly("order", "dueUntil");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getAllAttributesAndNamesByNamespace",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING)
    }
  )
  public void canGetAllAttributeNamesByNonExistingNamespace() {
    List<String> names = script.getVariable("names");
    assertThat(names).isEmpty();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "date")
    }
  )
  public void canGetSingleChildElementByName() {
    SpinXmlDomElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").stringValue()).isEqualTo("20140512");
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetSingleChildElementByNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetSingleChildElementByNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "date")
    }
  )
  public void canGetSingleChildElementByNamespaceAndName() {
    SpinXmlDomElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").stringValue()).isEqualTo("20140512");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "date")
    }
  )
  public void canGetSingleChildElementByNullNamespaceAndName() {
    SpinXmlDomElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").stringValue()).isEqualTo("20140512");
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetChildElementByNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetChildElementByNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = "date")
    },
    execute = false
  )
  public void cannotGetChildElementByNonExistingNamespaceAndName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetChildElementByNonExistingNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetChildElementByNullNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "customer")
    }
  )
  public void canGetAllChildElementsByName() {
    SpinCollection<SpinXmlDomElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "customer")
    }
  )
  public void canGetAllChildElementsByNamespaceAndName() {
    SpinCollection<SpinXmlDomElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(3);
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "customer")
    }
  )
  public void canGetAllChildElementsByNullNamespaceAndName() {
    SpinCollection<SpinXmlDomElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = "customer")
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNonExistingNamespaceAndName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNamespaceAndNonNullName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNonExistingNamespaceAndNonExistingName() throws Throwable {
    failingWithException();
  }

  @Test(expected = SpinXmlDomElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void cannotGetAllChildElementsByNullNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  private void failingWithException() throws Throwable {
    try {
      script.execute();
    }
    catch (SpinScriptException e) {
      Throwable cause = e.getCause();
      if (cause.getCause() != null) {
        throw cause.getCause().getCause();
      }
      else {
        throw JavaScriptExceptionUnwrapper.unwrap(cause);
      }
    }
  }

}
