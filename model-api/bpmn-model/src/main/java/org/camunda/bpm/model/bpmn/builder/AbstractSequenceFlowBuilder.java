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
import org.camunda.bpm.model.bpmn.instance.ConditionExpression;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractSequenceFlowBuilder<B extends AbstractSequenceFlowBuilder<B>> extends AbstractFlowElementBuilder<B, SequenceFlow> {

  protected AbstractSequenceFlowBuilder(BpmnModelInstance modelInstance, SequenceFlow element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the source flow node of this sequence flow.
   *
   * @param source  the source of this sequence flow
   * @return the builder object
   */
  public B from(FlowNode source) {
    element.setSource(source);
    source.getOutgoing().add(element);
    return myself;
  }

  /**
   * Sets the target flow node of this sequence flow.
   *
   * @param target  the target of this sequence flow
   * @return the builder object
   */
  public B to(FlowNode target) {
    element.setTarget(target);
    target.getIncoming().add(element);
    return myself;
  }

  /**
   * Sets the condition for this sequence flow.
   *
   * @param conditionExpression  the condition expression for this sequence flow
   * @return the builder object
   */
  public B condition(ConditionExpression conditionExpression) {
    element.setConditionExpression(conditionExpression);
    return myself;
  }

}
