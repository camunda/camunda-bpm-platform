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

package org.camunda.spin.xml;

import org.camunda.spin.SpinCollection;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttribute;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttributeException;
import org.camunda.spin.impl.xml.dom.SpinXmlDomElement;
import org.camunda.spin.impl.xml.dom.SpinXmlDomElementException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.xml.XmlTestConstants.*;

/**
 * @author Sebastian Menski
 */
public class SpinXmlDomElementTest {

  protected SpinXmlDomElement element;

  @Before
  public void parseXml() {
    element = S(exampleXmlFileAsStream());
  }

  @Test
  public void canReadAttributeByName() {
    SpinXmlDomAttribute attribute = element.attr("order");
    String value = attribute.stringValue();
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  public void cannotReadAttributeByNonExistingName() {
    element.attr(NON_EXISTING);
  }

  @Test
  public void canReadAttributeByNamespaceAndName() {
    SpinXmlDomAttribute attribute = element.attrNs(EXAMPLE_NAMESPACE, "order");
    String value = attribute.stringValue();
    assertThat(value).isEqualTo("order1");
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  public void cannotReadAttributeByNonExistingNameAndNamespace() {
    element.attrNs(NON_EXISTING, NON_EXISTING);
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  public void cannotReadAttributeByNameAndWrongNamespace() {
    element.attrNs(NON_EXISTING, "order");
  }

  @Test(expected = SpinXmlDomAttributeException.class)
  public void cannotReadAttributeByNamespaceAndWrongName() {
    element.attrNs(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test
  public void canGetAllAttributes() {
    SpinCollection<SpinXmlDomAttribute> attributes = element.attrs();
    for (SpinXmlDomAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
      assertThat(attribute.stringValue()).isIn("order1", "20150112");
    }
  }

  @Test
  public void canGetAllAttributesByNamespace() {
    SpinCollection<SpinXmlDomAttribute> attributes = element.attrs(EXAMPLE_NAMESPACE);
    for (SpinXmlDomAttribute attribute : attributes) {
      assertThat(attribute.name()).isIn("order", "dueUntil");
      assertThat(attribute.stringValue()).isIn("order1", "20150112");
      assertThat(attribute.namespace()).isEqualTo(EXAMPLE_NAMESPACE);
    }
  }

  @Test
  public void canGetAllAttributesByNonExistingNamespace() {
    SpinCollection<SpinXmlDomAttribute> attributes = element.attrs(NON_EXISTING);
    assertThat(attributes).isEmpty();
  }

  @Test
  public void canGetAllAttributeNames() {
    List<String> names = element.attrNames();
    assertThat(names).containsOnly("order", "dueUntil");
  }

  @Test
  public void canGetAllAttributeNamesByNamespace() {
    List<String> names = element.attrNames(EXAMPLE_NAMESPACE);
    assertThat(names).containsOnly("order", "dueUntil");
  }

  @Test
  public void canGetAllAttributeNamesByNonExistingNamespace() {
    List<String> names = element.attrNames(NON_EXISTING);
    assertThat(names).isEmpty();
  }

  @Test
  public void canGetSingleChildElementByName() {
    SpinXmlDomElement childElement = element.childElement("date");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").stringValue()).isEqualTo("20140512");;
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetSingleChildElementByNonExistingName() {
    element.childElement(NON_EXISTING);
  }

  @Test
  public void canGetSingleChildElementByNameAndNamespace() {
    SpinXmlDomElement childElement = element.childElement(EXAMPLE_NAMESPACE, "date");
    assertThat(childElement).isNotNull();
    assertThat(childElement.attr("name").stringValue()).isEqualTo("20140512");
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetChildElementByNamespaceAndNonExistingName() {
    element.childElement(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetChildElementByNonExistingNamespaceAndName() {
    element.childElement(NON_EXISTING, "date");
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetChildElementByNonExistingNamespaceAndNonExistingName() {
    element.childElement(NON_EXISTING, NON_EXISTING);
  }

  @Test
  public void canGetAllChildElementsByName() {
    SpinCollection<SpinXmlDomElement> childElements = element.childElements("customer");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetAllChildElementsByNonExistingName() {
    SpinCollection<SpinXmlDomElement> childElements = element.childElements(NON_EXISTING);
  }

  @Test
  public void canGetAllChildElementsByNamespaceAndName() {
    SpinCollection<SpinXmlDomElement> childElements = element.childElements(EXAMPLE_NAMESPACE, "customer");
    assertThat(childElements).hasSize(3);
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetAllChildElementsByNonExistingNamespaceAndName() {
    element.childElements(NON_EXISTING, "customer");
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetAllChildElementsByNamespaceAndNonExistingName() {
    element.childElements(EXAMPLE_NAMESPACE, NON_EXISTING);
  }

  @Test(expected = SpinXmlDomElementException.class)
  public void cannotGetAllChildElementsByNonExistingNamespaceAndNonExistingName() {
    element.childElements(NON_EXISTING, NON_EXISTING);
  }

}
