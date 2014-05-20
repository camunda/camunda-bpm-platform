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

package org.camunda.spin.impl.xml.dom;

import org.camunda.spin.SpinCollection;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Sebastian Menski
 */
public class SpinXmlDomElement extends SpinXmlElement {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  private final Element domElement;

  public SpinXmlDomElement(Element domElement) {
    this.domElement = domElement;
  }

  public String getDataFormatName() {
    return DomDataFormat.INSTANCE.getName();
  }

  public SpinXmlDomAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  public SpinXmlDomAttribute attrNs(String namespace, String attributeName) {
    Attr attributeNode = domElement.getAttributeNodeNS(namespace, attributeName);
    if (attributeNode == null) {

    }
    return new SpinXmlDomAttribute(attributeNode);
  }

  public SpinCollection<SpinXmlDomAttribute> attrs() {
    return attrs(null);
  }

  public SpinCollection<SpinXmlDomAttribute> attrs(String namespace) {
    NamedNodeMap domAttributes = domElement.getAttributes();
    Collection<SpinXmlDomAttribute> attributes = new ArrayList<SpinXmlDomAttribute>();
    for (int i = 0; i < domAttributes.getLength(); i++) {
      Node attr = domAttributes.item(i);
      if ((namespace == null && attr.getNamespaceURI() == null) || (namespace != null && attr.getNamespaceURI().equals(namespace))) {
        attributes.add(new SpinXmlDomAttribute((Attr) attr));
      }
    }
    return (SpinCollection<SpinXmlDomAttribute>) attributes;
  }

  public List<String> attrNames() {
    return attrNames(null);
  }

  public List<String> attrNames(String namespace) {
    NamedNodeMap domAttributes = domElement.getAttributes();
    List<String> attributeNames = new ArrayList<String>();
    for (int i = 0; i < domAttributes.getLength(); i++) {
      Node attr = domAttributes.item(i);
      if ((namespace == null && attr.getNamespaceURI() == null) || (namespace != null && attr.getNamespaceURI().equals(namespace))) {
        attributeNames.add(attr.getLocalName());
      }
    }
    return attributeNames;
  }

  public SpinCollection<SpinXmlDomElement> childElements(String elementName) {
    return childElements(null, elementName);
  }

  public SpinCollection<SpinXmlDomElement> childElements(String namespace, String elementName) {
    return null;
  }

  public SpinXmlDomElement childElement(String elementName) {
    return childElement(null, elementName);
  }

  public SpinXmlDomElement childElement(String namespace, String elementName) {
    return null;
  }
}
