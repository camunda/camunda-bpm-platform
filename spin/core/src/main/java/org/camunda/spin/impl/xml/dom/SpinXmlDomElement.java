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

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.spin.impl.util.SpinEnsure.ensureChildElement;
import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

/**
 * Wrapper for a xml dom element.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomElement extends SpinXmlElement {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected Element domElement;

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
    return XmlDomDataFormat.INSTANCE.getName();
  }

  /**
   * Unwrappes the xml dom element.
   *
   * @return the unwrapped xml dom element
   */
  public Element unwrap() {
    return domElement;
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
   * Checks whether this element has a attribute with the given name.
   *
   * @param attributeName the name of the attribute
   * @return true if the element has an attribute with this name under the local namespace, false otherwise
   */
  public boolean hasAttr(String attributeName) {
    return hasAttrNs(null, attributeName);
  }

  /**
   * Checks whether this element has a attribute with the given name.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return true if the element has an attribute with this name under given namespace, false otherwise
   * @throws SpinXmlDomAttributeException if the attributeName is null
   */
  public boolean hasAttrNs(String namespace, String attributeName) {
    if (attributeName == null) {
      throw LOG.unableToCheckAttributeWithNullName();
    }
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    return domElement.hasAttributeNS(namespace, attributeName);
  }

  /**
   * Returns all wrapped attributes for the local namespace.
   *
   * @return the wrapped attributes or an empty list of no attributes are found
   */
  public SpinList<SpinXmlDomAttribute> attrs() {
    return attrs(null);
  }

  /**
   * Returns all wrapped attributes for the given namespace.
   *
   * @param namespace the namespace of the attributes
   * @return the wrapped attributes or an empty list of no attributes are found
   */
  public SpinList<SpinXmlDomAttribute> attrs(String namespace) {
    NamedNodeMap domAttributes = domElement.getAttributes();
    SpinList<SpinXmlDomAttribute> attributes = new SpinListImpl<SpinXmlDomAttribute>();
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
    SpinList<SpinXmlDomAttribute> attributes = attrs(namespace);
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
    SpinList<SpinXmlDomElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return childElements.iterator().next();
    }
  }

  /**
   * Returns all child elements of this element.
   *
   * @return list of wrapped child elements
   */
  public SpinList<SpinXmlDomElement> childElements() {
    NodeList childNodes = domElement.getChildNodes();
    SpinList<SpinXmlDomElement> childElements = new SpinListImpl<SpinXmlDomElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element) {
        childElements.add(new SpinXmlDomElement((Element) node));
      }
    }
    return childElements;
  }

  /**
   * Returns all child element with a given name in the local namespace.
   *
   * @param elementName the element name
   * @return a collection of wrapped elements
   * @throws SpinXmlDomElementException if no child element was found
   */
  public SpinList<SpinXmlDomElement> childElements(String elementName) {
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
  public SpinList<SpinXmlDomElement> childElements(String namespace, String elementName) {
    if (namespace == null) {
      namespace = namespace();
    }
    NodeList childNodes = domElement.getChildNodes();
    SpinList<SpinXmlDomElement> childElements = new SpinListImpl<SpinXmlDomElement>();
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

  /**
   * Sets the attribute value in the local namespace of the element.
   *
   * @param attributeName the name of the attribute
   * @param value the value to set
   * @return the wrapped xml dom element
   * @throws SpinXmlDomAttributeException if the name is null
   */
  public SpinXmlDomElement attr(String attributeName, String value) {
    return attrNs(null, attributeName, value);
  }

  /**
   * Sets the attribute value in the given namespace.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @param value the value to set
   * @return the wrapped xml dom element
   * @throws SpinXmlDomAttributeException if the name is null
   */
  public SpinXmlDomElement attrNs(String namespace, String attributeName, String value) {
    if (attributeName == null) {
      throw LOG.unableToCreateAttributeWithNullName();
    }
    if (value == null) {
      throw LOG.unableToSetAttributeValueToNull(namespace, attributeName);
    }
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    domElement.setAttributeNS(namespace, attributeName, value);
    return this;
  }

  /**
   * Removes the attribute under the local namespace.
   *
   * @param attributeName the name of the attribute
   * @return the wrapped xml dom element
   * @throws SpinXmlDomAttributeException if the attributeName is null
   */
  public SpinXmlDomElement removeAttr(String attributeName) {
    return removeAttrNs(null, attributeName);
  }

  /**
   * Removes the attribute under the given namespace.
   *
   * @param namespace the namespace of the attribute
   * @param attributeName the name of the attribute
   * @return the wrapped xml dom element
   * @throws SpinXmlDomAttributeException if the attributeName is null
   */
  public SpinXmlDomElement removeAttrNs(String namespace, String attributeName) {
    if (attributeName == null) {
      throw LOG.unableToRemoveAttributeWithNullName();
    }
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    domElement.removeAttributeNS(namespace, attributeName);
    return this;
  }

  /**
   * Appends child elements to this element.
   *
   * @param childElements the child elements to append
   * @return the wrapped xml dom element
   * @throws IllegalArgumentException if the child element is null
   */
  public SpinXmlDomElement append(SpinXmlDomElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlDomElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      adoptElement(childElement);
      domElement.appendChild(childElement.domElement);
    }
    return this;
  }

  /**
   * Appends a child element to this element before the existing child element.
   *
   * @param childElement the child element to append
   * @param existingChildElement the child element to append before
   * @throws IllegalArgumentException if the child element or existing child element is null
   * @throws SpinXmlDomElementException if the existing child element is not a child of this element
   */
  public SpinXmlDomElement appendBefore(SpinXmlDomElement childElement, SpinXmlDomElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    ensureChildElement(this, existingChildElement);

    adoptElement(childElement);
    domElement.insertBefore(childElement.domElement, existingChildElement.domElement);
    return this;
  }

  /**
   * Appends a child element to this element after the existing child element.
   *
   * @param childElement the child element to append
   * @param existingChildElement the child element to append after
   * @throws IllegalArgumentException if the child element or existing child element is null
   * @throws SpinXmlDomElementException if the existing child element is not a child of this element
   */
  public SpinXmlDomElement appendAfter(SpinXmlDomElement childElement, SpinXmlDomElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    ensureChildElement(this, existingChildElement);

    adoptElement(childElement);

    Node nextSibling = existingChildElement.domElement.getNextSibling();
    if (nextSibling != null) {
      domElement.insertBefore(childElement.domElement, nextSibling);
    }
    else {
      domElement.appendChild(childElement.domElement);
    }
    return this;
  }

  /**
   * Adopts a xml dom element to the owner document of this element if nessesary.
   *
   * @param elementToAdopt the element to adopt
   */
  protected void adoptElement(SpinXmlDomElement elementToAdopt) {
    Document document = this.domElement.getOwnerDocument();
    Element element = elementToAdopt.domElement;

    if (!document.equals(element.getOwnerDocument())) {
      Node node = document.adoptNode(element);
      if (node == null) {
        throw LOG.unableToAdoptElement(elementToAdopt.namespace(), elementToAdopt.name());
      }
    }
  }

}
