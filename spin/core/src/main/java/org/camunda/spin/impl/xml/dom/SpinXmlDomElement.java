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
import org.camunda.spin.xml.tree.SpinXmlTreeAttribute;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.spin.impl.util.SpinEnsure.*;

/**
 * Wrapper for a xml dom element.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomElement extends SpinXmlTreeElement {

  private final static XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected final Element domElement;

  protected final XmlDomDataFormat dataFormat;

  public SpinXmlDomElement(Element domElement, XmlDomDataFormat dataFormat) {
    this.domElement = domElement;
    this.dataFormat = dataFormat;
  }

  public String getDataFormatName() {
    return dataFormat.getName();
  }

  public Element unwrap() {
    return domElement;
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

  public SpinXmlTreeAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  public SpinXmlTreeAttribute attrNs(String namespace, String attributeName) {
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    Attr attributeNode = domElement.getAttributeNodeNS(namespace, attributeName);
    if (attributeNode == null) {
      throw LOG.unableToFindAttributeWithNamespaceAndName(namespace, attributeName);
    }
    return dataFormat.createAttributeWrapper(attributeNode);
  }

  public boolean hasAttr(String attributeName) {
    return hasAttrNs(null, attributeName);
  }

  public boolean hasAttrNs(String namespace, String attributeName) {
    if (attributeName == null) {
      throw LOG.unableToCheckAttributeWithNullName();
    }
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    return domElement.hasAttributeNS(namespace, attributeName);
  }

  public SpinList<SpinXmlTreeAttribute> attrs() {
    return attrs(null);
  }

  public SpinList<SpinXmlTreeAttribute> attrs(String namespace) {
    NamedNodeMap domAttributes = domElement.getAttributes();
    SpinList<SpinXmlTreeAttribute> attributes = new SpinListImpl<SpinXmlTreeAttribute>();
    for (int i = 0; i < domAttributes.getLength(); i++) {
      Attr attr = (Attr) domAttributes.item(i);
      if (attr != null) {
        SpinXmlTreeAttribute attribute = dataFormat.createAttributeWrapper(attr);
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
    SpinList<SpinXmlTreeAttribute> attributes = attrs(namespace);
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlTreeAttribute attribute : attributes) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public SpinXmlTreeElement childElement(String elementName) {
    return childElement(namespace(), elementName);
  }

  public SpinXmlTreeElement childElement(String namespace, String elementName) {
    SpinList<SpinXmlTreeElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return (SpinXmlDomElement) childElements.get(0);
    }
  }

  public SpinList<SpinXmlTreeElement> childElements() {
    NodeList childNodes = domElement.getChildNodes();
    SpinList<SpinXmlTreeElement> childElements = new SpinListImpl<SpinXmlTreeElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element) {
        childElements.add(dataFormat.createElementWrapper((Element) node));
      }
    }
    return childElements;
  }

  public SpinList<SpinXmlTreeElement> childElements(String elementName) {
    return childElements(namespace(), elementName);
  }

  public SpinList<SpinXmlTreeElement> childElements(String namespace, String elementName) {
    if (namespace == null) {
      namespace = namespace();
    }
    NodeList childNodes = domElement.getChildNodes();
    SpinList<SpinXmlTreeElement> childElements = new SpinListImpl<SpinXmlTreeElement>();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        SpinXmlTreeElement childElement = dataFormat.createElementWrapper((Element) childNode);
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

  public SpinXmlTreeElement attr(String attributeName, String value) {
    return attrNs(null, attributeName, value);
  }

  public SpinXmlTreeElement attrNs(String namespace, String attributeName, String value) {
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

  public SpinXmlTreeElement removeAttr(String attributeName) {
    return removeAttrNs(null, attributeName);
  }

  public SpinXmlTreeElement removeAttrNs(String namespace, String attributeName) {
    if (attributeName == null) {
      throw LOG.unableToRemoveAttributeWithNullName();
    }
    if (hasNamespace(namespace)) {
      namespace = null;
    }
    domElement.removeAttributeNS(namespace, attributeName);
    return this;
  }

  public SpinXmlTreeElement append(SpinXmlTreeElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlTreeElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      SpinXmlDomElement spinDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
      adoptElement(spinDomElement);
      domElement.appendChild(spinDomElement.domElement);
    }
    return this;
  }

  public SpinXmlTreeElement appendBefore(SpinXmlTreeElement childElement, SpinXmlTreeElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    SpinXmlDomElement childDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
    SpinXmlDomElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, SpinXmlDomElement.class);
    ensureChildElement(this, existingChildDomElement);

    adoptElement(childDomElement);
    domElement.insertBefore(childDomElement.domElement, existingChildDomElement.domElement);
    return this;
  }


  public SpinXmlTreeElement appendAfter(SpinXmlTreeElement childElement, SpinXmlTreeElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    SpinXmlDomElement childDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
    SpinXmlDomElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, SpinXmlDomElement.class);
    ensureChildElement(this, existingChildDomElement);

    adoptElement(childDomElement);

    Node nextSibling = existingChildDomElement.domElement.getNextSibling();
    if (nextSibling != null) {
      domElement.insertBefore(childDomElement.domElement, nextSibling);
    }
    else {
      domElement.appendChild(childDomElement.domElement);
    }
    return this;
  }

  /**
   * Adopts a xml dom element to the owner document of this element if necessary.
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
