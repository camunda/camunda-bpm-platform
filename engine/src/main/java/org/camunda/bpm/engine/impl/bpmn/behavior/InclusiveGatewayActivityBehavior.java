/* Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implementation of the Inclusive Gateway/OR gateway/inclusive data-based
 * gateway as defined in the BPMN specification.
 *
 * @author Tijs Rademakers
 * @author Tom Van Buskirk
 * @author Joram Barrez
 */
public class InclusiveGatewayActivityBehavior extends GatewayActivityBehavior {

  private static Logger log = Logger.getLogger(InclusiveGatewayActivityBehavior.class.getName());

  public void execute(ActivityExecution execution) throws Exception {

    execution.inactivate();
    lockConcurrentRoot(execution);

    PvmActivity activity = execution.getActivity();
    if (!activeConcurrentExecutionsExist(execution)) {

      if (log.isLoggable(Level.FINE)) {
        log.fine("inclusive gateway '" + activity.getId() + "' activates");
      }

      List<ActivityExecution> joinedExecutions = execution.findInactiveConcurrentExecutions(activity);
      String defaultSequenceFlow = (String) execution.getActivity().getProperty("default");
      List<PvmTransition> transitionsToTake = new ArrayList<PvmTransition>();

      // find matching non-default sequence flows
      for (PvmTransition outgoingTransition : execution.getActivity().getOutgoingTransitions()) {
        if (defaultSequenceFlow == null || !outgoingTransition.getId().equals(defaultSequenceFlow)) {
          Condition condition = (Condition) outgoingTransition.getProperty(BpmnParse.PROPERTYNAME_CONDITION);
          if (condition == null || condition.evaluate(execution)) {
            transitionsToTake.add(outgoingTransition);
          }
        }
      }

      // if none found, add default flow
      if (transitionsToTake.isEmpty()) {
        if (defaultSequenceFlow != null) {
          PvmTransition defaultTransition = execution.getActivity().findOutgoingTransition(defaultSequenceFlow);
          if (defaultTransition == null) {
            throw new ProcessEngineException("Default sequence flow '" + defaultSequenceFlow + "' could not be not found");
          }

          transitionsToTake.add(defaultTransition);

        } else {
          // No sequence flow could be found, not even a default one
          throw new ProcessEngineException("No outgoing sequence flow of the inclusive gateway '" + execution.getActivity().getId()
                  + "' could be selected for continuing the process");
        }
      }

      // take the flows found
      execution.leaveActivityViaTransitions(transitionsToTake, joinedExecutions);
    } else {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Inclusive gateway '" + activity.getId() + "' does not activate");
      }
    }
  }

  protected List< ? extends ActivityExecution> getLeaveExecutions(ActivityExecution parent) {
    List<ActivityExecution> executionlist = new ArrayList<ActivityExecution>();
    List< ? extends ActivityExecution> subExecutions = parent.getExecutions();
    if (subExecutions.size() == 0) {
      executionlist.add(parent);
    } else {
      for (ActivityExecution concurrentExecution : subExecutions) {
        executionlist.addAll(getLeaveExecutions(concurrentExecution));
      }
    }

    return executionlist;
  }

  public boolean activeConcurrentExecutionsExist(ActivityExecution execution) {
    PvmActivity activity = execution.getActivity();
    if (execution.isConcurrent()) {
      for (ActivityExecution concurrentExecution : getLeaveExecutions(execution.getParent())) {
        if (concurrentExecution.isActive()) {

          boolean reachable = false;
          PvmTransition pvmTransition = concurrentExecution.getTransition();
          if (pvmTransition != null) {
            reachable = isReachable(pvmTransition.getDestination(), activity, new HashSet<PvmActivity>());
          } else {
            reachable = isReachable(concurrentExecution.getActivity(), activity, new HashSet<PvmActivity>());
          }

          if (reachable) {
            if (log.isLoggable(Level.FINE)) {
              log.fine("an active concurrent execution found: '" + concurrentExecution.getActivity());
            }
            return true;
          }
        }
      }
    } else if (execution.isActive()) { // is this ever true?
      if (log.isLoggable(Level.FINE)) {
        log.fine("an active concurrent execution found: '" + execution.getActivity());
      }
      return true;
    }

    return false;
  }

  protected boolean isReachable(PvmActivity srcActivity, PvmActivity targetActivity, Set<PvmActivity> visitedActivities) {
    // if source has no outputs, it is the end of the process, and its parent process should be checked.
    if (srcActivity.getOutgoingTransitions().size() == 0) {
      visitedActivities.add(srcActivity);
      if (srcActivity.getFlowScope() == null || !(srcActivity.getFlowScope() instanceof PvmActivity)) {
        return false;
      }
      srcActivity = (PvmActivity) srcActivity.getFlowScope();
    }
    if (srcActivity.equals(targetActivity)) {
      return true;
    }

    // To avoid infinite looping, we must capture every node we visit and
    // check before going further in the graph if we have already visitied the node.
    visitedActivities.add(srcActivity);

    List<PvmTransition> transitionList = srcActivity.getOutgoingTransitions();
    if (transitionList != null && transitionList.size() > 0) {
      for (PvmTransition pvmTransition : transitionList) {
        PvmActivity destinationActivity = pvmTransition.getDestination();
        if (destinationActivity != null && !visitedActivities.contains(destinationActivity)) {
          boolean reachable = isReachable(destinationActivity, targetActivity, visitedActivities);
          // If false, we should investigate other paths, and not yet return the result
          if (reachable) {
            return true;
          }
        }
      }
    }
    return false;
  }

}