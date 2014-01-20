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
package org.camunda.bpm.model.xml.impl.type.child;

import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.xml.UnsupportedModelOperationException;
import org.camunda.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.camunda.bpm.model.xml.impl.util.DomUtil;
import org.camunda.bpm.model.xml.impl.util.DomUtil.ElementNodeListFilter;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <p>This collection is a view on an the children of a Model Element.</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class ChildElementCollectionImpl<T extends ModelElementInstance> implements ChildElementCollection<T> {

  /** the minimal count of child elements in the collection
   */
  private int minOccurs = 0;

  /** the maximum count of child elements in the collection.
   * An unbounded collection has a negative maxOccurs.
   */
  int maxOccurs = -1;

  /** indicates whether this collection is mutable.
   */
  private boolean isMutable = true;

  /** the containing type of the collection */
  ModelElementType containingType;

  public void setImmutable() {
    this.isMutable = false;
  }

  public boolean isImmutable() {
    return !isMutable;
  }

  // view /////////////////////////////////////////////////////////

  /**
   * <p>To be overridden by subclasses: allows subclasses to filter the
   * actual child elements of the element in order to build the collection of
   * elements to be contained in the view represented by this collection.</p>
   *
   * @param modelElement the element to check
   * @return true if the element should be contained in the collection. False otherwise.
   */
  protected abstract ElementNodeListFilter getFilter(ModelElementInstanceImpl modelElement);

  /**
   * Internal method providing access to the view represented by this collection.
   *
   * @return the view represented by this collection
   */
  Collection<Element> getView(ModelElementInstanceImpl modelElement) {
    NodeList childNodes = DomUtil.getChildNodes(modelElement.getDomElement());
    return DomUtil.filterNodeList(childNodes, getFilter(modelElement));
  }

  public int getMinOccurs() {
    return minOccurs;
  }

  public void setMinOccurs(int minOccurs) {
    this.minOccurs = minOccurs;
  }

  public int getMaxOccurs() {
    return maxOccurs;
  }

  public ModelElementType getContainingType() {
    return containingType;
  }

  public void setMaxOccurs(int maxOccurs) {
    this.maxOccurs = maxOccurs;
  }

  /** the "add" operation used by the collection */
  void performAddOperation(ModelElementInstanceImpl modelElement, T e) {
    modelElement.addChildElement(e);
  }

  /** the "remove" operation used by this collection */
  boolean performRemoveOperation(ModelElementInstanceImpl modelElement, Object e) {
    return modelElement.removeChildElement((ModelElementInstanceImpl)e);
  }

  /** the "clear" operation used by this collection */
  void performClearOperation(ModelElementInstanceImpl modelElement, Collection<Element> elementsToRemove) {
    Collection<ModelElementInstance> modelElements = ModelUtil.getModelElementCollection(elementsToRemove, modelElement.getModelInstance());
    for (ModelElementInstance element : modelElements) {
      modelElement.removeChildElement((ModelElementInstanceImpl) element);
    }
  }

  public Collection<T> get(ModelElementInstance element) {

    final ModelElementInstanceImpl modelElement = (ModelElementInstanceImpl) element;

    return new Collection<T>() {

      public boolean contains(Object o) {
        if(o == null) {
          return false;

        } else if(!(o instanceof ModelElementInstanceImpl)) {
          return false;

        } else {
          return getView(modelElement).contains(((ModelElementInstanceImpl)o).getDomElement());

        }
      }

      public boolean containsAll(Collection<?> c) {
        for (Object elementToCheck : c) {
          if(!contains(elementToCheck)) {
            return false;
          }
        }
        return true;
      }

      public boolean isEmpty() {
        return getView(modelElement).isEmpty();
      }

      public Iterator<T> iterator() {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(modelElement), modelElement.getModelInstance());
        return modelElementCollection.iterator();
      }

      public Object[] toArray() {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(modelElement), modelElement.getModelInstance());
        return modelElementCollection.toArray();
      }

      public <U> U[] toArray(U[] a) {
        Collection<T> modelElementCollection = ModelUtil.getModelElementCollection(getView(modelElement), modelElement.getModelInstance());
        return modelElementCollection.toArray(a);
      }

      public int size() {
        return getView(modelElement).size();
      }

      public boolean add(T e) {
        if(!isMutable) {
          throw new UnsupportedModelOperationException("add()", "collection is immutable");
        }
        performAddOperation(modelElement, e);
        return true;
      }

      public boolean addAll(Collection<? extends T> c) {
        if(!isMutable) {
          throw new UnsupportedModelOperationException("addAll()", "collection is immutable");
        }
        boolean result = false;
        for (T t : c) {
          result |= add(t);
        }
        return result;
      }

      public void clear() {
        if(!isMutable) {
          throw new UnsupportedModelOperationException("clear()", "collection is immutable");
        }
        Collection<Element> view = getView(modelElement);
        performClearOperation(modelElement, view);
      }

      public boolean remove(Object e) {
        if(!isMutable) {
          throw new UnsupportedModelOperationException("remove()", "collection is immutable");
        }
        ModelUtil.ensureInstanceOf(e, ModelElementInstanceImpl.class);
        return performRemoveOperation(modelElement, e);
      }

      public boolean removeAll(Collection<?> c) {
        if(!isMutable) {
          throw new UnsupportedModelOperationException("removeAll()", "collection is immutable");
        }
        boolean result = false;
        for (Object t : c) {
          result |= remove(t);
        }
        return result;
      }

      public boolean retainAll(Collection<?> c) {
        throw new UnsupportedModelOperationException("retainAll()", "not implemented");
      }

    };
  }

}
