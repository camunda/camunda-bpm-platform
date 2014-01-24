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

import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.impl.type.attribute.AttributeImpl;
import org.camunda.bpm.model.xml.impl.type.reference.ReferenceImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.reference.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.List;

/**
 * Base class for implementing Model Elements.
 *
 * @author Daniel Meyer
 *
 */
public abstract class ModelElementInstanceImpl implements ModelElementInstance {

  /** the containing model instance */
  protected final ModelInstanceImpl modelInstance;
  /** the wrapped DOM {@link Element} */
  private final Element domElement;
  /** the implementing model element type */
  private final ModelElementTypeImpl elementType;

  public ModelElementInstanceImpl(final ModelTypeInstanceContext instanceContext) {
    this.domElement = instanceContext.getDomElement();
    this.modelInstance = instanceContext.getModel();
    this.elementType = instanceContext.getModelType();
  }

  public Element getDomElement() {
    return domElement;
  }

  public ModelInstanceImpl getModelInstance() {
    return modelInstance;
  }

  public ModelElementInstance getParentElement() {
    final Element parentDomElement = DomUtil.getParentElement(domElement);
    if (parentDomElement != null) {
      return ModelUtil.getModelElement(parentDomElement, modelInstance);
    }
    else {
      return null;
    }
  }

  public ModelElementType getElementType() {
    return elementType;
  }

  public String getAttributeValue(final String attributeName) {
    return DomUtil.getAttributeValue(attributeName, domElement);
  }

  public void setAttributeValue(final String attributeName, final String xmlValue, final boolean isIdAttribute) {
    final String oldValue = getAttributeValue(attributeName);
    DomUtil.setAttributeValue(attributeName, xmlValue, domElement);
    if(isIdAttribute) {
      DomUtil.setIdAttribute(domElement, attributeName);
    }
    final Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).updateIncomingReferences(this, xmlValue, oldValue);
    }
  }

  public void removeAttribute(final String attributeName) {
    final Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      final Object identifier = attribute.getValue(this);
      if (identifier != null) {
        ((AttributeImpl<?>) attribute).unlinkReference(this, identifier);
      }
    }
    DomUtil.removeAttribute(domElement, attributeName);
  }

  public String getAttributeValueNs(final String attributeName, final String namespaceUri) {
    return DomUtil.getAttributeValueNs(attributeName, namespaceUri, domElement);
  }

  public void setAttributeValueNs(final String attributeName, final String namespaceUri, final String xmlValue, final boolean isIdAttribute) {
    final String oldValue = getAttributeValueNs(attributeName, namespaceUri);
    DomUtil.setAttributeValueNs(attributeName, namespaceUri, xmlValue, domElement);
    if(isIdAttribute) {
      DomUtil.setIdAttributeNs(domElement, attributeName, namespaceUri);
    }
    final Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      ((AttributeImpl<?>) attribute).updateIncomingReferences(this, xmlValue, oldValue);
    }
  }

  public void removeAttributeNs(final String attributeName, final String namespaceUri) {
    final Attribute<?> attribute = elementType.getAttribute(attributeName);
    if (attribute != null) {
      final Object identifier = attribute.getValue(this);
      if (identifier != null) {
        ((AttributeImpl<?>) attribute).unlinkReference(this, identifier);
      }
    }
    DomUtil.removeAttributeNs(domElement, attributeName, namespaceUri);
  }

  public String getTextContent() {
    return getRawTextContent().trim();
  }

  public void setTextContent(final String textContent) {
    DomUtil.setTextContent(domElement, textContent);
  }

  public String getRawTextContent() {
    return DomUtil.getTextContent(domElement);
  }

  /**
   * Returns a child element with the given name or 'null' if no such element exists
   *
   * @param elementName the local name of the element
   * @param namespaceUri the namespace of the element
   * @return the child element or null.
   */
  public ModelElementInstance getUniqueChildElementByNameNs(final String elementName, final String namespaceUri) {
    final NodeList childNodes = domElement.getChildNodes();
    final List<Element> childElements = DomUtil.filterNodeListByName(childNodes, elementName, namespaceUri);

    if(!childElements.isEmpty()) {
      return ModelUtil.getModelElement(childElements.get(0), modelInstance);
    } else {
      return null;
    }
  }

  /**
   * Returns a child element with the given type
   *
   * @param elementType  the type of the element
   * @return the child element or null
   */
  public ModelElementInstance getUniqueChildElementByType(final Class<?> elementType) {
    final NodeList childNodes = domElement.getChildNodes();
    final List<Element> childElements = DomUtil.filterNodeListByType(childNodes, elementType, modelInstance);

    if(!childElements.isEmpty()) {
      return ModelUtil.getModelElement(childElements.get(0), modelInstance);
    }
    else {
      return null;
    }
  }

  /**
   * Adds or replaces a child element by name. Replaces an existing Child Element with the same name
   * or adds a new child if no such element exists.
   *
   * @param newChild the child to add
   */
  public void setUniqueChildElementByNameNs(final ModelElementInstance newChild) {
    ModelUtil.ensureInstanceOf(newChild, ModelElementInstanceImpl.class);
    final ModelElementInstanceImpl newChildElement = (ModelElementInstanceImpl) newChild;

    final Element childElement = newChildElement.getDomElement();
    final ModelElementInstance existingChild = getUniqueChildElementByNameNs(childElement.getNodeName(), childElement.getNamespaceURI());
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
  protected void replaceChildElement(final ModelElementInstanceImpl existingChild, final ModelElementInstanceImpl newChild) {
    final Element existingChildDomElement = existingChild.getDomElement();
    final Element newChildDomElement = newChild.getDomElement();

    // unlink (remove all references) of child elements
    existingChild.unlinkAllChildReferences();

    // update incoming references from old to new child element
    updateIncomingReferences(existingChild, newChild);

    // replace the existing child with the new child in the DOM
    domElement.replaceChild(newChildDomElement, existingChildDomElement);
  }

  @SuppressWarnings("unchecked")
  private void updateIncomingReferences(final ModelElementInstanceImpl oldInstance, final ModelElementInstanceImpl newInstance) {
    final String oldId = oldInstance.getAttributeValue("id");
    final String newId = newInstance.getAttributeValue("id");

    if (oldId == null || newId == null) {
      return;
    }

    final Collection<Attribute<?>> attributes = ((ModelElementTypeImpl) oldInstance.getElementType()).getAllAttributes();
    for (Attribute<?> attribute : attributes) {
      if (attribute.isIdAttribute()) {
        for (Reference<?> incomingReference : attribute.getIncomingReferences()) {
          ((ReferenceImpl<ModelElementInstance>) incomingReference).referencedElementUpdated(newInstance, oldId, newId);
        }
      }
    }

  }

  public void replaceWithElement(final ModelElementInstance newElement) {
    final ModelElementInstanceImpl parentElement = (ModelElementInstanceImpl) getParentElement();
    if (parentElement != null) {
      parentElement.replaceChildElement(this, (ModelElementInstanceImpl) newElement);
    }
    else {
      throw new ModelException("Unable to remove replace without parent");
    }
  }

  /**
   * Appends a new child element to the children of this element. Updates the underlying DOM element tree.
   *
   * @param newChild the new child element
   */
  public void addChildElement(final ModelElementInstance newChild) {
    ModelUtil.ensureInstanceOf(newChild, ModelElementInstanceImpl.class);
    final ModelElementInstanceImpl newChildElement = (ModelElementInstanceImpl) newChild;

    final ModelElementInstance elementToInsertAfter = findElementToInsertAfter(newChild);
    insertElementAfter(newChild, elementToInsertAfter);
  }

  /**
   * Removes the child element from this.
   *
   * @param child  the child element to remove
   * @return true if the child element could be removed
   */
  public boolean removeChildElement(final ModelElementInstanceImpl child) {
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
  private Collection<ModelElementInstance> getChildElementsByType(final ModelElementType childElementType) {
    final List<Element> elements = DomUtil.filterNodeListByName(DomUtil.getChildNodes(domElement), childElementType.getTypeName(), childElementType.getTypeNamespace());
    return ModelUtil.getModelElementCollection(elements, modelInstance);
  }

  /**
   * Returns the element after which the new element should be inserted in the DOM document.
   *
   * @param elementToInsert  the new element to insert
   * @return the element to insert after or null
   */
  private ModelElementInstance findElementToInsertAfter(final ModelElementInstance elementToInsert) {
    final List<ModelElementType> childElementTypes = elementType.getChildElementTypes();
    final List<Element> childDomElements = DomUtil.filterNodeList(domElement.getChildNodes(), new DomUtil.ElementNodeListFilter());
    final Collection<ModelElementInstance> childElements = ModelUtil.getModelElementCollection(childDomElements, modelInstance);

    ModelElementInstance insertAfterElement = null;
    final int newElementTypeIndex = ModelUtil.getIndexOfElementType(elementToInsert, childElementTypes);
    for (ModelElementInstance childElement : childElements) {
      final int childElementTypeIndex = ModelUtil.getIndexOfElementType(childElement, childElementTypes);
      if (newElementTypeIndex >= childElementTypeIndex) {
        insertAfterElement = childElement;
      }
      else {
        break;
      }
    }
    return insertAfterElement;
  }

  /**
   * Inserts the new element after the given element or at the beginning if the given element is null.
   *
   * @param elementToInsert  the new element to insert
   * @param insertAfterElement  the element to insert after or null
   */
  private void insertElementAfter(final ModelElementInstance elementToInsert, final ModelElementInstance insertAfterElement) {
    final Element domElementToInsert = elementToInsert.getDomElement();
    if (insertAfterElement == null) {
      final Node firstChild = domElement.getFirstChild();
      if (firstChild == null) {
        domElement.appendChild(domElementToInsert);
      }
      else {
        domElement.insertBefore(domElementToInsert, firstChild);
      }
    }
    else {
      final Node insertBeforeElement = insertAfterElement.getDomElement().getNextSibling();
      if (insertBeforeElement == null) {
        domElement.appendChild(domElementToInsert);
      }
      else {
        domElement.insertBefore(domElementToInsert, insertBeforeElement);
      }
    }
  }

  /**
   * Removes all reference to this.
   */
  private void unlinkAllReferences() {
    final Collection<Attribute<?>> attributes = elementType.getAllAttributes();
    for (Attribute<?> attribute : attributes) {
      final Object identifier = attribute.getValue(this);
      if (identifier != null) {
        ((AttributeImpl<?>) attribute).unlinkReference(this, identifier);
      }
    }
  }

  /**
   * Removes every reference to children of this.
   */
  private void unlinkAllChildReferences() {
    final List<ModelElementType> childElementTypes = elementType.getChildElementTypes();
    for (ModelElementType type : childElementTypes) {
      final Collection<ModelElementInstance> childElementsForType = getChildElementsByType(type);
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
