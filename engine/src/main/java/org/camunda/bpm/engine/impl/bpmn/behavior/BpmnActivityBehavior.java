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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.CompensationBehavior;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

/**
 * Helper class for implementing BPMN 2.0 activities, offering convenience
 * methods specific to BPMN 2.0.
 *
 * This class can be used by inheritance or aggregation.
 *
 * @author Joram Barrez
 */
public class BpmnActivityBehavior {

  protected static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  /**
   * Performs the default outgoing BPMN 2.0 behavior, which is having parallel
   * paths of executions for the outgoing sequence flow.
   *
   * More precisely: every sequence flow that has a condition which evaluates to
   * true (or which doesn't have a condition), is selected for continuation of
   * the process instance. If multiple sequencer flow are selected, multiple,
   * parallel paths of executions are created.
   */
  public void performDefaultOutgoingBehavior(ActivityExecution activityExecution) {
    performOutgoingBehavior(activityExecution, true);
  }

  /**
   * Performs the default outgoing BPMN 2.0 behavior (@see
   * {@link #performDefaultOutgoingBehavior(ActivityExecution)}), but without
   * checking the conditions on the outgoing sequence flow.
   *
   * This means that every outgoing sequence flow is selected for continuing the
   * process instance, regardless of having a condition or not. In case of
   * multiple outgoing sequence flow, multiple parallel paths of executions will
   * be created.
   */
  public void performIgnoreConditionsOutgoingBehavior(ActivityExecution activityExecution) {
    performOutgoingBehavior(activityExecution, false);
  }

  /**
   * Actual implementation of leaving an activity.
   *
   * @param execution
   *          The current execution context
   * @param checkConditions
   *          Whether or not to check conditions before determining whether or
   *          not to take a transition.
   */
  protected void performOutgoingBehavior(ActivityExecution execution,
          boolean checkConditions) {

    LOG.leavingActivity(execution.getActivity().getId());

    String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
    List<PvmTransition> transitionsToTake = new ArrayList<>();

    List<PvmTransition> outgoingTransitions = execution.getActivity().getOutgoingTransitions();
    for (PvmTransition outgoingTransition : outgoingTransitions) {
      if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
        Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
        if (condition == null || !checkConditions || condition.evaluate(execution)) {
          transitionsToTake.add(outgoingTransition);
        }
      }
    }

    if (transitionsToTake.size() == 1) {

      execution.leaveActivityViaTransition(transitionsToTake.get(0));

    } else if (transitionsToTake.size() >= 1) {
      execution.leaveActivityViaTransitions(transitionsToTake, Arrays.asList(execution));
    } else {

      if (defaultSequenceFlow != null) {
        PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
        if (defaultTransition != null) {
          execution.leaveActivityViaTransition(defaultTransition);
        } else {
          throw LOG.missingDefaultFlowException(execution.getActivity().getId(), defaultSequenceFlow);
        }

      } else if (!outgoingTransitions.isEmpty()) {
        throw LOG.missingConditionalFlowException(execution.getActivity().getId());

      } else {

        if (((ActivityImpl) execution.getActivity()).isCompensationHandler() && isAncestorCompensationThrowing(execution)) {

         execution.endCompensation();

        } else {
          LOG.missingOutgoingSequenceFlow(execution.getActivity().getId());
          execution.end(true);
        }
      }
    }
  }

  protected boolean isAncestorCompensationThrowing(ActivityExecution execution) {
    ActivityExecution parent = execution.getParent();
    while (parent != null) {
      if (CompensationBehavior.isCompensationThrowing((PvmExecutionImpl) parent)) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

}
