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

package org.camunda.bpm.model.xml.impl.type.reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.camunda.bpm.model.xml.ModelException;
import org.camunda.bpm.model.xml.ModelReferenceException;
import org.camunda.bpm.model.xml.UnsupportedModelOperationException;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.type.ModelElementTypeImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.reference.ElementReferenceCollection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Sebastian Menski
 */
public class ElementReferenceCollectionImpl<T extends ModelElementInstance, V extends ModelElementInstance> extends  ReferenceImpl<T> implements ElementReferenceCollection<T, V> {

  private final ChildElementCollection<V> referenceSourceCollection;
  private ModelElementTypeImpl referenceSourceType;

  public ElementReferenceCollectionImpl(ChildElementCollection<V> referenceSourceCollection) {
    this.referenceSourceCollection = referenceSourceCollection;
  }

  public ChildElementCollection<V> getReferenceSourceCollection() {
    return referenceSourceCollection;
  }

  @Override
  public void setReferenceIdentifier(ModelElementInstance referenceSourceElement, String referenceIdentifier) {
    referenceSourceElement.setTextContent(referenceIdentifier);
  }

  @SuppressWarnings("unchecked")
  private void performAddOperation(ModelElementInstanceImpl referenceSourceParentElement, T referenceTargetElement) {
    ModelInstanceImpl modelInstance = referenceSourceParentElement.getModelInstance();
    String referenceTargetIdentifier = referenceTargetAttribute.getValue(referenceTargetElement);
    ModelElementInstance existingElement = modelInstance.getModelElementById(referenceTargetIdentifier);

    if (existingElement == null || !existingElement.equals(referenceTargetElement)) {
      throw new ModelReferenceException("Cannot create reference to model element " + referenceTargetElement
        +": element is not part of model. Please connect element to the model first.");
    }
    else {
      Collection<V> referenceSourceElements = referenceSourceCollection.get(referenceSourceParentElement);
      V referenceSourceElement = modelInstance.newInstance(referenceSourceType);
      referenceSourceElements.add(referenceSourceElement);
      setReferenceIdentifier(referenceSourceElement, referenceTargetIdentifier);
    }
  }

  private void performRemoveOperation(ModelElementInstanceImpl referenceSourceParentElement, Object o) {
    referenceSourceParentElement.removeChildElement((ModelElementInstanceImpl) o);
  }

  private void performClearOperation(ModelElementInstanceImpl referenceSourceParentElement, Collection<Element> elementsToRemove) {
    for (Element element: elementsToRemove) {
      DomUtil.removeChild(referenceSourceParentElement.getDomElement(), element);
    }
  }

  public String getReferenceIdentifier(ModelElementInstance referenceSourceElement) {
    return referenceSourceElement.getTextContent();
  }

  @Override
  protected void updateReference(ModelElementInstance referenceSourceElement, String oldIdentifier, String newIdentifier) {
    String referencingTextContent = getReferenceIdentifier(referenceSourceElement);
    if (oldIdentifier != null && oldIdentifier.equals(referencingTextContent)) {
      setReferenceIdentifier(referenceSourceElement, newIdentifier);
    }
  }

  @Override
  protected void removeReference(ModelElementInstance referenceSourceElement) {
    ModelElementInstance parentElement = referenceSourceElement.getParentElement();
    Collection<V> childElementCollection = referenceSourceCollection.get(parentElement);
    childElementCollection.remove(referenceSourceElement);
  }

  public void setReferenceSourceElementType(ModelElementTypeImpl referenceSourceType) {
    this.referenceSourceType = referenceSourceType;
  }

  @Override
  protected ModelElementType getReferenceSourceElementType() {
    return referenceSourceType;
  }

  @SuppressWarnings("unchecked")
  private Collection<Element> getView(ModelElementInstanceImpl referenceSourceParentElement) {
    Document document = referenceSourceParentElement.getModelInstance().getDocument();
    Collection<V> referenceSourceElements = referenceSourceCollection.get(referenceSourceParentElement);
    Collection<Element> referenceTargetElements = new ArrayList<Element>();
    for (V referenceSourceElement : referenceSourceElements) {
      String identifier = getReferenceIdentifier(referenceSourceElement);
      Element referenceTargetElement = DomUtil.findElementById(document, identifier);
      if (referenceTargetElement != null) {
        try {
          referenceTargetElements.add(referenceTargetElement);
        }
        catch (ClassCastException e) {
          throw new ModelException("Reference found for element type " + referenceTargetElement.getClass()
            + " but expected " + referenceTargetElement.getClass());
        }
      }
      else {
        throw new ModelException("Unable to find a model element instance for id " + identifier);
      }
    }
    return referenceTargetElements;
  }

  public T getFirstReferenceTargetElement(final ModelElementInstanceImpl referenceSourceParentElement) {
    Collection<T> referenceTargetElements = getReferenceTargetElements(referenceSourceParentElement);
    if (!referenceTargetElements.isEmpty()) {
      return referenceTargetElements.iterator().next();
    }
    else {
      return null;
    }
  }

  public void setSingleTargetElement(final ModelElementInstanceImpl referenceSourceParentElement, T referenceTargetElement) {
    Collection<T> referenceTargetElements = getReferenceTargetElements(referenceSourceParentElement);
    if (!referenceTargetElements.isEmpty()) {
      referenceTargetElements.clear();
    }
    referenceTargetElements.add(referenceTargetElement);
  }

  public Collection<T> getReferenceTargetElements(final ModelElementInstanceImpl referenceSourceParentElement) {

    return new Collection<T>() {

      public int size() {
        return getView(referenceSourceParentElement).size();
      }

      public boolean isEmpty() {
        return getView(referenceSourceParentElement).isEmpty();
      }

      public boolean contains(Object o) {
        if (o == null) {
          return false;
        }
        else if (!(o instanceof ModelElementInstanceImpl)) {
          return false;
        }
        else {
          return getView(referenceSourceParentElement).contains(((ModelElementInstanceImpl)o).getDomElement());
        }
      }

      public Iterator<T> iterator() {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(referenceSourceParentElement), referenceSourceParentElement.getModelInstance());
        return modelElementCollection.iterator();
      }

      public Object[] toArray() {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(referenceSourceParentElement), referenceSourceParentElement.getModelInstance());
        return modelElementCollection.toArray();
      }

      public <T1> T1[] toArray(T1[] a) {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(referenceSourceParentElement), referenceSourceParentElement.getModelInstance());
        return modelElementCollection.toArray(a);
      }

      public boolean add(T t) {
        if (referenceSourceCollection.isImmutable()) {
          throw new UnsupportedModelOperationException("add()", "collection is immutable");
        }
        else {
          if (!contains(t)) {
            performAddOperation(referenceSourceParentElement, t);
          }
          return true;
        }
      }

      public boolean remove(Object o) {
        if (referenceSourceCollection.isImmutable()) {
          throw new UnsupportedModelOperationException("remove()", "collection is immutable");
        }
        else {
          ModelUtil.ensureInstanceOf(o, ModelElementInstanceImpl.class);
          performRemoveOperation(referenceSourceParentElement, o);
          return true;
        }
      }

      public boolean containsAll(Collection<?> c) {
        Collection<Target> modelElementCollection = ModelUtil.getModelElementCollection(getView(referenceSourceParentElement), referenceSourceParentElement.getModelInstance());
        return modelElementCollection.containsAll(c);
      }

      public boolean addAll(Collection<? extends T> c) {
        if (referenceSourceCollection.isImmutable()) {
          throw new UnsupportedModelOperationException("addAll()", "collection is immutable");
        }
        else {
          boolean result = false;
          for (T o: c) {
            result |= add(o);
          }
          return result;
        }

      }

      public boolean removeAll(Collection<?> c) {
        if (referenceSourceCollection.isImmutable()) {
          throw new UnsupportedModelOperationException("removeAll()", "collection is immutable");
        }
        else {
          boolean result = false;
          for (Object o: c) {
            result |= remove(o);
          }
          return result;
        }
      }

      public boolean retainAll(Collection<?> c) {
        throw new UnsupportedModelOperationException("retainAll()", "not implemented");
      }

      public void clear() {
        if (referenceSourceCollection.isImmutable()) {
          throw new UnsupportedModelOperationException("clear()", "collection is immutable");
        }
        else {
          Collection<Element> view = new ArrayList<Element>();
          for (V referenceSourceElement : referenceSourceCollection.get(referenceSourceParentElement)) {
            view.add(((ModelElementInstanceImpl) referenceSourceElement).getDomElement());
          }
          performClearOperation(referenceSourceParentElement, view);
        }
      }
    };
  }
}
