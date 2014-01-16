/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.model.xml.impl.instance;

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.impl.type.attribute.AttributeImpl;
import org.camunda.bpm.model.xml.impl.type.reference.ReferenceImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.reference.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Base class for implementing Model Elements. </p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class ModelElementInstanceImpl implements ModelElementInstance {

  /** the wrapped DOM {@link Element} */
  private final Element domElement;

  protected final ModelInstanceImpl modelInstance;

  private final ModelElementTypeImpl elementType;

  public ModelElementInstanceImpl(ModelTypeInstanceContext instanceContext) {
    this.domElement = instanceContext.getDomElement();
    this.modelInstance = instanceContext.getModel();
    this.elementType = instanceContext.getModelType();
  }

  /**
   * @return the wrapped DOM {@link Element}
   */
  public Element getDomElement() {
    return domElement;
  }

  /**
   * Returns a child element with the given name or 'null' if no such element exists
   *
   * @param elementName the local name of the element
   * @param namespaceUri the namespace of the element
   * @return the child element or null.
   */
  public ModelElementInstance getUniqueChildElementByNameNs(String elementName, String namespaceUri) {

    NodeList childNodes = domElement.getChildNodes();

    List<Element> childElements = DomUtil.filterNodeListByName(childNodes, elementName, namespaceUri);

    if(!childElements.isEmpty()) {
      return ModelUtil.getModelElement(childElements.get(0), modelInstance);

    } else {
      return null;

    }
  }

  /**
   * Adds or replaces a child element by name. replaces an existing Child Element with the same name or adds a new child if no such element exists.
   *
   * @param newChild the child to add
   */
  public void setUniqueChildElementByNameNs(ModelElementInstance newChild) {
    ModelUtil.ensureInstanceOf(newChild, ModelElementInstanceImpl.class);
    ModelElementInstanceImpl newChildElement = (ModelElementInstanceImpl) newChild;

    Element childElement = newChildElement.getDomElement();
    ModelElementInstance existingChild = getUniqueChildElementByNameNs(childElement.getNodeName(), childElement.getNamespaceURI());
    if(existingChild == null) {
      addChildElement(newChild);

    } else {
      replaceChildElement((ModelElementInstanceImpl) existingChild, newChildElement);

    }

  }

  /**
   * Replace an existing child element with a new child element. Changes the underlying DOM element tree.
   *
   * @param existingChild the child element to replace
   * @param newChild the new child element
   */
  @SuppressWarnings("unchecked")
  void replaceChildElement(ModelElementInstanceImpl existingChild, ModelElementInstanceImpl newChild) {

    Element existingChildDomElement = existingChild.getDomElement();
    Element newChildDomElement = newChild.getDomElement();

    existingChild.unlinkAllChildReferences();

    String oldId = existingChild.getAttributeValue("id");
    if (oldId != null) {
      Collection<Attribute<?>> attributes = ((ModelElementTypeImpl) newChild.getElementType()).getAllAttributes();
      for (Attribute<?> attribute : attributes) {
        if (attribute.isIdAttribute()) {
          Attribute<String> idAttribute = (Attribute<String>) attribute;
          for (Reference<?> incomingReference : attribute.getIncomingReferences()) {
            ((ReferenceImpl<ModelElementInstance>) incomingReference).referencedElementUpdated(newChild, oldId, idAttribute.getValue(newChild));
          }
        }
      }
    }

    // replace the existing child with the new child in the DOM
    domElement.replaceChild(newChildDomElement, existingChildDomElement);
  }

  public void replaceElement(ModelElementInstance newElement) {
    ModelElementInstanceImpl parentElement = (ModelElementInstanceImpl) getParentElement();
    if (parentElement != null) {
      parentElement.replaceChildElement(this, (ModelElementInstanceImpl) newElement);
    }
    else {
      throw new ModelException("Unable to remove replace without parent");
    }
  }

  public ModelElementInstance getParentElement() {
    Element parentDomElement = DomUtil.getParentElement(domElement);
    if (parentDomElement != null) {
      return ModelUtil.getModelElement(parentDomElement, modelInstance);
    }
    else {
      return null;
    }
  }

  /**
   * Appends a new child element to the children of this element. Updates the underlying DOM element tree.
   *
   * @param newChild the new child element
   */
  public void addChildElement(ModelElementInstance newChild) {
    ModelUtil.ensureInstanceOf(newChild, ModelElementInstanceImpl.class);
    ModelElementInstanceImpl newChildElement = (ModelElementInstanceImpl) newChild;
    Element newChildDomElement = newChildElement.getDomElement();

    // add new element to the DOM
    NodeList childDomElements = domElement.getChildNodes();
    Node beforeNewChildDomElement = null;
    Node childDomElement;
    List<ModelElementType> childElementTypes = elementType.getChildElementTypes();
    int newChildTypeIndex = ModelUtil.getIndexOfElementType(newChild, childElementTypes);
    if (newChildTypeIndex == -1) {
      throw new ModelException("New child for " + elementType.getTypeName() + " is not a valid child element type: " + newChild.getElementType().getTypeName() +"; valid types are: " + childElementTypes);
    }
    for (int i = 0; i < childDomElements.getLength(); i++) {
      childDomElement = childDomElements.item(i);
      if (childDomElement.getNodeType() != Node.ELEMENT_NODE) {
        // skip not element nodes like comments
        continue;
      }
      // get model element wrapper for current DOM child element
      ModelElementInstance currentChild = ModelUtil.getModelElement((Element) childDomElement, modelInstance);
      // compare child element type with new child element type
      int childTypeIndex = ModelUtil.getIndexOfElementType(currentChild, childElementTypes);
      if (childTypeIndex == -1) {
        throw new ModelException("Child element " + currentChild.getElementType().getTypeName() + " is not a valid child element for " + elementType.getTypeName() +"; valid types are: " + childElementTypes);
      }
      if (childTypeIndex > newChildTypeIndex) {
        break;
      }
      else {
        beforeNewChildDomElement = childDomElement;
      }
    }

    // add new element to the DOM in the correct position
    if (beforeNewChildDomElement == null) {
      if (domElement.getFirstChild() == null) {
        domElement.appendChild(newChildDomElement);
      }
      else {
        domElement.insertBefore(newChildDomElement, domElement.getFirstChild());
      }
    }
    else if (beforeNewChildDomElement.getNextSibling() == null) {
      domElement.appendChild(newChildDomElement);
    }
    else {
      domElement.insertBefore(newChildDomElement, beforeNewChildDomElement.getNextSibling());
    }
  }

  public ModelInstanceImpl getModelInstance() {
    return modelInstance;
  }

  /**
   * @param child the child element to remove
   * @return true if the child element could be removed.
   */
  public boolean removeChildElement(ModelElementInstanceImpl child) {
    child.unlinkAllReferences();
    child.unlinkAllChildReferences();
    return DomUtil.removeChild(domElement, child.getDomElement());
  }

  /**
   * Return all child elements of a given type
   *
   * @param childElementType the child element type to search for
   * @return a collection of elements of the given type
   */
  Collection<ModelElementInstance> getChildElementsByType(ModelElementType childElementType) {
    List<Element> elements = DomUtil.filterNodeListByName(DomUtil.getChildNodes(domElement), childElementType.getTypeName(), childElementType.getTypeNamespace());
    return ModelUtil.getModelElementCollection(elements, modelInstance);
  }

  /**
   * Return the attribute value for the attribute name
   *
   * @param attributeName the name of the attribute
   * @return the value of the attribute
   */
  public String getAttributeValue(String attributeName) {
    return DomUtil.getAttributeValue(attributeName, domElement);
  }

  /**
   * Return the attribute value for the given attribute name and namespace URI
   *
   * @param attributeName the attribute name of the attribute
   * @param namespaceUri the namespace URI of the attribute
   * @return the value of the attribute
   */
  public String getAttributeValueNs(String attributeName, String namespaceUri) {
    return DomUtil.getAttributeValueNs(attributeName, namespaceUri, domElement);
  }

  /**
   * Set attribute value
   *
   * @param attributeName the name of the attribute
   * @param xmlValue the value to set
   * @param isIdAttribute is the attribute an ID attribute
   */
  public void setAttributeValue(String attributeName, String xmlValue, boolean isIdAttribute) {
    String oldValue = getAttributeValue(attributeName);
    DomUtil.setAttributeValue(attributeName, xmlValue, domElement);
    if(isIdAttribute) {
      DomUtil.setIdAttribute(domElement, attributeName);
    }
    Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).updateIncomingReferences(this, xmlValue, oldValue);
    }
  }

  public void setAttributeValueNs(String attributeName, String namespaceUri, String xmlValue, boolean isIdAttribute) {
    String oldValue = getAttributeValueNs(attributeName, namespaceUri);
    DomUtil.setAttributeValueNs(attributeName, namespaceUri, xmlValue, domElement);
    if(isIdAttribute) {
      DomUtil.setIdAttributeNs(domElement, attributeName, namespaceUri);
    }
    Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).updateIncomingReferences(this, xmlValue, oldValue);
    }
  }

  public void removeAttribute(String attributeName) {
    Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).unlinkReference(this);
    }
    DomUtil.removeAttribute(domElement, attributeName);
  }

  public void removeAttributeNs(String attributeName, String namespaceUri) {
    Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).unlinkReference(this);
    }
    DomUtil.removeAttributeNs(domElement, attributeName, namespaceUri);
  }

  public ModelElementType getElementType() {
    return elementType;
  }

  /**
   * Return the text content of the DOM element without leading and trailing spaces. For
   * raw text content see {@link ModelElementInstanceImpl#getRawTextContent()}.
   *
   * @return text content of underlying DOM element with leading and trailing whitespace trimmed
   */
  public String getTextContent() {
    return getRawTextContent().trim();
  }

  /**
   * Return the text content of the DOM element
   *
   * @return text content of underlying DOM element
   */
  public String getRawTextContent() {
    return DomUtil.getTextContent(domElement);
  }

  /**
   * Set the text content of the DOM element
   *
   * @param textContent the new text content
   */
  public void setTextContent(String textContent) {
    DomUtil.setTextContent(domElement, textContent);
  }

  void unlinkAllReferences() {
    Collection<Attribute<?>> attributes = elementType.getAllAttributes();
    for (Attribute<?> attribute : attributes) {
      ((AttributeImpl<?>) attribute).unlinkReference(this);
    }
  }

  void unlinkAllChildReferences() {
    List<ModelElementType> childElementTypes = elementType.getChildElementTypes();
    for (ModelElementType type : childElementTypes) {
      Collection<ModelElementInstance> childElementsForType = getChildElementsByType(type);
      for (ModelElementInstance childElement : childElementsForType) {
        ((ModelElementInstanceImpl) childElement).unlinkAllReferences();

      }
    }
  }

  @Override
  public int hashCode() {
    return domElement.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    } else if(obj == this) {
      return true;
    } else if(!(obj instanceof ModelElementInstanceImpl)) {
      return false;
    } else {
      ModelElementInstanceImpl other = (ModelElementInstanceImpl) obj;
      return other.domElement == domElement;
    }
  }

}
