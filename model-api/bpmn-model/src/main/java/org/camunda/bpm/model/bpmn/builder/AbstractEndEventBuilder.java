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
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractEndEventBuilder<B extends AbstractEndEventBuilder<B>> extends AbstractThrowEventBuilder<B, EndEvent> {

  protected AbstractEndEventBuilder(BpmnModelInstance modelInstance, EndEvent element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets an error definition for the given error code. If already an error
   * with this code exists it will be used, otherwise a new error is created.
   *
   * @param errorCode the code of the error
   * @return the builder object
   */
  public B error(String errorCode) {
    return error(errorCode, null);
  }

  /**
   * Sets an error definition for the given error code. If already an error
   * with this code exists it will be used, otherwise a new error is created
   * with the given errorMessage.
   *
   * @param errorCode the code of the error
   * @param errorMessage the error message that is used when a new error needs
   *        to be created
   * @return the builder object
   */
  public B error(String errorCode, String errorMessage) {
    ErrorEventDefinition errorEventDefinition = createErrorEventDefinition(errorCode, errorMessage);
    element.getEventDefinitions().add(errorEventDefinition);
    
    return myself;
  }
}
