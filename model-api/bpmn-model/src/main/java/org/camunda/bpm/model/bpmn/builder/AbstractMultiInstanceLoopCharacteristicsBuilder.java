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
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.CompletionCondition;
import org.camunda.bpm.model.bpmn.instance.LoopCardinality;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;

/**
 * @author Thorben Lindhauer
 *
 */
public class AbstractMultiInstanceLoopCharacteristicsBuilder<B extends AbstractMultiInstanceLoopCharacteristicsBuilder<B>>
  extends AbstractBaseElementBuilder<B, MultiInstanceLoopCharacteristics>{

  protected AbstractMultiInstanceLoopCharacteristicsBuilder(BpmnModelInstance modelInstance, MultiInstanceLoopCharacteristics element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets the multi instance loop characteristics to be sequential.
   *
   * @return  the builder object
   */
  public B sequential() {
    element.setSequential(true);
    return myself;
  }

  /**
   * Sets the multi instance loop characteristics to be parallel.
   *
   * @return  the builder object
   */
  public B parallel() {
    element.setSequential(false);
    return myself;
  }

  /**
   * Sets the cardinality expression.
   *
   * @param expression the cardinality expression
   * @return the builder object
   */
  public B cardinality(String expression) {
    LoopCardinality cardinality = getCreateSingleChild(LoopCardinality.class);
    cardinality.setTextContent(expression);

    return myself;
  }

  /**
   * Sets the completion condition expression.
   *
   * @param expression the completion condition expression
   * @return the builder object
   */
  public B completionCondition(String expression) {
    CompletionCondition condition = getCreateSingleChild(CompletionCondition.class);
    condition.setTextContent(expression);

    return myself;
  }

  /**
   * Sets the camunda collection expression.
   *
   * @param expression the collection expression
   * @return the builder object
   */
  public B camundaCollection(String expression) {
    element.setCamundaCollection(expression);

    return myself;
  }

  /**
   * Sets the camunda element variable name.
   *
   * @param variableName the name of the element variable
   * @return the builder object
   */
  public B camundaElementVariable(String variableName) {
    element.setCamundaElementVariable(variableName);

    return myself;
  }

  /**
   * Sets the multi instance loop characteristics to be asynchronous before.
   *
   * @return  the builder object
   */
  public B camundaAsyncBefore() {
    element.setCamundaAsyncBefore(true);
    return myself;
  }

  /**
   * Sets the multi instance loop characteristics to be asynchronous after.
   *
   * @return  the builder object
   */
  public B camundaAsyncAfter() {
    element.setCamundaAsyncAfter(true);
    return myself;
  }

  /**
   * Finishes the building of a multi instance loop characteristics.
   *
   * @return the parent activity builder
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public <T extends AbstractActivityBuilder> T multiInstanceDone() {
    return (T) ((Activity) element.getParentElement()).builder();
  }

}
