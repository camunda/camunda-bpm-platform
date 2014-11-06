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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_NAMESPACE;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML;
import static org.camunda.spin.xml.XmlTestConstants.NON_EXISTING;
import static org.camunda.spin.xml.XmlTestConstants.exampleXmlFileAsReader;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.spin.SpinList;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlAttributeException;
import org.camunda.spin.xml.SpinXmlElement;
import org.camunda.spin.xml.SpinXmlElementException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class XmlDomElementTest {

  protected SpinXmlElement element;

  @Before
  public void parseXml() {
    element = S(exampleXmlFileAsReader());
  }

  // has attribute

  @Test
  public void canCheckAttributeByName() {
    boolean hasAttribute = element.hasAttr("order");
    assertThat(hasAttribute).isTrue();
  }

  @Test
  public void canCheckAttributeByNonExistingName() {
    boolean hasAttribute = element.hasAttr(NON_EXISTING);
    assertThat(hasAttribute).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotCheckAttributeByNullName() {
    element.hasAttr(null);
  }

  @Test
  public void canCheckAttributeByNamespaceAndName() {
    boolean hasAttribute = element.hasAttrNs(EXAMPLE_NAMESPACE, "order");
    assertThat(hasAttribute).isFalse();
  }

  @Test
  public void canCheckAttributeByNamespaceAndNonExistingName() {
    boolean hasAttribute = element.hasAttrNs(EXAMPLE_NAMESPACE, NON_EXISTING);
    assertThat(hasAttribute).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void canCheckAttributeByNamespaceAndNullName() {
    element.hasAttrNs(EXAMPLE_NAMESPACE, null);
  }

  @Test
  public void canCheckAttributeByNonExistingNamespaceAndName() {
    boolean hasAttribute = element.hasAttrNs(NON_EXISTING, "order");
    assertThat(hasAttribute).isFalse();
  }

  @Test
  public void canCheckAttributeByNullNamespaceAndName() {
    boolean hasAttribute = element.hasAttrNs(null, "order");
    assertThat(hasAttribute).isTrue();
  }

  // read attribute

  @Test
  public void canReadAttributeByName() {
    SpinXmlAttribute attribute = element.attr("order");
    String value = attribute.value();
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlAttributeException.class)
  public void cannotReadAttributeByNonExistingName() {
    element.attr(NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReadAttributeByNullName() {
    element.attr(null);
  }

  @Test
  public void canReadAttributeByNamespaceAndName() {
    SpinXmlAttribute attribute = element.attrNs(EXAMPLE_NAMESPACE, "dueUntil");
    String value = attribute.value();
    assertThat(value).isEqualTo("20150112");
  }

  @Test
  public void canReadAttributeByNullNamespaceAndName() {
    SpinXmlAttribute attribute = element.attrNs(null, "order");
    String value = attribute.value();
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlAttributeException.class)
  public void cannotReadAttributeByNonExistingNamespaceAndName() {
    element.attrNs(NON_EXISTING, "order");
  }

  @Test(expected = SpinXmlAttributeException.class)
  public void cannotReadAttributeByNamespaceAndNonExistingName() {
    element.attrNs(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReadAttributeByNamespaceAndNullName() {
    element.attrNs(EXAMPLE_NAMESPACE, null);
  }

  @Test(expected = SpinXmlAttributeException.class)
  public void cannotReadAttributeByNonExistingNamespaceAndNonExistingName() {
    element.attrNs(NON_EXISTING, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReadAttributeByNullNamespaceAndNullName() {
    element.attrNs(null, null);
  }

  // write attribute

  @Test
  public void canWriteAttributeByName() {
    String newValue = element.attr("order", "order2").attr("order").value();
    assertThat(newValue).isEqualTo("order2");
  }

  @Test
  public void canWriteAttributeByNonExistingName() {
    String newValue = element.attr(NON_EXISTING, "newValue").attr(NON_EXISTING).value();
    assertThat(newValue).isEqualTo("newValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotWriteAttributeByNullName() {
    element.attr(null, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void canWriteAttributeByNameWithNullValue() {
    element.attr("order", null);
  }

  @Test
  public void canWriteAttributeByNamespaceAndName() {
    String newValue = element.attrNs(EXAMPLE_NAMESPACE, "order", "order2").attrNs(EXAMPLE_NAMESPACE, "order").value();
    assertThat(newValue).isEqualTo("order2");
  }

  @Test
  public void canWriteAttributeByNamespaceAndNonExistingName() {
    String newValue = element.attrNs(EXAMPLE_NAMESPACE, NON_EXISTING, "newValue").attrNs(EXAMPLE_NAMESPACE, NON_EXISTING).value();
    assertThat(newValue).isEqualTo("newValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotWriteAttributeByNamespaceAndNullName() {
    element.attrNs(EXAMPLE_NAMESPACE, null, "newValue");
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotWriteAttributeByNamespaceAndNameWithNullValue() {
    element.attrNs(EXAMPLE_NAMESPACE, "order", null);
  }

  @Test
  public void canWriteAttributeByNonExistingNamespaceAndName() {
    String newValue = element.attrNs(NON_EXISTING, "order", "newValue").attrNs(NON_EXISTING, "order").value();
    assertThat(newValue).isEqualTo("newValue");
  }

  @Test
  public void canWriteAttributeByNullNamespaceAndName() {
    String newValue = element.attrNs(null, "order", "order2").attrNs(null, "order").value();
    assertThat(newValue).isEqualTo("order2");
  }

  // remove attribute

  @Test
  public void canRemoveAttributeByName() {
    element.removeAttr("order");
    assertThat(element.hasAttr("order")).isFalse();
  }

  @Test
  public void canRemoveAttributeByNonExistingName() {
    element.removeAttr(NON_EXISTING);
    assertThat(element.hasAttr(NON_EXISTING)).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotRemoveAttributeByNullName() {
    element.removeAttr(null);
  }

  @Test
  public void canRemoveAttributeByNamespaceAndName() {
    element.removeAttrNs(EXAMPLE_NAMESPACE, "order");
    assertThat(element.hasAttrNs(EXAMPLE_NAMESPACE, "order")).isFalse();
  }

  @Test
  public void canRemoveAttributeByNullNamespaceAndName() {
    element.removeAttrNs(null, "order");
    assertThat(element.hasAttrNs(null, "order")).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotRemoveAttributeByNamespaceAndNullName() {
    element.removeAttrNs(EXAMPLE_NAMESPACE, null);
  }

  @Test
  public void canRemoveAttributeByNonExistingNamespaceAndName() {
    element.removeAttrNs(NON_EXISTING, "order");
    assertThat(element.hasAttrNs(NON_EXISTING, "order")).isFalse();
  }

  // get attributes

  @Test
  public void canGetAllAttributes() {
    SpinList<SpinXmlAttribute> attributes = element.attrs();
    assertThat(attributes).hasSize(4);
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil", "xmlns", "ex");
    }
  }

  @Test
  public void canGetAllAttributesByNamespace() {
    SpinList<SpinXmlAttribute> attributes = element.attrs(EXAMPLE_NAMESPACE);
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.value()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
    }
  }

  @Test
  public void canGetAllAttributesByNullNamespace() {
    SpinList<SpinXmlAttribute> attributes = element.attrs(null);
    for (SpinXmlAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.value()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isNull();
    }
  }

  @Test
  public void canGetAllAttributesByNonExistingNamespace() {
    SpinList<SpinXmlAttribute> attributes = element.attrs(NON_EXISTING);
    assertThat(attributes).isEmpty();
  }

  // get attribute names

  @Test
  public void canGetAllAttributeNames() {
    List<String> names = element.attrNames();
    assertThat(names).containsOnly("order", "dueUntil", "xmlns", "ex");
  }

  @Test
  public void canGetAllAttributeNamesByNamespace() {
    List<String> names = element.attrNames(EXAMPLE_NAMESPACE);
    assertThat(names).containsOnly("dueUntil");
  }

  @Test
  public void canGetAllAttributeNamesByNullNamespace() {
    List<String> names = element.attrNames(null);
    assertThat(names).containsOnly("order");
  }

  @Test
  public void canGetAllAttributeNamesByNonExistingNamespace() {
    List<String> names = element.attrNames(NON_EXISTING);
    assertThat(names).isEmpty();
  }

  // get child element

  @Test
  public void canGetSingleChildElementByName() {
    SpinXmlElement childElement = element.childElement("date");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").value()).isEqualTo("20140512");
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetSingleChildElementByNonExistingName() {
    element.childElement(NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetSingleChildElementByNullName() {
    element.childElement(null);
  }

  @Test
  public void canGetSingleChildElementByNamespaceAndName() {
    SpinXmlElement childElement = element.childElement(EXAMPLE_NAMESPACE, "date");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").value()).isEqualTo("20140512");
  }

  @Test
  public void canGetSingleChildElementByNullNamespaceAndName() {
    SpinXmlElement childElement = element.childElement(null, "file");
    assertThat(childElement).isNotNull();
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetChildElementByNamespaceAndNonExistingName() {
    element.childElement(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetChildElementByNamespaceAndNullName() {
    element.childElement(EXAMPLE_NAMESPACE, null);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetChildElementByNonExistingNamespaceAndName() {
    element.childElement(NON_EXISTING, "date");
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetChildElementByNonExistingNamespaceAndNonExistingName() {
    element.childElement(NON_EXISTING, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetChildElementByNullNamespaceAndNullName() {
    element.childElement(null, null);
  }

  // append child element

  @Test
  public void canAppendChildElement() {
    SpinXmlElement child = XML("<child/>");
    element = element.append(child);

    child.attr("id", "child");
    child = element.childElement(null, "child");

    assertThat(child).isNotNull();
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  public void canAppendChildElementWithNamespace() {
    SpinXmlElement child = XML("<child xmlns=\"" + EXAMPLE_NAMESPACE + "\"/>");
    element = element.append(child);

    child.attr("id", "child");
    child = element.childElement(EXAMPLE_NAMESPACE, "child");

    assertThat(child).isNotNull();
    assertThat(child.attr("id").value()).isEqualTo("child");
  }

  @Test
  public void canAppendMultipleChildElements() {
    SpinXmlElement child1 = XML("<child/>");
    SpinXmlElement child2 = XML("<child/>");
    SpinXmlElement child3 = XML("<child/>");

    element = element.append(child1, child2, child3);

    child1.attr("id", "child");
    child2.attr("id", "child");
    child3.attr("id", "child");

    SpinList<SpinXmlElement> childs = element.childElements(null, "child");
    assertThat(childs).hasSize(3);

    for (SpinXmlElement childElement : childs) {
      assertThat(childElement).isNotNull();
      assertThat(childElement.attr("id").value()).isEqualTo("child");
    }
  }

  @Test
  public void canAppendChildElementCollection() {
    Collection<SpinXmlElement> childElements = new ArrayList<SpinXmlElement>();
    childElements.add(XML("<child/>"));
    childElements.add(XML("<child/>"));
    childElements.add(XML("<child/>"));

    element = element.append(childElements);

    SpinList<SpinXmlElement> childs = element.childElements(null, "child");
    assertThat(childs).hasSize(3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAppendNullChildElements() {
    element.append((SpinXmlElement[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotAppendNullChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.append(child, null);
  }

  @Test
  public void canAppendChildElementBeforeExistingElement() {
    SpinXmlElement child = XML("<child/>");
    SpinXmlElement date = element.childElement("date");
    element.appendBefore(child, date);
    SpinXmlElement insertedElement = element.childElements().get(0);
    assertThat(insertedElement.name()).isEqualTo("child");
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotAppendChildElementBeforeNonChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.appendBefore(child, child);
  }

  @Test
  public void canAppendChildElementAfterExistingElement() {
    SpinXmlElement child = XML("<child/>");
    SpinXmlElement date = element.childElement("date");
    element.appendAfter(child, date);
    SpinXmlElement insertedElement = element.childElements().get(1);
    assertThat(insertedElement.name()).isEqualTo("child");
  }

  @Test
  public void canAppendChildElementAfterLastChildElement() {
    SpinXmlElement child = XML("<child/>");
    int childCount = element.childElements().size();
    SpinXmlElement lastChildElement = element.childElements().get(childCount - 1);
    element.appendAfter(child, lastChildElement);
    SpinXmlElement insertedElement = element.childElements().get(childCount);
    assertThat(insertedElement.name()).isEqualTo("child");
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotAppendChildElementAfterNonChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.appendAfter(child, child);
  }

  // remove child elements

  @Test
  public void canRemoveAChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.append(child);

    assertThat(element.childElement(null, "child")).isNotNull();

    element.remove(child);

    try {
      assertThat(element.childElement(null, "child"));
      fail("Child element should be removed");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotRemoveANullChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.append(child);

    element.remove(child, null);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotRemoveNonChildElement() {
    SpinXmlElement child1 = XML("<child/>");
    SpinXmlElement child2 = XML("<child/>");

    element.append(child1);

    element.remove(child1, child2);
  }

  @Test
  public void canRemoveMultipleChildElements() {
    SpinXmlElement child1 = XML("<child/>");
    SpinXmlElement child2 = XML("<child/>");
    SpinXmlElement child3 = XML("<child/>");
    element.append(child1, child2, child3);

    assertThat(element.childElements(null, "child")).hasSize(3);

    element.remove(child1, child2, child3);

    try {
      assertThat(element.childElements(null, "child"));
      fail("Child element should be removed");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test
  public void canRemoveChildElementCollection() {
    SpinXmlElement child1 = XML("<child/>");
    SpinXmlElement child2 = XML("<child/>");
    SpinXmlElement child3 = XML("<child/>");
    element.append(child1, child2, child3);

    assertThat(element.childElements(null, "child")).hasSize(3);

    element.remove(element.childElements(null, "child"));

    try {
      assertThat(element.childElements(null, "child"));
      fail("Child element should be removed");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }

  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotRemoveNullChildElements() {
    element.remove((SpinXmlElement[]) null);
  }

  // get child elements

  @Test
  public void canGetAllChildElements() {
    SpinList<SpinXmlElement> childElements = element.childElements();
    assertThat(childElements).hasSize(7);
  }

  @Test
  public void canGetAllChildElementsByName() {
    SpinList<SpinXmlElement> childElements = element.childElements("customer");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetAllChildElementsByNonExistingName() {
    element.childElements(NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetAllChildElementsByNullName() {
    element.childElements(null);
  }

  @Test
  public void canGetAllChildElementsByNamespaceAndName() {
    SpinList<SpinXmlElement> childElements = element.childElements(EXAMPLE_NAMESPACE, "customer");
    assertThat(childElements).hasSize(3);
  }

  @Test
  public void canGetAllChildElementsByNullNamespaceAndName() {
    SpinList<SpinXmlElement> childElements = element.childElements(null, "info");
    assertThat(childElements).hasSize(2);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetAllChildElementsByNonExistingNamespaceAndName() {
    element.childElements(NON_EXISTING, "customer");
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetAllChildElementsByNamespaceAndNonExistingName() {
    element.childElements(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetAllChildElementsByNamespaceAndNullName() {
    element.childElements(EXAMPLE_NAMESPACE, null);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotGetAllChildElementsByNonExistingNamespaceAndNonExistingName() {
    element.childElements(NON_EXISTING, NON_EXISTING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotGetAllChildElementsByNullNamespaceAndNullName() {
    element.childElements(null, null);
  }

  // replace child element

  @Test
  public void canReplaceAChildElement() {
    SpinXmlElement child = XML("<child/>");
    SpinXmlElement date = element.childElement("date");
    assertThat(date).isNotNull();

    element.replaceChild(date, child);

    assertThat(element.childElement(null, "child")).isNotNull();
    try {
      element.childElement("date");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReplaceANullChildElement() {
    SpinXmlElement child = XML("<child/>");
    element.replaceChild(null, child);
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReplaceByANullChildElement() {
    SpinXmlElement date = element.childElement("date");
    element.replaceChild(date, null);
  }

  @Test(expected = SpinXmlElementException.class)
  public void cannotReplaceANonChildElement() {
    SpinXmlElement child = XML("<child/>");
    SpinXmlElement nonChild = XML("<child/>");
    element.replaceChild(nonChild, child);
  }


  // replace element

  @Test
  public void canReplaceAElement() {
    SpinXmlElement child = XML("<child/>");
    SpinXmlElement date = element.childElement("date");
    assertThat(date).isNotNull();

    date.replace(child);

    assertThat(element.childElement(null, "child")).isNotNull();
    try {
      element.childElement("date");
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SpinXmlElementException.class);
    }
  }

  @Test
  public void canReplaceRootElement() {
    SpinXmlElement root = XML("<root/>");
    assertThat(element.name()).isEqualTo("customers");
    assertThat(element.childElements()).isNotEmpty();
    element = element.replace(root);
    assertThat(element.name()).isEqualTo("root");
    assertThat(element.childElements()).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotReplaceByNullElement() {
    element.replace(null);
  }

  // test io

  @Test
  public void canWriteToString() {
    assertThat(element.toString()).isXmlEqualTo(EXAMPLE_XML);
  }

  @Test
  public void canWriteToWriter() {
    StringWriter writer = new StringWriter();
    element.writeToWriter(writer);
    String value = writer.toString();
    assertThat(value).isXmlEqualTo(EXAMPLE_XML);
  }

  // text content

  @Test
  public void canReadTextContent() {
    assertThat(XML("<customer>Foo</customer>").textContent()).isEqualTo("Foo");
  }

  @Test
  public void canReadEmptyTextContent() {
    assertThat(XML("<customer/>").textContent()).isEmpty();
  }

  @Test
  public void canWriteTextContent() {
    assertThat(XML("<customer/>").textContent("Foo").textContent()).isEqualTo("Foo");
  }

  @Test
  public void canWriteEmptyTextContent() {
    assertThat(XML("<customer>Foo</customer>").textContent("").textContent()).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void cannotWriteNullTextContent() {
    XML("<customer/>").textContent(null);
  }

}
