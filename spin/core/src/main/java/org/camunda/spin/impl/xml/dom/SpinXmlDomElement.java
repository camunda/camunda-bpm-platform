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
import org.camunda.spin.impl.SpinCollectionImpl;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.*;

import java.util.ArrayList;
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

  public String name() {
    return domElement.getLocalName();
  }

  public String namespace() {
    String namespaceURI = domElement.getNamespaceURI();
    if (namespaceURI != null) {
      return namespaceURI;
    }
    else {
      return domElement.getOwnerDocument().lookupNamespaceURI(domElement.getPrefix());
    }
  }

  public boolean hasNamespace(String namespace) {
    if (namespace == null) {
      return domElement.getNamespaceURI() == null;
    }
    else {
      return namespace.equals(namespace());
    }
  }

  public SpinXmlDomAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  public SpinXmlDomAttribute attrNs(String namespace, String attributeName) {
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    Attr attributeNode = domElement.getAttributeNodeNS(namespace, attributeName);
    if (attributeNode == null) {
      throw LOG.unableToFindAttributeWithNamespaceAndName(namespace, attributeName);
    }
    return new SpinXmlDomAttribute(attributeNode);
  }

  public SpinCollection<SpinXmlDomAttribute> attrs() {
    return attrs(null);
  }

  public SpinCollection<SpinXmlDomAttribute> attrs(String namespace) {
    NamedNodeMap domAttributes = domElement.getAttributes();
    SpinCollection<SpinXmlDomAttribute> attributes = new SpinCollectionImpl<SpinXmlDomAttribute>();
    for (int i = 0; i < domAttributes.getLength(); i++) {
      Attr attr = (Attr) domAttributes.item(i);
      if (attr != null) {
        SpinXmlDomAttribute attribute = new SpinXmlDomAttribute(attr);
        if (attribute.hasNamespace(namespace)) {
          attributes.add(attribute);
        }
      }
    }
    return attributes;
  }

  public List<String> attrNames() {
    return attrNames(null);
  }

  public List<String> attrNames(String namespace) {
    SpinCollection<SpinXmlDomAttribute> attributes = attrs(namespace);
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlDomAttribute attribute : attributes) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public SpinXmlDomElement childElement(String elementName) {
    return childElement(namespace(), elementName);
  }

  public SpinXmlDomElement childElement(String namespace, String elementName) {
    SpinCollection<SpinXmlDomElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return childElements.iterator().next();
    }
  }

  public SpinCollection<SpinXmlDomElement> childElements(String elementName) {
    return childElements(namespace(), elementName);
  }

  public SpinCollection<SpinXmlDomElement> childElements(String namespace, String elementName) {
    NodeList childNodes = domElement.getChildNodes();
    SpinCollection<SpinXmlDomElement> childElements = new SpinCollectionImpl<SpinXmlDomElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        SpinXmlDomElement childElement = new SpinXmlDomElement((Element) childNode);
        if (childElement.hasNamespace(namespace) && childElement.name().equals(elementName)) {
          childElements.add(childElement);
        }
      }
    }
    if (childElements.isEmpty()) {
      throw LOG.unableToFindChildElementWithNamespaceAndName(namespace, elementName);
    }
    return childElements;
  }

}
