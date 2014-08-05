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
import org.camunda.spin.xml.tree.SpinXmlTreeElementException;
import org.camunda.spin.xml.tree.SpinXmlTreeXPathQuery;
import org.w3c.dom.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.camunda.spin.impl.util.SpinEnsure.*;

/**
 * Wrapper for an xml dom element.
 *
 * @author Sebastian Menski
 */
public class SpinXmlDomElement extends SpinXmlTreeElement {

  private static final XmlDomLogger LOG = SpinLogger.XML_DOM_LOGGER;

  protected static Transformer cachedTransformer = null;

  protected static XPathFactory cachedXPathFactory;

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
    return domElement.getNamespaceURI();
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

  public SpinXmlTreeAttribute attr(String attributeName) {
    return attrNs(null, attributeName);
  }

  public SpinXmlTreeAttribute attrNs(String namespace, String attributeName) {
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

  public SpinList<SpinXmlTreeAttribute> attrs() {
    return new SpinListImpl<SpinXmlTreeAttribute>(new SpinXmlDomAttributeMapIterable(domElement, dataFormat));
  }

  public SpinList<SpinXmlTreeAttribute> attrs(String namespace) {
    return new SpinListImpl<SpinXmlTreeAttribute>(new SpinXmlDomAttributeMapIterable(domElement, dataFormat, namespace));
  }

  public List<String> attrNames() {
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlTreeAttribute attribute : attrs()) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public List<String> attrNames(String namespace) {
    List<String> attributeNames = new ArrayList<String>();
    for (SpinXmlTreeAttribute attribute : attrs(namespace)) {
      attributeNames.add(attribute.name());
    }
    return attributeNames;
  }

  public String textContent() {
    return domElement.getTextContent();
  }

  public SpinXmlTreeElement textContent(String textContent) {
    ensureNotNull("textContent", textContent);
    domElement.setTextContent(textContent);
    return this;
  }

  public SpinXmlTreeElement childElement(String elementName) {
    return childElement(namespace(), elementName);
  }

  public SpinXmlTreeElement childElement(String namespace, String elementName) {
    ensureNotNull("elementName", elementName);
    SpinList<SpinXmlTreeElement> childElements = childElements(namespace, elementName);
    if (childElements.size() > 1) {
      throw LOG.moreThanOneChildElementFoundForNamespaceAndName(namespace, elementName);
    }
    else {
      return childElements.get(0);
    }
  }

  public SpinList<SpinXmlTreeElement> childElements() {
    return new SpinListImpl<SpinXmlTreeElement>(new SpinXmlDomElementIterable(domElement, dataFormat));
  }

  public SpinList<SpinXmlTreeElement> childElements(String elementName) {
    return childElements(namespace(), elementName);
  }

  public SpinList<SpinXmlTreeElement> childElements(String namespace, String elementName) {
    ensureNotNull("elementName", elementName);
    SpinList<SpinXmlTreeElement> childs = new SpinListImpl<SpinXmlTreeElement>(new SpinXmlDomElementIterable(domElement, dataFormat, namespace, elementName));
    if (childs.isEmpty()) {
      throw LOG.unableToFindChildElementWithNamespaceAndName(namespace, elementName);
    }
    return childs;
  }

  public SpinXmlTreeElement attr(String attributeName, String value) {
    return attrNs(null, attributeName, value);
  }

  public SpinXmlTreeElement attrNs(String namespace, String attributeName, String value) {
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

  public SpinXmlTreeElement removeAttr(String attributeName) {
    return removeAttrNs(null, attributeName);
  }

  public SpinXmlTreeElement removeAttrNs(String namespace, String attributeName) {
    ensureNotNull("attributeName", attributeName);
    domElement.removeAttributeNS(namespace, attributeName);
    return this;
  }

  public SpinXmlTreeElement append(SpinXmlTreeElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlTreeElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      SpinXmlDomElement spinDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
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

  public SpinXmlTreeElement append(Collection<SpinXmlTreeElement> childElements) {
    ensureNotNull("childElements", childElements);
    return append(childElements.toArray(new SpinXmlTreeElement[childElements.size()]));
  }

  public SpinXmlTreeElement appendBefore(SpinXmlTreeElement childElement, SpinXmlTreeElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    SpinXmlDomElement childDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
    SpinXmlDomElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, SpinXmlDomElement.class);
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


  public SpinXmlTreeElement appendAfter(SpinXmlTreeElement childElement, SpinXmlTreeElement existingChildElement) {
    ensureNotNull("childElement", childElement);
    ensureNotNull("existingChildElement", existingChildElement);
    SpinXmlDomElement childDomElement = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
    SpinXmlDomElement existingChildDomElement = ensureParamInstanceOf("existingChildElement", existingChildElement, SpinXmlDomElement.class);
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

  public SpinXmlTreeElement remove(SpinXmlTreeElement... childElements) {
    ensureNotNull("childElements", childElements);
    for (SpinXmlTreeElement childElement : childElements) {
      ensureNotNull("childElement", childElement);
      SpinXmlDomElement child = ensureParamInstanceOf("childElement", childElement, SpinXmlDomElement.class);
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

  public SpinXmlTreeElement remove(Collection<SpinXmlTreeElement> childElements) {
    ensureNotNull("childElements", childElements);
    return remove(childElements.toArray(new SpinXmlTreeElement[childElements.size()]));
  }

  public SpinXmlTreeElement replace(SpinXmlTreeElement newElement) {
    ensureNotNull("newElement", newElement);
    SpinXmlDomElement element = ensureParamInstanceOf("newElement", newElement, SpinXmlDomElement.class);
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

  public SpinXmlTreeElement replaceChild(SpinXmlTreeElement existingChildElement, SpinXmlTreeElement newChildElement) {
    ensureNotNull("existingChildElement", existingChildElement);
    ensureNotNull("newChildElement", newChildElement);
    ensureChildElement(this, (SpinXmlDomElement) existingChildElement);
    SpinXmlDomElement existingChild = ensureParamInstanceOf("existingChildElement", existingChildElement, SpinXmlDomElement.class);
    SpinXmlDomElement newChild = ensureParamInstanceOf("newChildElement", newChildElement, SpinXmlDomElement.class);
    adoptElement(newChild);

    try {
      domElement.replaceChild(newChild.domElement, existingChild.domElement);
    }
    catch (DOMException e) {
      throw LOG.unableToReplaceElementInImplementation(existingChild, newChildElement, e);
    }

    return this;
  }

  public SpinXmlTreeXPathQuery xPath(String expression) {
    try {
      XPathExpression query = getXPathFactory().newXPath().compile(expression);
      return new SpinXmlDomXPathQuery(this, query, dataFormat);
    } catch (XPathExpressionException e) {
      throw LOG.unableToCompileXPathQuery(expression, e);
    }
  }

  /**
   * Adopts an xml dom element to the owner document of this element if necessary.
   *
   * @param elementToAdopt the element to adopt
   */
  protected void adoptElement(SpinXmlDomElement elementToAdopt) {
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
    return writeToWriter(new StringWriter()).toString();
  }

  public OutputStream toStream() {
    return writeToStream(new ByteArrayOutputStream());
  }

  public <T extends OutputStream> T writeToStream(T outputStream) {
    writeToStreamResult(new StreamResult(outputStream));
    return outputStream;
  }

  public <T extends Writer> T writeToWriter(T writer) {
    writeToStreamResult(new StreamResult(writer));
    return writer;
  }

  /**
   * Writes the dom element to a stream result.
   *
   * @param streamResult the stream result to transform in
   * @throws SpinXmlTreeElementException if the element cannot be transformed or no new transformer can be created
   */
  protected void writeToStreamResult(StreamResult streamResult) {
    DOMSource domSource = new DOMSource(domElement);
    try {
      getTransformer().transform(domSource, streamResult);
    } catch (TransformerException e) {
      throw LOG.unableToTransformElement(this, e);
    }
  }

  /**
   * Returns a configured transformer to write XML. Creates a new one
   * if non is cached.
   *
   * @return the XML configured transformer
   * @throws SpinXmlTreeElementException if no new transformer can be created
   */
  protected Transformer getTransformer() {
    if (cachedTransformer == null) {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      try {
        cachedTransformer = transformerFactory.newTransformer();
      } catch (TransformerConfigurationException e) {
        throw LOG.unableToCreateTransformer(e);
      }
      cachedTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      cachedTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
      cachedTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    }
    return cachedTransformer;
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

}
