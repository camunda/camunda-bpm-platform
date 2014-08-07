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

package org.camunda.bpm.model.bpmn.impl.instance.camunda;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_ELEMENT_LIST;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaList;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.UnsupportedModelOperationException;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * @author Sebastian Menski
 */
public class CamundaListImpl extends BpmnModelElementInstanceImpl implements CamundaList {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaList.class, CAMUNDA_ELEMENT_LIST)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaList>() {
        public CamundaList newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaListImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public CamundaListImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @SuppressWarnings("unchecked")
  public <T extends BpmnModelElementInstance> Collection<T> getValues() {

    return new Collection<T>() {

      protected Collection<T> getElements() {
        return ModelUtil.getModelElementCollection(getDomElement().getChildElements(), getModelInstance());
      }

      public int size() {
        return getElements().size();
      }

      public boolean isEmpty() {
        return getElements().isEmpty();
      }

      public boolean contains(Object o) {
        return getElements().contains(o);
      }

      public Iterator<T> iterator() {
        return (Iterator<T>) getElements().iterator();
      }

      public Object[] toArray() {
        return getElements().toArray();
      }

      public <T1> T1[] toArray(T1[] a) {
        return getElements().toArray(a);
      }

      public boolean add(T t) {
        getDomElement().appendChild(t.getDomElement());
        return true;
      }

      public boolean remove(Object o) {
        ModelUtil.ensureInstanceOf(o, BpmnModelElementInstance.class);
        return getDomElement().removeChild(((BpmnModelElementInstance) o).getDomElement());
      }

      public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
          if (!contains(o)) {
            return false;
          }
        }
        return true;
      }

      public boolean addAll(Collection<? extends T> c) {
        for (T element : c) {
          add(element);
        }
        return true;
      }

      public boolean removeAll(Collection<?> c) {
        boolean result = false;
        for (Object o : c) {
          result |= remove(o);
        }
        return result;
      }

      public boolean retainAll(Collection<?> c) {
        throw new UnsupportedModelOperationException("retainAll()", "not implemented");
      }

      public void clear() {
        DomElement domElement = getDomElement();
        List<DomElement> childElements = domElement.getChildElements();
        for (DomElement childElement : childElements) {
          domElement.removeChild(childElement);
        }
      }
    };
  }

}
