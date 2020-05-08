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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Daniel Meyer
 *
 */
public class SaveAuthorizationCmd implements Command<Authorization> {

  protected AuthorizationEntity authorization;

  public SaveAuthorizationCmd(Authorization authorization) {
    this.authorization = (AuthorizationEntity) authorization;
    validate();
  }

  protected void validate() {
    ensureOnlyOneNotNull("Authorization must either have a 'userId' or a 'groupId'.", authorization.getUserId(), authorization.getGroupId());
    ensureNotNull("Authorization 'resourceType' cannot be null.", "authorization.getResource()", authorization.getResource());
  }
  
  public Authorization execute(CommandContext commandContext) {
    
    final AuthorizationManager authorizationManager = commandContext.getAuthorizationManager();

    authorizationManager.validateResourceCompatibility(authorization);

    provideRemovalTime(commandContext);

    String operationType = null;
    AuthorizationEntity previousValues = null;
    if(authorization.getId() == null) {
      authorizationManager.insert(authorization);
      operationType = UserOperationLogEntry.OPERATION_TYPE_CREATE;
    } else {
      previousValues = commandContext.getDbEntityManager().selectById(AuthorizationEntity.class, authorization.getId());
      authorizationManager.update(authorization);
      operationType = UserOperationLogEntry.OPERATION_TYPE_UPDATE;
    }
    commandContext.getOperationLogManager().logAuthorizationOperation(operationType, authorization, previousValues);
    
    return authorization;
  }

  protected void provideRemovalTime(CommandContext commandContext) {
    for (Entry<Resources, Supplier<HistoryEvent>> resourceEntry :
        getHistoricInstanceResources(commandContext)) {
      Resources resource = resourceEntry.getKey();
      if (isResourceEqualTo(resource)) {

        Supplier<HistoryEvent> historyEventSupplier = resourceEntry.getValue();

        HistoryEvent historyEvent = historyEventSupplier.get();
        provideRemovalTime(historyEvent);

        break;
      }
    }
  }

  protected Set<Entry<Resources, Supplier<HistoryEvent>>> getHistoricInstanceResources(
      CommandContext commandContext) {
    Map<Resources, Supplier<HistoryEvent>> resources = new HashMap<>();

    resources.put(Resources.HISTORIC_PROCESS_INSTANCE, () ->
        getHistoricProcessInstance(commandContext));
    resources.put(Resources.HISTORIC_TASK, () -> getHistoricTaskInstance(commandContext));

    return resources.entrySet();
  }

  protected void provideRemovalTime(HistoryEvent historicInstance) {

    if (historicInstance != null) {
      String rootProcessInstanceId = historicInstance.getRootProcessInstanceId();
      authorization.setRootProcessInstanceId(rootProcessInstanceId);

      Date removalTime = historicInstance.getRemovalTime();
      authorization.setRemovalTime(removalTime);

    } else { // reset
      authorization.setRootProcessInstanceId(null);
      authorization.setRemovalTime(null);

    }
  }

  protected HistoryEvent getHistoricProcessInstance(CommandContext commandContext) {
    String historicProcessInstanceId = authorization.getResourceId();

    if (isNullOrAny(historicProcessInstanceId)) {
      return null;
    }

    return commandContext.getHistoricProcessInstanceManager()
        .findHistoricProcessInstance(historicProcessInstanceId);
  }

  protected HistoryEvent getHistoricTaskInstance(CommandContext commandContext) {
    String historicTaskInstanceId = authorization.getResourceId();

    if (isNullOrAny(historicTaskInstanceId)) {
      return null;
    }

    return commandContext.getHistoricTaskInstanceManager()
        .findHistoricTaskInstanceById(historicTaskInstanceId);
  }

  protected boolean isNullOrAny(String resourceId) {
    return resourceId == null || isAny(resourceId);
  }

  protected boolean isAny(String resourceId) {
    return Objects.equals(Authorization.ANY, resourceId);
  }

  protected boolean isResourceEqualTo(Resources resource) {
    return Objects.equals(resource.resourceType(), authorization.getResource());
  }

}
