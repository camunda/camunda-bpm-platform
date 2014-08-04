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
import org.camunda.spin.test.Script;
import org.camunda.spin.test.ScriptTest;
import org.camunda.spin.test.ScriptVariable;
import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.camunda.spin.xml.tree.SpinXmlTreeXPathException;
import org.camunda.spin.xml.tree.SpinXmlTreeXPathQuery;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian Menski
 */
public abstract class XmlDomXPathScriptTest extends ScriptTest {

  private static final String xml = "<root><child id=\"child\"><a id=\"a\"/><b id=\"b\"/><a id=\"c\"/></child></root>";

  @Test
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child")
    }
  )
  public void canQueryElement() {
    SpinXmlTreeXPathQuery query = script.getVariable("query");
    SpinXmlTreeElement child = query.element();
    assertThat(child.name()).isEqualTo("child");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test(expected = SpinXmlTreeXPathException.class)
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child")
    }
  )
  public void canNotQueryElementAsAttribute() {
    SpinXmlTreeXPathQuery query = script.getVariable("query");
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
    SpinList<SpinXmlTreeElement> childs = query.elementList();
    assertThat(childs).hasSize(2);
  }

  @Test(expected = SpinXmlTreeXPathException.class)
  @Script(
    name = "XmlDomXPathScriptTest.xPath",
    variables = {
      @ScriptVariable(name = "input", value = xml),
      @ScriptVariable(name = "expression", value = "/root/child/@id")
    }
  )
  public void canNotQueryAttributeAsElement() {
    SpinXmlTreeXPathQuery query = script.getVariable("query");
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
    SpinXmlTreeAttribute attribute = query.attribute();
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
    SpinList<SpinXmlTreeAttribute> attributes = query.attributeList();
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
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
    SpinXmlTreeXPathQuery query = script.getVariable("query");
    Boolean exists = query.bool();
    assertThat(exists).isFalse();
  }

}
