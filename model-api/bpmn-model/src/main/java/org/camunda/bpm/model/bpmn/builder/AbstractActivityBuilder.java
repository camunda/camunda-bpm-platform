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

package org.camunda.bpm.model.bpmn.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractActivityBuilder<B extends AbstractActivityBuilder<B, E>, E extends Activity> extends AbstractFlowNodeBuilder<B, E> {

  protected AbstractActivityBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  public BoundaryEventBuilder boundaryEvent() {
    return boundaryEvent(null);
  }

  public BoundaryEventBuilder boundaryEvent(String id) {
    BoundaryEvent boundaryEvent = createSibling(BoundaryEvent.class, id);
    boundaryEvent.setAttachedTo(element);

    BpmnShape boundaryEventBpmnShape = createBpmnShape(boundaryEvent);
    setBoundaryEventCoordinates(boundaryEventBpmnShape);

    return boundaryEvent.builder();
  }

  public MultiInstanceLoopCharacteristicsBuilder multiInstance() {
    MultiInstanceLoopCharacteristics miCharacteristics
      = createChild(MultiInstanceLoopCharacteristics.class);

    return miCharacteristics.builder();
  }

  /**
   * Creates a new camunda input parameter extension element with the
   * given name and value.
   *
   * @param name the name of the input parameter
   * @param value the value of the input parameter
   * @return the builder object
   */
  public B camundaInputParameter(String name, String value) {
    CamundaInputOutput camundaInputOutput = getCreateSingleExtensionElement(CamundaInputOutput.class);

    CamundaInputParameter camundaInputParameter = createChild(camundaInputOutput, CamundaInputParameter.class);
    camundaInputParameter.setCamundaName(name);
    camundaInputParameter.setTextContent(value);

    return myself;
  }

  /**
   * Creates a new camunda output parameter extension element with the
   * given name and value.
   *
   * @param name the name of the output parameter
   * @param value the value of the output parameter
   * @return the builder object
   */
  public B camundaOutputParameter(String name, String value) {
    CamundaInputOutput camundaInputOutput = getCreateSingleExtensionElement(CamundaInputOutput.class);

    CamundaOutputParameter camundaOutputParameter = createChild(camundaInputOutput, CamundaOutputParameter.class);
    camundaOutputParameter.setCamundaName(name);
    camundaOutputParameter.setTextContent(value);

    return myself;
  }

  protected double calculateXCoordinate(Bounds boundaryEventBounds) {
    BpmnShape attachedToElement = findBpmnShape(element);

    double x = 0;

    if (attachedToElement != null) {

      Bounds attachedToBounds = attachedToElement.getBounds();

      Collection<BoundaryEvent> boundaryEvents = element.getParentElement().getChildElementsByType(BoundaryEvent.class);
      Collection<BoundaryEvent> attachedBoundaryEvents = new ArrayList<BoundaryEvent>();

      Iterator<BoundaryEvent> iterator = boundaryEvents.iterator();
      while (iterator.hasNext()) {
        BoundaryEvent tmp = iterator.next();
        if (tmp.getAttachedTo().equals(element)) {
          attachedBoundaryEvents.add(tmp);
        }
      }

      double attachedToX = attachedToBounds.getX();
      double attachedToWidth = attachedToBounds.getWidth();
      double boundaryWidth = boundaryEventBounds.getWidth();

      switch (attachedBoundaryEvents.size()) {
        case 2: {
          x = attachedToX + attachedToWidth / 2 + boundaryWidth / 2;
          break;
        }
        case 3: {
          x = attachedToX + attachedToWidth / 2 - 1.5 * boundaryWidth;
          break;
        }
        default: {
          x = attachedToX + attachedToWidth / 2 - boundaryWidth / 2;
          break;
        }
      }

    }

    return x;
  }

  protected void setBoundaryEventCoordinates(BpmnShape bpmnShape) {
    BpmnShape activity = findBpmnShape(element);
    Bounds boundaryBounds = bpmnShape.getBounds();

    double x = 0;
    double y = 0;

    if (activity != null) {
      Bounds activityBounds = activity.getBounds();
      double activityY = activityBounds.getY();
      double activityHeight = activityBounds.getHeight();
      double boundaryHeight = boundaryBounds.getHeight();
      x = calculateXCoordinate(boundaryBounds);
      y = activityY + activityHeight - boundaryHeight / 2;
    }

    boundaryBounds.setX(x);
    boundaryBounds.setY(y);
  }

}
