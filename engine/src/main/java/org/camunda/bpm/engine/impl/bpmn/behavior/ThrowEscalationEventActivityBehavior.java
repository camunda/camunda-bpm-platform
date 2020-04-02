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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.bpmn.helper.EscalationHandler;
import org.camunda.bpm.engine.impl.bpmn.parser.Escalation;
import org.camunda.bpm.engine.impl.bpmn.parser.EscalationEventDefinition;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * The activity behavior for an intermediate throwing escalation event and an escalation end event.
 *
 * @author Philipp Ossler
 *
 */
public class ThrowEscalationEventActivityBehavior extends AbstractBpmnActivityBehavior {

  protected final Escalation escalation;

  public ThrowEscalationEventActivityBehavior(Escalation escalation) {
    this.escalation = escalation;
  }

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    final PvmActivity currentActivity = execution.getActivity();

    EscalationEventDefinition escalationEventDefinition = EscalationHandler.executeEscalation(execution, escalation.getEscalationCode());

    if (escalationEventDefinition == null || !escalationEventDefinition.isCancelActivity()) {
      leaveExecution(execution, currentActivity, escalationEventDefinition);
    }
  }

  protected void leaveExecution(ActivityExecution execution, final PvmActivity currentActivity, EscalationEventDefinition escalationEventDefinition) {

    // execution tree could have been expanded by triggering a non-interrupting event
    ExecutionEntity replacingExecution = ((ExecutionEntity) execution).getReplacedBy();

    ExecutionEntity leavingExecution = (ExecutionEntity) (replacingExecution != null ? replacingExecution : execution);
    leave(leavingExecution);
  }

}
