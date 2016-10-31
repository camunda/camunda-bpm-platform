/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.bpmn.parser.ConditionalEventDefinition;
import org.camunda.bpm.engine.impl.core.variable.event.VariableEvent;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Represents an interface for the condition event behaviors.
 * Makes it possible to leave the current activity if the condition of the
 * conditional event is satisfied.
 *
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
public interface ConditionalEventBehavior {

  /**
   * Returns the current conditional event definition.
   *
   * @return the conditional event definition
   */
  ConditionalEventDefinition getConditionalEventDefinition();

  /**
   * Checks the condition, on satisfaction the current activity is left.
   *
   * @param execution the execution on which the condition is evaluated
   * @param variableEvent the variableEvent to evaluate the condition
   * @param conditionalActivity the conditional activity which should be executed on satisfaction
   */
  void leaveOnSatisfiedCondition(final ExecutionEntity execution,
                                 final VariableEvent variableEvent,
                                 final ActivityImpl conditionalActivity);
}
