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

package org.camunda.bpm.model.xml.impl.instance;

import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.impl.util.XmlQName;
import org.camunda.bpm.model.xml.instance.DomDocument;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import java.util.List;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

/**
 * @author Sebastian Menski
 */
public class DomElementImpl implements DomElement {

  private static final String MODEL_ELEMENT_KEY = "camunda.modelElementRef";

  private final Element element;

  public DomElementImpl(Element element) {
    this.element = element;
  }

  protected Element getElement() {
    return element;
  }

  public String getNamespaceURI() {
    return element.getNamespaceURI();
  }

  public String getLocalName() {
    return element.getLocalName();
  }

  public DomDocument getDocument() {
    Document ownerDocument = element.getOwnerDocument();
    if (ownerDocument != null) {
      return new DomDocumentImpl(ownerDocument);
    }
    else {
      return null;
    }
  }

  public DomElement getRootElement() {
    DomDocument document = getDocument();
    if (document != null) {
      return document.getRootElement();
    }
    else {
      return null;
    }
  }

  public DomElement getParentElement() {
    Node parentNode = element.getParentNode();
    if (parentNode != null) {
      return new DomElementImpl((Element) parentNode);
    }
    else {
      return null;
    }
  }

  public List<DomElement> getChildElements() {
    NodeList childNodes = element.getChildNodes();
    return DomUtil.filterNodeListForElements(childNodes);
  }

  public List<DomElement> getChildElementsByNameNs(String namespaceUri, String elementName) {
    NodeList childNodes = element.getChildNodes();
    return DomUtil.filterNodeListByName(childNodes, namespaceUri, elementName);
  }

  public List<DomElement> getChildElementsByType(ModelInstanceImpl modelInstance, Class<? extends ModelElementInstance> elementType) {
    NodeList childNodes = element.getChildNodes();
    return DomUtil.filterNodeListByType(childNodes, modelInstance, elementType);
  }

  public void replaceChild(DomElement newChildDomElement, DomElement existingChildDomElement) {
    Element newElement = ((DomElementImpl) newChildDomElement).getElement();
    Element existingElement = ((DomElementImpl) existingChildDomElement).getElement();
    try {
      element.replaceChild(newElement, existingElement);
    }
    catch (DOMException e) {
      throw new ModelException("Unable to replace child <" + existingElement + "> of element <" + element + "> with element <" + newElement + ">", e);
    }
  }

  public boolean removeChild(DomElement childDomElement) {
    Element childElement = ((DomElementImpl) childDomElement).getElement();
    try {
      element.removeChild(childElement);
      return true;
    }
    catch (DOMException e) {
      return false;
    }
  }

  public void insertChildElementAfter(DomElement elementToInsert, DomElement insertAfter) {
    Element newElement = ((DomElementImpl) elementToInsert).getElement();

    // find node to insert before
    Node insertBeforeNode = null;
    if (insertAfter == null) {
      insertBeforeNode = element.getFirstChild();
    }
    else {
      insertBeforeNode = ((DomElementImpl) insertAfter).getElement().getNextSibling();
    }

    // insert before node or append if no node was found
    if (insertBeforeNode != null) {
      element.insertBefore(newElement, insertBeforeNode);
    }
    else {
      element.appendChild(newElement);
    }
  }

  public boolean hasAttribute(String localName) {
    return hasAttribute(getNamespaceURI(), localName);
  }

  public boolean hasAttribute(String namespaceUri, String localName) {
    return element.hasAttributeNS(namespaceUri, localName);
  }

  public String getAttribute(String attributeName) {
    return getAttribute(getNamespaceURI(), attributeName);
  }


  public String getAttribute(String namespaceUri, String localName) {
    return element.getAttributeNS(namespaceUri, localName);
  }

  public void setAttribute(String localName, String value) {
    setAttribute(getNamespaceURI(), localName, value);
  }

  public void setAttribute(String namespaceUri, String localName, String value) {
    XmlQName xmlQName = new XmlQName(this, namespaceUri, localName);
    element.setAttributeNS(xmlQName.getNamespaceUri(), xmlQName.getPrefixedName(), value);
  }

  public void setIdAttribute(String localName, String value) {
    setIdAttribute(getNamespaceURI(), localName, value);
  }

  public void setIdAttribute(String namespaceUri, String localName, String value) {
    setAttribute(namespaceUri, localName, value);
    element.setIdAttributeNS(namespaceUri, localName, true);
  }

  public void removeAttribute(String localName) {
    removeAttribute(getNamespaceURI(), localName);
  }

  public void removeAttribute(String namespaceUri, String localName) {
    element.removeAttributeNS(namespaceUri, localName);
  }

  public String getTextContent() {
    return element.getTextContent();
  }

  public void setTextContent(String textContent) {
    element.setTextContent(textContent);
  }

  public ModelElementInstance getModelElementInstance() {
    return (ModelElementInstance) element.getUserData(MODEL_ELEMENT_KEY);
  }

  public void setModelElementInstance(ModelElementInstance modelElementInstance) {
    element.setUserData(MODEL_ELEMENT_KEY, modelElementInstance, null);
  }

  public void registerNamespace(String prefix, String namespaceUri) {
    element.setAttributeNS(XMLNS_ATTRIBUTE_NS_URI, XMLNS_ATTRIBUTE + ":" + prefix, namespaceUri);
  }

  public String registerNamespace(String namespaceUri) {
    String prefix = ((DomDocumentImpl) getDocument()).getUnusedGenericNsPrefix();
    setAttribute(prefix, namespaceUri);
    return prefix;
  }

  public String lookupPrefix(String namespaceUri) {
    return element.lookupPrefix(namespaceUri);
  }


}
