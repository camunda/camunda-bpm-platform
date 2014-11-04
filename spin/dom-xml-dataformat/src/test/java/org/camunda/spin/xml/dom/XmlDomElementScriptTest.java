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

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlAttributeException;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.SpinXmlElementException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.*;

/**
 * @author Sebastian Menski
 */
public abstract class XmlDomElementScriptTest extends ScriptTest {

  // has attribute

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "order")
    }
  )
  public void canCheckAttributeByName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isTrue();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    }
  )
  public void canCheckAttributeByNonExistingName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void canCheckAttributeByNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "dueUntil")
    }
  )
  public void canCheckAttributeByNamespaceAndName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isTrue();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = "order")
    }
  )
  public void canCheckAttributeByNonExistingNamespaceAndName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", isNull = true)
    },
    execute = false
  )
  public void canCheckAttributeByNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = NON_EXISTING)
    }
  )
  public void canCheckAttributeByNamespaceAndNonExistingName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isFalse();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.checkAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "order")
    }
  )
  public void canCheckAttributeByNullNamespaceAndName() {
    Boolean hasAttribute = script.getVariable("hasAttribute");
    assertThat(hasAttribute).isTrue();
  }

  // read attribute

  @Test
  @Script(
    name = "XmlDomElementScriptTest.readAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "order")
    },
    execute = false
  )
  public void canReadAttributeByName() {
    script.setVariable("variables", new HashMap<String, Object>());
    script.execute();
    String value = script.getVariable("value");
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlAttributeException.class)
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

  @Test(expected = IllegalArgumentException.class)
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
      @ScriptVariable(name = "name", value = "dueUntil")
    }
  )
  public void canReadAttributeByNamespaceAndName() {
    String value = script.getVariable("value");
    assertThat(value).isEqualTo("20150112");
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

  @Test(expected = SpinXmlAttributeException.class)
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

  @Test(expected = SpinXmlAttributeException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  @Test(expected = SpinXmlAttributeException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  // write attribute

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", value = "order2")
    }
  )
  public void canWriteAttributeByName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("order2");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = NON_EXISTING),
      @ScriptVariable(name = "value", value = "newValue")
    }
  )
  public void canWriteAttributeByNonExistingName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("newValue");
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", isNull = true),
      @ScriptVariable(name = "value", value = "order2")
    },
    execute = false
  )
  public void canWriteAttributeByNullName() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", isNull = true)
    },
    execute = false
  )
  public void cannotWriteAttributeByNameWithNullValue() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", value = "order2")
    }
  )
  public void canWriteAttributeByNamespaceAndName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("order2");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = NON_EXISTING),
      @ScriptVariable(name = "value", value = "order2")
    }
  )
  public void canWriteAttributeByNamespaceAndNonExistingName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("order2");
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", isNull = true),
      @ScriptVariable(name = "value", value = "order2")
    },
    execute = false
  )
  public void canWriteAttributeByNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", isNull = true)
    },
    execute = false
  )
  public void canWriteAttributeByNamespaceAndNameWithNullValue() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", value = "order2")
    }
  )
  public void canWriteAttributeByNullNamespaceAndName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("order2");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeAttributeValueByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", value = NON_EXISTING),
      @ScriptVariable(name = "name", value = "order"),
      @ScriptVariable(name = "value", value = "order2")
    }
  )
  public void canWriteAttributeByNonExistingNamespaceAndName() {
    String newValue = script.getVariable("newValue");
    assertThat(newValue).isEqualTo("order2");
  }

  // remove attribute

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name",  value = "order")
    }
  )
  public void canRemoveAttributeByName() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttr("order")).isFalse();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name",  value = NON_EXISTING)
    }
  )
  public void canRemoveAttributeByNonExistingName() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttr(NON_EXISTING)).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name",  isNull = true)
    },
    execute = false
  )
  public void cannotRemoveAttributeByNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace",  value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name",  value = "order")
    }
  )
  public void canRemoveAttributeByNamespaceAndName() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttrNs(EXAMPLE_NAMESPACE, "order")).isFalse();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace",  isNull = true),
      @ScriptVariable(name = "name",  value = "order")
    }
  )
  public void canRemoveAttributeByNullNamespaceAndName() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttrNs(null, "order")).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace",  value = EXAMPLE_NAMESPACE),
      @ScriptVariable(name = "name",  isNull = true)
    },
    execute = false
  )
  public void canRemoveAttributeByNamespaceAndNullName() throws Throwable {
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeAttributeByNamespaceAndName",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace",  value = NON_EXISTING),
      @ScriptVariable(name = "name",  value = "order")
    }
  )
  public void canRemoveAttributeByNonExistingNamespaceAndName() {
    SpinXmlElement element = script.getVariable("element");
    assertThat(element.hasAttrNs(NON_EXISTING, "order")).isFalse();
  }

  // get attributes

  @Test
  @Script("XmlDomElementScriptTest.getAllAttributesAndNames")
  @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME)
  public void canGetAllAttributes() {
    SpinList<SpinXmlAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil", "xmlns", "ex");
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
    SpinList<SpinXmlAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.value()).isIn("order1", "20150112");
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
    SpinList<SpinXmlAttribute> attributes = script.getVariable("attributes");
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.value()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isNull();
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
    SpinList<SpinXmlAttribute> attributes = script.getVariable("attributes");
    assertThat(attributes).isEmpty();
  }

  @Test
  @Script("XmlDomElementScriptTest.getAllAttributesAndNames")
  @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME)
  public void canGetAllAttributeNames() {
    List<String> names = script.getVariable("names");
    assertThat(names).containsOnly("order", "dueUntil", "xmlns", "ex");
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
    assertThat(names).containsOnly("dueUntil");
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
    assertThat(names).containsOnly("order");
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

  // get child element

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "date")
    }
  )
  public void canGetSingleChildElementByName() {
    SpinXmlElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").value()).isEqualTo("20140512");
  }

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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
    SpinXmlElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").value()).isEqualTo("20140512");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "file")
    }
  )
  public void canGetSingleChildElementByNullNamespaceAndName() {
    SpinXmlElement childElement = script.getVariable("childElement");
    assertThat(childElement).isNotNull();
  }

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  // append child element

  @Test
  @Script(
    name = "XmlDomElementScriptTest.appendChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child", value = "<child/>")
    }
  )
  public void canAppendChildElement() {
    SpinXmlElement element = script.getVariable("element");

    SpinXmlElement child = element.childElement(null, "child");
    assertThat(child).isNotNull();
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.appendChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child", value = "<child xmlns=\"" + EXAMPLE_NAMESPACE + "\"/>")
    }
  )
  public void canAppendChildElementWithNamespace() {
    SpinXmlElement element = script.getVariable("element");

    SpinXmlElement child = element.childElement(EXAMPLE_NAMESPACE, "child");
    assertThat(child).isNotNull();
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test(expected = IllegalArgumentException.class)
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

  @Test
  @Script(
    name = "XmlDomElementScriptTest.appendChildElementAtPosition",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child", value = "<child/>")
    }
  )
  public void canAppendChildElementAtPosition() {
    SpinXmlElement element = script.getVariable("element");

    SpinList<SpinXmlElement> childs = element.childElements();

    assertThat(childs.get(0).name()).isEqualTo("child");
    assertThat(childs.get(2).name()).isEqualTo("child");
    assertThat(childs.get(childs.size() - 1).name()).isEqualTo("child");
  }

  // remove child elements

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeChildElement",
    variables = {
      @ScriptVariable(name = "input", isNull = true),
      @ScriptVariable(name = "child2", isNull = true)
    },
    execute = false
  )
  public void canRemoveAChildElement() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    SpinXmlElement child = XML("<child/>");
    element.append(child);

    element = script
      .setVariable("element", element)
      .setVariable("child", child)
      .execute()
      .getVariable("element");

    try {
      assertThat(element.childElement(null, "child"));
      fail("Child element should be removed");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test(expected = IllegalArgumentException.class)
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

  @Test(expected = SpinXmlElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.removeChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "child2", isNull = true)
    },
    execute = false
  )
  public void cannotRemoveANonChildElement() throws Throwable {
    script.setVariable("child", S("<child/>"));
    failingWithException();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeChildElement",
    variables = {
      @ScriptVariable(name = "input", isNull = true)
    },
    execute = false
  )
  public void canRemoveMultipleChildElements() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    SpinXmlElement child1 = XML("<child/>");
    SpinXmlElement child2 = XML("<child/>");
    element.append(child1, child2);

    element = script
      .setVariable("element", element)
      .setVariable("child", child1)
      .setVariable("child2", child2)
      .execute()
      .getVariable("element");

    try {
      assertThat(element.childElement(null, "child"));
      fail("Child elements should be removed");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.removeChildElement",
    variables = {
      @ScriptVariable(name = "input", isNull = true),
      @ScriptVariable(name = "child2", isNull = true)
    },
    execute = false
  )
  public void canRemoveChildElementCollection() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    element.append(XML("<child/>"), XML("<child/>"), XML("<child/>"));

    element = script
      .setVariable("element", element)
      .setVariable("child", element.childElements(null, "child"))
      .execute()
      .getVariable("element");

    try {
      assertThat(element.childElement(null, "child"));
      fail("Child elements should be removed");
    }
    catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  // get child elements

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "name", value = "customer")
    }
  )
  public void canGetAllChildElementsByName() {
    SpinList<SpinXmlElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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
    SpinList<SpinXmlElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(3);
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.getChildElementsByNamespaceAndName",
    variables = {
      @ScriptVariable(name= "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "namespace", isNull = true),
      @ScriptVariable(name = "name", value = "info")
    }
  )
  public void canGetAllChildElementsByNullNamespaceAndName() {
    SpinList<SpinXmlElement> childElements = script.getVariable("childElements");
    assertThat(childElements).hasSize(2);
  }

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  @Test(expected = SpinXmlElementException.class)
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

  @Test(expected = IllegalArgumentException.class)
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

  // replace child element

  @Test
  @Script(
    name = "XmlDomElementScriptTest.replaceChildElement",
    variables = {
      @ScriptVariable(name = "input", isNull = true),
      @ScriptVariable(name = "newChild", value = "<child/>")
    },
    execute = false
  )
  public void canReplaceAChildElement() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    SpinXmlElement date = element.childElement("date");

    element = script
      .setVariable("element", element)
      .setVariable("existingChild", date)
      .execute()
      .getVariable("element");

    assertThat(element.childElement(null, "child")).isNotNull();
    try {
      element.childElement("date");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.replaceChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "existingChild", isNull = true),
      @ScriptVariable(name = "newChild", value = "<child/>")
    },
    execute = false
  )
  public void cannotReplaceANullChildElement() throws Throwable {
    failingWithException();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.replaceChildElement",
    variables = {
      @ScriptVariable(name = "input", isNull = true),
      @ScriptVariable(name = "newChild", isNull = true)
    },
    execute = false
  )
  public void cannotReplaceByANullChildElement() throws Throwable {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    SpinXmlElement date = element.childElement("date");

    script
      .setVariable("element", element)
      .setVariable("existingChild", date);
    failingWithException();
  }

  @Test(expected = SpinXmlElementException.class)
  @Script(
    name = "XmlDomElementScriptTest.replaceChildElement",
    variables = {
      @ScriptVariable(name = "input", file = EXAMPLE_XML_FILE_NAME),
      @ScriptVariable(name = "newChild", value = "<child/>")
    },
    execute = false
  )
  public void cannotReplaceANoneChildElement() throws Throwable {
    script.setVariable("existingChild", XML("<child/>"));
    failingWithException();
  }

  // replace element

  @Test
  @Script(
    name = "XmlDomElementScriptTest.replaceElement",
    variables = {
      @ScriptVariable(name = "newElement", value = "<child/>")
    },
    execute = false
  )
  public void canReplaceElement() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    script
      .setVariable("oldElement", element.childElement("date"))
      .execute();

    assertThat(element.childElement(null, "child")).isNotNull();
    try {
      element.childElement("date");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }

  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.replaceElement",
    variables = {
      @ScriptVariable(name = "newElement", value = "<root/>")
    },
    execute = false
  )
  public void canReplaceRootElement() {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    element = script
      .setVariable("oldElement", element)
      .execute()
      .getVariable("element");

    assertThat(element.name()).isEqualTo("root");
    assertThat(element.childElements()).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.replaceElement",
    variables = {
      @ScriptVariable(name = "newElement", isNull = true)
    },
    execute = false
  )
  public void cannotReplaceByNullElement() throws Throwable {
    SpinXmlElement element = XML(exampleXmlFileAsReader());
    script
      .setVariable("oldElement", element);
    failingWithException();
  }

  @Test
  @Script("XmlDomElementScriptTest.readTextContent")
  @ScriptVariable(name = "input", value="<customer>Foo</customer>")
  public void canReadTextContent() {
    String textContent = script.getVariable("textContent");
    assertThat(textContent).isEqualTo("Foo");
  }

  @Test
  @Script("XmlDomElementScriptTest.readTextContent")
  @ScriptVariable(name = "input", value="<customer/>")
  public void canEmptyReadTextContent() {
    String textContent = script.getVariable("textContent");
    assertThat(textContent).isEmpty();
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeTextContent",
    variables = {
      @ScriptVariable(name = "input", value="<customer/>"),
      @ScriptVariable(name = "text", value = "Foo")
    }
  )
  public void canWriteTextContent() {
    String textContent = script.getVariable("textContent");
    assertThat(textContent).isEqualTo("Foo");
  }

  @Test
  @Script(
    name = "XmlDomElementScriptTest.writeTextContent",
    variables = {
      @ScriptVariable(name = "input", value="<customer/>"),
      @ScriptVariable(name = "text", value = "")
    }
  )
  public void canWriteEmptyTextContent() {
    String textContent = script.getVariable("textContent");
    assertThat(textContent).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(
    name = "XmlDomElementScriptTest.writeTextContent",
    variables = {
      @ScriptVariable(name = "input", value="<customer/>"),
      @ScriptVariable(name = "text", isNull = true)
    },
    execute = false
  )
  public void cannotWriteNullTextContent() throws Throwable {
    failingWithException();
  }

}
