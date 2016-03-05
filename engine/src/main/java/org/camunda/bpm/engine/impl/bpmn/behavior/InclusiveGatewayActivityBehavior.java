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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.impl.Condition;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

/**
 * Implementation of the Inclusive Gateway/OR gateway/inclusive data-based
 * gateway as defined in the BPMN specification.
 *
 * @author Tijs Rademakers
 * @author Tom Van Buskirk
 * @author Joram Barrez
 */
public class InclusiveGatewayActivityBehavior extends GatewayActivityBehavior {

  protected static BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  public void execute(ActivityExecution execution) throws Exception {

    execution.inactivate();
    lockConcurrentRoot(execution);

    PvmActivity activity = execution.getActivity();
    if (activatesGateway(execution, activity)) {

      LOG.activityActivation(activity.getId());

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
            throw LOG.missingDefaultFlowException(execution.getActivity().getId(), defaultSequenceFlow);
          }

          transitionsToTake.add(defaultTransition);

        } else {
          // No sequence flow could be found, not even a default one
          throw LOG.stuckExecutionException(execution.getActivity().getId());
        }
      }

      // take the flows found
      execution.leaveActivityViaTransitions(transitionsToTake, joinedExecutions);
    } else {
      LOG.noActivityActivation(activity.getId());
    }
  }

  protected Collection<ActivityExecution> getLeafExecutions(ActivityExecution parent) {
    List<ActivityExecution> executionlist = new ArrayList<ActivityExecution>();
    List<? extends ActivityExecution> subExecutions = parent.getNonEventScopeExecutions();
    if (subExecutions.size() == 0) {
      executionlist.add(parent);
    } else {
      for (ActivityExecution concurrentExecution : subExecutions) {
        executionlist.addAll(getLeafExecutions(concurrentExecution));
      }
    }

    return executionlist;
  }

  protected boolean activatesGateway(ActivityExecution execution, PvmActivity gatewayActivity) {
    int numExecutionsGuaranteedToActivate = gatewayActivity.getIncomingTransitions().size();
    ActivityExecution scopeExecution = execution.isScope() ? execution : execution.getParent();

    List<ActivityExecution> executionsAtGateway = execution.findInactiveConcurrentExecutions(gatewayActivity);

    if (executionsAtGateway.size() >= numExecutionsGuaranteedToActivate) {
      return true;
    }
    else {
      Collection<ActivityExecution> executionsNotAtGateway = getLeafExecutions(scopeExecution);
      executionsNotAtGateway.removeAll(executionsAtGateway);

      for (ActivityExecution executionNotAtGateway : executionsNotAtGateway) {
        if (canReachActivity(executionNotAtGateway, gatewayActivity)) {
          return false;
        }
      }

      // if no more token may arrive, then activate
      return true;
    }

  }

  protected boolean canReachActivity(ActivityExecution execution, PvmActivity activity) {
    PvmTransition pvmTransition = execution.getTransition();
    if (pvmTransition != null) {
      return isReachable(pvmTransition.getDestination(), activity, new HashSet<PvmActivity>());
    } else {
      return isReachable(execution.getActivity(), activity, new HashSet<PvmActivity>());
    }
  }

  protected boolean isReachable(PvmActivity srcActivity, PvmActivity targetActivity, Set<PvmActivity> visitedActivities) {
    if (srcActivity.equals(targetActivity)) {
      return true;
    }

    // To avoid infinite looping, we must capture every node we visit and
    // check before going further in the graph if we have already visited the node.
    visitedActivities.add(srcActivity);

    List<PvmTransition> outgoingTransitions = srcActivity.getOutgoingTransitions();
    if (outgoingTransitions.isEmpty()) {
      ScopeImpl flowScope = srcActivity.getFlowScope();
      if (flowScope == null || !(flowScope instanceof PvmActivity)) {
        return false;
      }

      return isReachable((PvmActivity) flowScope, targetActivity, visitedActivities);
    }
    else {
      for (PvmTransition pvmTransition : outgoingTransitions) {
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
