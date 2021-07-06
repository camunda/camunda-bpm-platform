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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.repository.StaticCalledProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.StaticCalledProcessDefinition;

public class GetStaticCalledProcessDefinitionCmd implements Command<Collection<StaticCalledProcessDefinition>> {

  protected String processDefinitionId;

  public GetStaticCalledProcessDefinitionCmd(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @Override
  public Collection<StaticCalledProcessDefinition> execute(CommandContext commandContext) {
    ProcessDefinition definition = commandContext.getProcessEngineConfiguration().getRepositoryService()
      .getProcessDefinition(processDefinitionId);

    List<ActivityImpl> activities = commandContext.getProcessEngineConfiguration().getDeploymentCache()
      .findDeployedProcessDefinitionById(processDefinitionId).getActivities();

    List<ActivityImpl> callActivities = activities.stream()
      .filter(act -> act.getActivityBehavior() instanceof CallActivityBehavior).collect(Collectors.toList());

    Map<String, StaticCalledProcessDefinitionImpl> map = new HashMap<>();

    for (ActivityImpl activity : callActivities) {
      CallActivityBehavior behavior = (CallActivityBehavior) activity.getActivityBehavior();
      CallableElement callableElement = behavior.getCallableElement();

      String tenantId = definition.getTenantId();
      ProcessDefinitionEntity calledProcessDefinition = null;
      calledProcessDefinition = CallableElementUtil.getStaticallyBoundProcessDefinition(callableElement, tenantId);

      if (calledProcessDefinition != null) {
        if (!map.containsKey(calledProcessDefinition.getId())) {
          try {
            for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
              checker.checkReadProcessDefinition(calledProcessDefinition);
            }
            StaticCalledProcessDefinitionImpl result =
              new StaticCalledProcessDefinitionImpl(calledProcessDefinition, processDefinitionId);
            result.addCallingCallActivity(activity.getActivityId());

            map.put(calledProcessDefinition.getId(), result);
          } catch (AuthorizationException e) {
            // unauthorized Process definitions will not be added.
          }

        } else {
          map.get(calledProcessDefinition.getId()).addCallingCallActivity(activity.getActivityId());
        }
      }
    }
    return new ArrayList<>(map.values());
  }
}
