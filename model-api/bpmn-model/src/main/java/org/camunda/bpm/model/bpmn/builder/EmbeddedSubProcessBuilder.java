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

import static org.camunda.bpm.model.bpmn.builder.AbstractBaseElementBuilder.SPACE;

import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;

/**
 * @author Sebastian Menski
 */
public class EmbeddedSubProcessBuilder extends AbstractEmbeddedSubProcessBuilder<EmbeddedSubProcessBuilder, AbstractSubProcessBuilder<?>> {

  @SuppressWarnings("rawtypes")
  protected EmbeddedSubProcessBuilder(AbstractSubProcessBuilder subProcessBuilder) {
    super(subProcessBuilder, EmbeddedSubProcessBuilder.class);
  }

  public StartEventBuilder startEvent() {
    return startEvent(null);
  }

  public StartEventBuilder startEvent(String id) {
    StartEvent start = subProcessBuilder.createChild(StartEvent.class, id);

    BpmnShape startShape = subProcessBuilder.createBpmnShape(start);
    BpmnShape subProcessShape = subProcessBuilder.findBpmnShape(subProcessBuilder.getElement());

    if (subProcessShape != null) {
      Bounds subProcessBounds = subProcessShape.getBounds();
      Bounds startBounds = startShape.getBounds();

      double subProcessX = subProcessBounds.getX();
      double subProcessY = subProcessBounds.getY();
      double subProcessHeight = subProcessBounds.getHeight();
      double startHeight = startBounds.getHeight();

      startBounds.setX(subProcessX + SPACE);
      startBounds.setY(subProcessY + subProcessHeight / 2 - startHeight / 2);
    }

    return start.builder();
  }


  public EventSubProcessBuilder eventSubProcess() {
    return eventSubProcess(null);
  }

  public EventSubProcessBuilder eventSubProcess(String id) {
    // Create a subprocess, triggered by an event, and add it to modelInstance
    SubProcess subProcess = subProcessBuilder.createChild(SubProcess.class, id);
    subProcess.setTriggeredByEvent(true);

    // Create Bpmn shape so subprocess will be drawn
    BpmnShape targetBpmnShape = subProcessBuilder.createBpmnShape(subProcess);
    //find the lowest shape in the process
    // place event sub process underneath
    setCoordinates(targetBpmnShape);

    subProcessBuilder.resizeSubProcess(targetBpmnShape);

    // Return the eventSubProcessBuilder
    EventSubProcessBuilder eventSubProcessBuilder = new EventSubProcessBuilder(subProcessBuilder.modelInstance, subProcess);
    return eventSubProcessBuilder;

  }

  protected void setCoordinates(BpmnShape targetBpmnShape) {

    SubProcess eventSubProcess = (SubProcess) targetBpmnShape.getBpmnElement();
    SubProcess parentSubProcess = (SubProcess) eventSubProcess.getParentElement();
    BpmnShape parentBpmnShape = subProcessBuilder.findBpmnShape(parentSubProcess);


    Bounds targetBounds = targetBpmnShape.getBounds();
    Bounds parentBounds = parentBpmnShape.getBounds();

    // these should just be offsets maybe
    Double ycoord = parentBounds.getHeight() + parentBounds.getY();


    Double xcoord = (parentBounds.getWidth()/2) - (targetBounds.getWidth()/2) + parentBounds.getX();
    if(xcoord-parentBounds.getX() < 50.0) {
      xcoord = 50.0+parentBounds.getX();
    }

    // move target
    targetBounds.setY(ycoord);
    targetBounds.setX(xcoord);

    // parent expands automatically

    // nodes surrounding the parent subprocess will not be moved
    // they may end up inside the subprocess (but only graphically)
  }
}
