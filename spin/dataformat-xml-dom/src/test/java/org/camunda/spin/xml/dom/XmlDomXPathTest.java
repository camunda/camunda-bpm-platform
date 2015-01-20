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
import org.camunda.spin.xml.SpinXPathException;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.Spin.S;

/**
 * @author Sebastian Menski
 */
public class XmlDomXPathTest {

  protected SpinXmlElement element;
  protected SpinXmlElement elementWithNamespace;
  protected SpinXmlElement elementWithDefaultNamespace;

  @Before
  public void parseXml() {
    element = S("<root><child id=\"child\"><a id=\"a\"/><b id=\"b\"/><a id=\"c\"/></child></root>");
    elementWithNamespace = S("<root xmlns:bar=\"http://camunda.org\" xmlns:foo=\"http://camunda.com\"><foo:child id=\"child\"><bar:a id=\"a\"/><foo:b id=\"b\"/><a id=\"c\"/></foo:child></root>");
    elementWithDefaultNamespace = S("<root xmlns=\"http://camunda.com/example\" xmlns:bar=\"http://camunda.org\" xmlns:foo=\"http://camunda.com\"><foo:child id=\"child\"><bar:a id=\"a\"/><foo:b id=\"b\"/><a id=\"c\"/></foo:child></root>");
  }

  @Test
  public void canQueryElement() {
    SpinXmlElement child = element.xPath("/root/child").element();
    assertThat(child.name()).isEqualTo("child");
    assertThat(child.attr("id").value()).isEqualTo("child");

    SpinXmlElement b = child.xPath("b").element();
    assertThat(b.name()).isEqualTo("b");
    assertThat(b.attr("id").value()).isEqualTo("b");
  }

  @Test(expected = SpinXPathException.class)
  public void canNotQueryElementAsAttribute() {
    element.xPath("/root/child/").attribute();
  }

  @Test
  public void canQueryElementList() {
    SpinList<SpinXmlElement> childs = element.xPath("/root/child/a").elementList();
    assertThat(childs).hasSize(2);
  }

  @Test(expected = SpinXPathException.class)
  public void canNotQueryAttributeAsElement() {
    element.xPath("/root/child/@id").element();
  }

  @Test
  public void canQueryAttribute() {
    SpinXmlAttribute attribute = element.xPath("/root/child/@id").attribute();
    assertThat(attribute.value()).isEqualTo("child");
  }

  @Test
  public void canQueryAttributeList() {
    SpinList<SpinXmlAttribute> attributes = element.xPath("/root/child/a/@id").attributeList();
    assertThat(attributes).hasSize(2);
  }

  @Test
  public void canQueryString() {
    String value = element.xPath("string(/root/child/@id)").string();
    assertThat(value).isEqualTo("child");
  }

  @Test
  public void canQueryNumber() {
    Double count = element.xPath("count(/root/child/a)").number();
    assertThat(count).isEqualTo(2);
  }

  @Test
  public void canQueryBoolean() {
    Boolean exists = element.xPath("boolean(/root/child)").bool();
    assertThat(exists).isTrue();

    exists = element.xPath("boolean(/root/not)").bool();
    assertThat(exists).isFalse();
  }

  @Test
  public void canQueryElementWithNamespace() {
    SpinXmlElement child = elementWithNamespace.xPath("/root/a:child")
      .ns("a", "http://camunda.com")
      .element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  public void canQueryElementWithNamespaceAutoDetect() {
    SpinXmlElement child = elementWithNamespace.xPath("/root/foo:child")
      .detectNamespaces()
      .element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");

    SpinXmlElement child2 = child.xPath("foo:b")
      .ns("foo", "http://camunda.com")
      .element();

    assertThat(child2.name()).isEqualTo("b");
    assertThat(child2.namespace()).isEqualTo("http://camunda.com");
    assertThat(child2.attr("id").value()).isEqualTo("b");
  }

  @Test
  public void canQueryElementWithNamespaceMap() {
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("a", "http://camunda.com");
    namespaces.put("b", "http://camunda.org");

    SpinXmlElement child = elementWithNamespace.xPath("/root/a:child/b:a")
      .ns(namespaces)
      .element();

    assertThat(child.name()).isEqualTo("a");
    assertThat(child.namespace()).isEqualTo("http://camunda.org");
    assertThat(child.attr("id").value()).isEqualTo("a");
  }

  @Test
  public void canQueryElementWithDefaultNamespace() {
    SpinXmlElement child = elementWithDefaultNamespace.xPath("/DEFAULT:root/a:child")
      .ns("a", "http://camunda.com")
      .element();

    assertThat(child.name()).isEqualTo("child");
    assertThat(child.namespace()).isEqualTo("http://camunda.com");
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

}
