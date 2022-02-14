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
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.ProcessEngineLogger.CMD_LOGGER;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.repository.CalledProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.CalledProcessDefinition;

public class GetStaticCalledProcessDefinitionCmd implements Command<Collection<CalledProcessDefinition>> {

  protected String processDefinitionId;

  public GetStaticCalledProcessDefinitionCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  protected List<ActivityImpl> findCallActivitiesInProcess(ProcessDefinitionEntity processDefinition) {
    List<ActivityImpl> callActivities = new ArrayList<>();

    Queue<ActivityImpl> toCheck = new LinkedList<>(processDefinition.getActivities());
    while (!toCheck.isEmpty()) {
      ActivityImpl candidate = toCheck.poll();

      if (!candidate.getActivities().isEmpty()) {
        toCheck.addAll(candidate.getActivities());
      }
      if (candidate.getActivityBehavior() instanceof CallActivityBehavior) {
        callActivities.add(candidate);
      }
    }
    return callActivities;
  }

  @Override
  public Collection<CalledProcessDefinition> execute(CommandContext commandContext) {
    ProcessDefinitionEntity processDefinition = new GetDeployedProcessDefinitionCmd(processDefinitionId, true).execute(commandContext);

    List<ActivityImpl> callActivities = findCallActivitiesInProcess(processDefinition);

    Map<String, CalledProcessDefinitionImpl> calledProcessDefinitionsById = new HashMap<>();

    for (ActivityImpl activity : callActivities) {
      CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();
      CallableElement callableElement = behavior.getCallableElement();
      String activityId = activity.getActivityId();

      String tenantId = processDefinition.getTenantId();
      ProcessDefinition calledProcess = CallableElementUtil.getStaticallyBoundProcessDefinition(
          processDefinitionId,
          activityId,
          callableElement,
          tenantId);

      if (calledProcess != null) {
        if (!calledProcessDefinitionsById.containsKey(calledProcess.getId())) {
          try {
            for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
              checker.checkReadProcessDefinition(calledProcess);
            }
            CalledProcessDefinitionImpl result = new CalledProcessDefinitionImpl(calledProcess, processDefinitionId);
            result.addCallingCallActivity(activityId);

            calledProcessDefinitionsById.put(calledProcess.getId(), result);
          } catch (AuthorizationException e) {
            // unauthorized Process definitions will not be added.
            CMD_LOGGER.debugNotAllowedToResolveCalledProcess(calledProcess.getId(), processDefinitionId, activityId, e);
          }

        } else {
          calledProcessDefinitionsById.get(calledProcess.getId()).addCallingCallActivity(activityId);
        }
      }
    }
    return new ArrayList<>(calledProcessDefinitionsById.values());
  }
}
