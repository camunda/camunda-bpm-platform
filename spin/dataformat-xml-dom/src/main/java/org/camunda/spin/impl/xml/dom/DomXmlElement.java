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

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;
import static org.camunda.commons.utils.EnsureUtil.ensureParamInstanceOf;
import static org.camunda.spin.impl.xml.dom.util.DomXmlEnsure.ensureChildElement;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.impl.xml.dom.query.DomXPathQuery;
import org.camunda.spin.spi.DataFormatMapper;
import org.camunda.spin.xml.SpinXPathQuery;
import org.camunda.spin.xml.SpinXmlAttribute;
import org.camunda.spin.xml.SpinXmlElement;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Wrapper for an xml dom element.
 *
 * @author Sebastian Menski
 */
public class DomXmlElement extends SpinXmlElement {

  private static final DomXmlLogger LOG = DomXmlLogger.XML_DOM_LOGGER;

  protected static Transformer cachedTransformer = null;

  protected static XPathFactory cachedXPathFactory;

  protected final Element domElement;
  protected final DomXmlDataFormat dataFormat;

  public DomXmlElement(Element domElement, DomXmlDataFormat dataFormat) {
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
    return domElement.getNamespaceURI();
  }

  public String prefix() {
    return domElement.getPrefix();
  }

  public boolean hasPrefix(String prefix) {
    String elementPrefix = prefix();
    if(elementPrefix == null) {
      return prefix == null;
    } else {
      return elementPrefix.equals(prefix);
    }
  }

  public boolean hasNamespace(String namespace) {
    String elementNamespace = namespace();
    if (elementNamespace == null) {
      return namespace == null;
    }
    else {
      return elementNamespace.equals(namespace);
    }
  }

  public SpinXmlAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  public SpinXmlAttribute attrNs(String namespace, String attributeName) {
    ensureNotNull("attributeName", attributeName);
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
    ensureNotNull("attributeName", attributeName);
    return domElement.hasAttributeNS(namespace, attributeName);
  }

  public SpinList<SpinXmlAttribute> attrs() {
    return new SpinListImpl<SpinXmlAttribute>(new DomXmlAttributeMapIterable(domElement, dataFormat));
  }

  public SpinList<SpinXmlAttribute> attrs(String namespace) {
    return new SpinListImpl<SpinXmlAttribute>(new DomXmlAttributeMapIterable(domElement, dataFormat, namespace));
  }

  public List<String> attrNames() {
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlAttribute attribute : attrs()) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public List<String> attrNames(String namespace) {
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlAttribute attribute : attrs(namespace)) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public String textContent() {
    return domElement.getTextContent();
  }

  public SpinXmlElement textContent(String textContent) {
    ensureNotNull("textContent", textContent);
    domElement.setTextContent(textContent);
    return this;
  }

  public SpinXmlElement childElement(String elementName) {
    return childElement(namespace(), elementName);
  }

  public SpinXmlElement childElement(String namespace, String elementName) {
    ensureNotNull("elementName", elementName);
    SpinList<SpinXmlElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return childElements.get(0);
    }
  }

  public SpinList<SpinXmlElement> childElements() {
    return new SpinListImpl<SpinXmlElement>(new DomXmlElementIterable(domElement, dataFormat));
  }

  public SpinList<SpinXmlElement> childElements(String elementName) {
    return childElements(namespace(), elementName);
  }

  public SpinList<SpinXmlElement> childElements(String namespace, String elementName) {
    ensureNotNull("elementName", elementName);
    SpinList<SpinXmlElement> childs = new SpinListImpl<SpinXmlElement>(new DomXmlElementIterable(domElement, dataFormat, namespace, elementName));
    if (childs.isEmpty()) {
      throw LOG.unableToFindChildElementWithNamespaceAndName(namespace, elementName);
    }
    return childs;
  }

  public SpinXmlElement attr(String attributeName, String value) {
    return attrNs(null, attributeName, value);
  }

  public SpinXmlElement attrNs(String namespace, String attributeName, String value) {
    ensureNotNull("attributeName", attributeName);
    ensureNotNull("value", value);

    try {
      domElement.setAttributeNS(namespace, attributeName, value);
    }
    catch (DOMException e) {
      throw LOG.unableToSetAttributeInImplementation(this, namespace, attributeName, value, e);
    }

    return this;
  }

  public SpinXmlElement removeAttr(String attributeName) {
    return removeAttrNs(null, attributeName);
  }

  public SpinXmlElement removeAttrNs(String namespace, String attributeName) {
    ensureNotNull("attributeName", attributeName);
    domElement.removeAttributeNS(namespace, attributeName);
    return this;
  }

  public SpinXmlElement append(SpinXmlElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      DomXmlElement spinDomElement = ensureParamInstanceOf("childElement", childElement, DomXmlElement.class);
      adoptElement(spinDomElement);
      try {
        domElement.appendChild(spinDomElement.domElement);
      }
      catch (DOMException e) {
        throw LOG.unableToAppendElementInImplementation(this, childElement, e);
      }
    }
    return this;
  }

  public SpinXmlElement append(Collection<SpinXmlElement> childElements) {
    ensureNotNull("childElements", childElements);
    return append(childElements.toArray(new SpinXmlElement[childElements.size()]));
  }

  public SpinXmlElement appendBefore(SpinXmlElement childElement, SpinXmlElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    DomXmlElement childDomElement = ensureParamInstanceOf("childElement", childElement, DomXmlElement.class);
    DomXmlElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, DomXmlElement.class);
    ensureChildElement(this, existingChildDomElement);

    adoptElement(childDomElement);

    try {
      domElement.insertBefore(childDomElement.domElement, existingChildDomElement.domElement);
    }
    catch (DOMException e) {
      throw LOG.unableToInsertElementInImplementation(this, childElement, e);
    }

    return this;
  }


  public SpinXmlElement appendAfter(SpinXmlElement childElement, SpinXmlElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    DomXmlElement childDomElement = ensureParamInstanceOf("childElement", childElement, DomXmlElement.class);
    DomXmlElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, DomXmlElement.class);
    ensureChildElement(this, existingChildDomElement);

    adoptElement(childDomElement);

    Node nextSibling = existingChildDomElement.domElement.getNextSibling();

    try {
      if (nextSibling != null) {
        domElement.insertBefore(childDomElement.domElement, nextSibling);
      } else {
        domElement.appendChild(childDomElement.domElement);
      }
    }
    catch (DOMException e) {
      throw LOG.unableToInsertElementInImplementation(this, childElement, e);
    }

    return this;
  }

  public SpinXmlElement remove(SpinXmlElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      DomXmlElement child = ensureParamInstanceOf("childElement", childElement, DomXmlElement.class);
      ensureChildElement(this, child);
      try {
        domElement.removeChild(child.domElement);
      }
      catch (DOMException e) {
        throw LOG.unableToRemoveChildInImplementation(this, childElement, e);
      }
    }
    return this;
  }

  public SpinXmlElement remove(Collection<SpinXmlElement> childElements) {
    ensureNotNull("childElements", childElements);
    return remove(childElements.toArray(new SpinXmlElement[childElements.size()]));
  }

  public SpinXmlElement replace(SpinXmlElement newElement) {
    ensureNotNull("newElement", newElement);
    DomXmlElement element = ensureParamInstanceOf("newElement", newElement, DomXmlElement.class);
    adoptElement(element);

    Node parentNode = domElement.getParentNode();
    if (parentNode == null) {
      throw LOG.elementHasNoParent(this);
    }

    try {
      parentNode.replaceChild(element.domElement, domElement);
    }
    catch (DOMException e) {
      throw LOG.unableToReplaceElementInImplementation(this, newElement, e);
    }
    return element;
  }

  public SpinXmlElement replaceChild(SpinXmlElement existingChildElement, SpinXmlElement newChildElement) {
    ensureNotNull("existingChildElement", existingChildElement);
    ensureNotNull("newChildElement", newChildElement);
    ensureChildElement(this, (DomXmlElement) existingChildElement);
    DomXmlElement existingChild = ensureParamInstanceOf("existingChildElement", existingChildElement, DomXmlElement.class);
    DomXmlElement newChild = ensureParamInstanceOf("newChildElement", newChildElement, DomXmlElement.class);
    adoptElement(newChild);

    try {
      domElement.replaceChild(newChild.domElement, existingChild.domElement);
    }
    catch (DOMException e) {
      throw LOG.unableToReplaceElementInImplementation(existingChild, newChildElement, e);
    }

    return this;
  }

  public SpinXPathQuery xPath(String expression) {
    XPath query = getXPathFactory().newXPath();
    return new DomXPathQuery(this, query, expression, dataFormat);
  }

  /**
   * Adopts an xml dom element to the owner document of this element if necessary.
   *
   * @param elementToAdopt the element to adopt
   */
  protected void adoptElement(DomXmlElement elementToAdopt) {
    Document document = this.domElement.getOwnerDocument();
    Element element = elementToAdopt.domElement;

    if (!document.equals(element.getOwnerDocument())) {
      Node node = document.adoptNode(element);
      if (node == null) {
        throw LOG.unableToAdoptElement(elementToAdopt);
      }
    }
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    writeToWriter(writer);
    return writer.toString();
  }

  public void writeToWriter(Writer writer) {
    dataFormat.getWriter().writeToWriter(writer, this.domElement);
  }

  /**
   * Returns a XPath Factory
   *
   * @return the XPath factory
   */
  protected XPathFactory getXPathFactory() {
    if (cachedXPathFactory == null) {
      cachedXPathFactory = XPathFactory.newInstance();
    }
    return cachedXPathFactory;
  }

  public <C> C mapTo(Class<C> javaClass) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(this.domElement, javaClass);
  }

  public <C> C mapTo(String javaClass) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(this.domElement, javaClass);
  }

}
