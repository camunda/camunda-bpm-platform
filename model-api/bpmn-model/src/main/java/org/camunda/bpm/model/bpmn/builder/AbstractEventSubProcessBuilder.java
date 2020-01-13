/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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



import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;

public class AbstractEventSubProcessBuilder <B extends AbstractEventSubProcessBuilder<B>> extends  AbstractFlowElementBuilder<B, SubProcess> {

  protected AbstractEventSubProcessBuilder(BpmnModelInstance modelInstance, SubProcess element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  public StartEventBuilder startEvent() {
    return startEvent(null);
  }

  public StartEventBuilder startEvent(String id) {
    StartEvent start = createChild(StartEvent.class, id);

    BpmnShape startShape = createBpmnShape(start);
    BpmnShape subProcessShape = findBpmnShape(getElement());

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
}


