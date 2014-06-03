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
import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.camunda.spin.xml.tree.SpinXmlTreeXPathException;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.Spin.S;

/**
 * @author Sebastian Menski
 */
public class XmlDomXPathTest {

  protected SpinXmlTreeElement element;

  @Before
  public void parseXml() {
    element = S("<root><child id=\"child\"><a id=\"a\"/><b id=\"b\"/><a id=\"c\"/></child></root>");
  }

  @Test
  public void canQueryElement() {
    SpinXmlTreeElement child = element.xPath("/root/child").element();
    assertThat(child.name()).isEqualTo("child");
    assertThat(child.attr("id").value()).isEqualTo("child");

    SpinXmlTreeElement b = child.xPath("b").element();
    assertThat(b.name()).isEqualTo("b");
    assertThat(b.attr("id").value()).isEqualTo("b");
  }

  @Test(expected = SpinXmlTreeXPathException.class)
  public void canNotQueryElementAsAttribute() {
    element.xPath("/root/child/").attribute();
  }

  @Test
  public void canQueryElementList() {
    SpinList<SpinXmlTreeElement> childs = element.xPath("/root/child/a").elementList();
    assertThat(childs).hasSize(2);
  }

  @Test(expected = SpinXmlTreeXPathException.class)
  public void canNotQueryAttributeAsElement() {
    element.xPath("/root/child/@id").element();
  }

  @Test
  public void canQueryAttribute() {
    SpinXmlTreeAttribute attribute = element.xPath("/root/child/@id").attribute();
    assertThat(attribute.value()).isEqualTo("child");
  }

  @Test
  public void canQueryAttributeList() {
    SpinList<SpinXmlTreeAttribute> attributes = element.xPath("/root/child/a/@id").attributeList();
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

}
