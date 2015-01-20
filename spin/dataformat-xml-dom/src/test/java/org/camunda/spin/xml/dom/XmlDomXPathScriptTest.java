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
import org.camunda.spin.xml.SpinXPathException;
import org.camunda.spin.xml.SpinXPathQuery;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
public abstract class XmlDomXPathScriptTest extends ScriptTest {

  private static final String xml = "<root><child id=\"child\"><a id=\"a\"/><b id=\"b\"/><a id=\"c\"/></child></root>";
  private static final String xmlWithNamespace = "<root xmlns:bar=\"http://camunda.org\" xmlns:foo=\"http://camunda.com\"><foo:child id=\"child\"><bar:a id=\"a\"/><foo:b id=\"b\"/><a id=\"c\"/></foo:child></root>";
  private static final String xmlWithDefaultNamespace = "<root xmlns=\"http://camunda.com/example\" xmlns:bar=\"http://camunda.org\" xmlns:foo=\"http://camunda.com\"><foo:child id=\"child\"><bar:a id=\"a\"/><foo:b id=\"b\"/><a id=\"c\"/></foo:child></root>";

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child")
    }
  )
  public void canQueryElement() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlElement child = query.element();
    assertThat(child.name()).isEqualTo("child");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test(expected = SpinXPathException.class)
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child")
    }
  )
  public void canNotQueryElementAsAttribute() {
    SpinXPathQuery query = script.getVariable("query");
    query.attribute();
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child/a")
    }
  )
  public void canQueryElementList() {
    SpinXPathQuery query = script.getVariable("query");
    SpinList<SpinXmlElement> childs = query.elementList();
    assertThat(childs).hasSize(2);
  }

  @Test(expected = SpinXPathException.class)
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child/@id")
    }
  )
  public void canNotQueryAttributeAsElement() {
    SpinXPathQuery query = script.getVariable("query");
    query.element();
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child/@id")
    }
  )
  public void canQueryAttribute() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlAttribute attribute = query.attribute();
    assertThat(attribute.value()).isEqualTo("child");
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child/a/@id")
    }
  )
  public void canQueryAttributeList() {
    SpinXPathQuery query = script.getVariable("query");
    SpinList<SpinXmlAttribute> attributes = query.attributeList();
    assertThat(attributes).hasSize(2);
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "string(/root/child/@id)")
    }
  )
  public void canQueryString() {
    SpinXPathQuery query = script.getVariable("query");
    String value = query.string();
    assertThat(value).isEqualTo("child");
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "count(/root/child/a)")
    }
  )
  public void canQueryNumber() {
    SpinXPathQuery query = script.getVariable("query");
    Double count = query.number();
    assertThat(count).isEqualTo(2);
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "boolean(/root/child)")
    }
  )
  public void canQueryBooleanTrue() {
    SpinXPathQuery query = script.getVariable("query");
    Boolean exists = query.bool();
    assertThat(exists).isTrue();
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "boolean(/root/not)")
    }
  )
  public void canQueryBoolean() {
    SpinXPathQuery query = script.getVariable("query");
    Boolean exists = query.bool();
    assertThat(exists).isFalse();
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.canQueryElementWithNamespace",
    variables = {
      @ScriptVariable(name = "input", value = xmlWithNamespace),
      @ScriptVariable(name = "expression", value = "/root/a:child")
    }
  )
  public void canQueryElementWithNamespace() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlElement child = query.element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name = "input", value = xmlWithNamespace),
      @ScriptVariable(name = "expression", value = "/root/foo:child")
    }
  )
  public void canQueryElementWithNamespaceDetection() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlElement child = query.element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  @Script(
    variables = {
      @ScriptVariable(name = "input", value = xmlWithNamespace),
      @ScriptVariable(name = "expression", value = "/root/a:child")
    }
  )
  public void canQueryElementWithNamespaceMap() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlElement child = query.element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.canQueryElementWithNamespace",
    variables = {
      @ScriptVariable(name = "input", value = xmlWithDefaultNamespace),
      @ScriptVariable(name = "expression", value = "/DEFAULT:root/a:child")
    }
  )
  public void canQueryElementWithDefaultNamespace() {
    SpinXPathQuery query = script.getVariable("query");
    SpinXmlElement child = query.element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }
}
