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

import java.util.List;

import org.camunda.bpm.model.bpmn.impl.instance.BpmnModelElementInstanceImpl;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaGenericValueElement;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.impl.util.ModelUtil;
import org.camunda.bpm.model.xml.instance.DomElement;

/**
 * A helper interface for camunda extension elements which
 * hold a generic child element like camunda:inputParameter,
 * camunda:outputParameter and camunda:entry.
 *
 * @author Sebastian Menski
 */
public class CamundaGenericValueElementImpl extends BpmnModelElementInstanceImpl implements CamundaGenericValueElement {

  public CamundaGenericValueElementImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @SuppressWarnings("unchecked")
  public <T extends BpmnModelElementInstance> T getValue() {
    List<DomElement> childElements = getDomElement().getChildElements();
    if (childElements.isEmpty()) {
      return null;
    }
    else {
      return (T) ModelUtil.getModelElement(childElements.get(0), modelInstance);
    }
  }

  public void removeValue() {
    DomElement domElement = getDomElement();
    List<DomElement> childElements = domElement.getChildElements();
    for (DomElement childElement : childElements) {
      domElement.removeChild(childElement);
    }
  }

  public <T extends BpmnModelElementInstance> void setValue(T value) {
    removeValue();
    getDomElement().appendChild(value.getDomElement());
  }

}
