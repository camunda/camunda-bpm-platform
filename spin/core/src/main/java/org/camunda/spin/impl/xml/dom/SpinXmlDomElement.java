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
 * Wrapper for a xml dom element.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomElement extends SpinXmlElement {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  private final Element domElement;

  /**
   * Create a new wrapper.
   *
   * @param domElement the xml dom element to wrap
   */
  public SpinXmlDomElement(Element domElement) {
    this.domElement = domElement;
  }

  /**
   * @return the xml dom data format name
   */
  public String getDataFormatName() {
    return DomDataFormat.INSTANCE.getName();
  }

  /**
   * The local name of the element without namespace or prefix.
   *
   * @return the name of the element
   */
  public String name() {
    return domElement.getLocalName();
  }

  /**
   * The full namespace uri of the element and not the prefix.
   *
   * @return the namespace uri
   */
  public String namespace() {
    String namespaceURI = domElement.getNamespaceURI();
    if (namespaceURI != null) {
      return namespaceURI;
    }
    else {
      return domElement.getOwnerDocument().lookupNamespaceURI(domElement.getPrefix());
    }
  }

  /**
   * Checks if the element has the same namespace.
   *
   * @param namespace the namespace to test
   * @return true if the element has the same namespace, false otherwise
   */
  public boolean hasNamespace(String namespace) {
    if (namespace == null) {
      return domElement.getNamespaceURI() == null;
    }
    else {
      return namespace.equals(namespace());
    }
  }

  /**
   * Returns the wrapped attribute for the given name under
   * the local namespace.
   *
   * @param attributeName the name of the attribute
   * @return the wrapped xml dom attribute
   * @throws SpinXmlDomAttributeException if the attribute is not found
   */
  public SpinXmlDomAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  /**
   * Returns the wrapped attribute for the given namespace
   * and name.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return the wrapped xml dom attribute
   * @throws SpinXmlDomAttributeException if the attribute is not found
   */
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

  /**
   * Returns all wrapped attributes for the local namespace.
   *
   * @return the wrapped attributes or an empty list of no attributes are found
   */
  public SpinCollection<SpinXmlDomAttribute> attrs() {
    return attrs(null);
  }

  /**
   * Returns all wrapped attributes for the given namespace.
   *
   * @param namespace the namespace of the attributes
   * @return the wrapped attributes or an empty list of no attributes are found
   */
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

  /**
   * Returns all names of the attributes in the local namespace.
   *
   * @return the names of the attributes
   */
  public List<String> attrNames() {
    return attrNames(null);
  }

  /**
   * Returns all names of the attributes in the given namespace.
   *
   * @return the names of the attributes
   */
  public List<String> attrNames(String namespace) {
    SpinCollection<SpinXmlDomAttribute> attributes = attrs(namespace);
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlDomAttribute attribute : attributes) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  /**
   * Returns a single wrapped child element for the given name
   * in the local namespace.
   *
   * @param elementName the element name
   * @return the wrapped child element
   * @throws SpinXmlDomElementException if none or more than one child element is found
   */
  public SpinXmlDomElement childElement(String elementName) {
    return childElement(namespace(), elementName);
  }

  /**
   * Returns a single wrapped child element for the given namespace
   * and name.
   *
   * @param namespace the namespace of the element
   * @param elementName the element name
   * @return the wrapped child element
   * @throws SpinXmlDomElementException if none or more than one child element is found
   */
  public SpinXmlDomElement childElement(String namespace, String elementName) {
    SpinCollection<SpinXmlDomElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return childElements.iterator().next();
    }
  }

  /**
   * Returns all child element with a given name in the local namespace.
   *
   * @param elementName the element name
   * @return a collection of wrapped elements
   * @throws SpinXmlDomElementException if no child element was found
   */
  public SpinCollection<SpinXmlDomElement> childElements(String elementName) {
    return childElements(namespace(), elementName);
  }

  /**
   * Returns all child element with a given namespace and name.
   *
   * @param namespace the namespace of the element
   * @param elementName the element name
   * @return a collection of wrapped elements
   * @throws SpinXmlDomElementException if no child element was found
   */
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
