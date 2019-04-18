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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * Command to delete process definitions by ids.
 *
 * @author Tassilo Weidner
 */
public class DeleteProcessDefinitionsByIdsCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final Set<String> processDefinitionIds;
  protected boolean cascadeToHistory;
  protected boolean cascadeToInstances;
  protected boolean skipCustomListeners;
  protected boolean writeUserOperationLog;
  protected boolean skipIoMappings;

  public DeleteProcessDefinitionsByIdsCmd(List<String> processDefinitionIds, boolean cascade, boolean skipCustomListeners, boolean skipIoMappings) {
    this(processDefinitionIds, cascade, cascade, skipCustomListeners, skipIoMappings, true);
  }

  public DeleteProcessDefinitionsByIdsCmd(List<String> processDefinitionIds, boolean cascadeToHistory, boolean cascadeToInstances, boolean skipCustomListeners, boolean writeUserOperationLog) {
    this(processDefinitionIds, cascadeToHistory, cascadeToInstances, skipCustomListeners, false, writeUserOperationLog);
  }

  public DeleteProcessDefinitionsByIdsCmd(List<String> processDefinitionIds, boolean cascadeToHistory, boolean cascadeToInstances, boolean skipCustomListeners, boolean skipIoMappings, boolean writeUserOperationLog) {
    this.processDefinitionIds = new HashSet<String>(processDefinitionIds);
    this.cascadeToHistory = cascadeToHistory;
    this.cascadeToInstances = cascadeToInstances;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
    this.writeUserOperationLog = writeUserOperationLog;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("processDefinitionIds", processDefinitionIds);

    List<ProcessDefinition> processDefinitions;
    if (processDefinitionIds.size() == 1) {
      ProcessDefinition processDefinition = getSingleProcessDefinition(commandContext);
      processDefinitions = new ArrayList<ProcessDefinition>();
      processDefinitions.add(processDefinition);
    } else {
      ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
      processDefinitions = processDefinitionManager.findDefinitionsByIds(processDefinitionIds);
      ensureNotEmpty(NotFoundException.class, "No process definition found", "processDefinitions", processDefinitions);
    }

    Set<ProcessDefinitionGroup> groups = groupByKeyAndTenant(processDefinitions);

    for (ProcessDefinitionGroup group : groups) {
      checkAuthorization(group);
    }

    for (ProcessDefinitionGroup group : groups) {
      deleteProcessDefinitions(group);
    }

    return null;
  }

  protected ProcessDefinition getSingleProcessDefinition(CommandContext commandContext) {
    String processDefinitionId = processDefinitionIds.iterator().next();
    ensureNotNull("processDefinitionId", processDefinitionId);
    ProcessDefinition processDefinition = commandContext.getProcessDefinitionManager().findLatestProcessDefinitionById(processDefinitionId);
    ensureNotNull(NotFoundException.class, "No process definition found with id '" + processDefinitionId + "'", "processDefinition", processDefinition);

    return processDefinition;
  }

  protected Set<ProcessDefinitionGroup> groupByKeyAndTenant(List<ProcessDefinition> processDefinitions) {
    Set<ProcessDefinitionGroup> groups = new HashSet<ProcessDefinitionGroup>();
    Map<ProcessDefinitionGroup, List<ProcessDefinitionEntity>> map = new HashMap<ProcessDefinitionGroup, List<ProcessDefinitionEntity>>();

    for (ProcessDefinition current : processDefinitions) {
      ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) current;

      ProcessDefinitionGroup group = new ProcessDefinitionGroup();
      group.key = processDefinition.getKey();
      group.tenant = processDefinition.getTenantId();

      List<ProcessDefinitionEntity> definitions = group.processDefinitions;
      if (map.containsKey(group)) {
        definitions = map.get(group);
      }
      else {
        groups.add(group);
        map.put(group, definitions);
      }

      definitions.add(processDefinition);
    }

    return groups;
  }

  protected ProcessDefinitionEntity findNewLatestProcessDefinition(ProcessDefinitionGroup group) {
    ProcessDefinitionEntity newLatestProcessDefinition = null;

    List<ProcessDefinitionEntity> processDefinitions = group.processDefinitions;
    ProcessDefinitionEntity firstProcessDefinition = processDefinitions.get(0);

    if (isLatestProcessDefinition(firstProcessDefinition)) {
      for (ProcessDefinitionEntity processDefinition : processDefinitions) {
        String previousProcessDefinitionId = processDefinition.getPreviousProcessDefinitionId();
        if (previousProcessDefinitionId != null && !this.processDefinitionIds.contains(previousProcessDefinitionId)) {
          CommandContext commandContext = Context.getCommandContext();
          ProcessDefinitionManager processDefinitionManager = commandContext.getProcessDefinitionManager();
          newLatestProcessDefinition = processDefinitionManager.findLatestDefinitionById(previousProcessDefinitionId);
          break;
        }
      }
    }

    return newLatestProcessDefinition;
  }

  protected boolean isLatestProcessDefinition(ProcessDefinitionEntity processDefinition) {
    ProcessDefinitionManager processDefinitionManager = Context.getCommandContext().getProcessDefinitionManager();
    String key = processDefinition.getKey();
    String tenantId = processDefinition.getTenantId();
    ProcessDefinitionEntity latestProcessDefinition = processDefinitionManager.findLatestDefinitionByKeyAndTenantId(key, tenantId);
    return processDefinition.getId().equals(latestProcessDefinition.getId());
  }

  protected void checkAuthorization(ProcessDefinitionGroup group) {
    List<CommandChecker> commandCheckers = Context.getCommandContext().getProcessEngineConfiguration().getCommandCheckers();
    List<ProcessDefinitionEntity> processDefinitions = group.processDefinitions;
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      for (CommandChecker commandChecker : commandCheckers) {
        commandChecker.checkDeleteProcessDefinitionById(processDefinition.getId());
      }
    }
  }

  protected void deleteProcessDefinitions(ProcessDefinitionGroup group) {
    ProcessDefinitionEntity newLatestProcessDefinition = findNewLatestProcessDefinition(group);

    CommandContext commandContext = Context.getCommandContext();
    UserOperationLogManager userOperationLogManager = commandContext.getOperationLogManager();
    ProcessDefinitionManager definitionManager = commandContext.getProcessDefinitionManager();

    List<ProcessDefinitionEntity> processDefinitions = group.processDefinitions;
    for (ProcessDefinitionEntity processDefinition : processDefinitions) {
      String processDefinitionId = processDefinition.getId();

      if (writeUserOperationLog) {
        userOperationLogManager.logProcessDefinitionOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE, processDefinitionId, processDefinition.getKey(),
            new PropertyChange("cascade", false, cascadeToHistory));
      }

      definitionManager.deleteProcessDefinition(processDefinition, processDefinitionId, cascadeToHistory, cascadeToInstances, skipCustomListeners, skipIoMappings);
    }

    if (newLatestProcessDefinition != null) {
      ProcessEngineConfigurationImpl configuration = Context.getProcessEngineConfiguration();
      DeploymentCache deploymentCache = configuration.getDeploymentCache();
      newLatestProcessDefinition = deploymentCache.resolveProcessDefinition(newLatestProcessDefinition);

      List<Deployer> deployers = configuration.getDeployers();
      for (Deployer deployer : deployers) {
        if (deployer instanceof BpmnDeployer) {
          ((BpmnDeployer) deployer).addEventSubscriptions(newLatestProcessDefinition);
        }
      }
    }
  }

  private static class ProcessDefinitionGroup {

    String key;
    String tenant;
    List<ProcessDefinitionEntity> processDefinitions = new ArrayList<ProcessDefinitionEntity>();

    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + ((tenant == null) ? 0 : tenant.hashCode());
      return result;
    }

    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ProcessDefinitionGroup other = (ProcessDefinitionGroup) obj;
      if (key == null) {
        if (other.key != null)
          return false;
      } else if (!key.equals(other.key))
        return false;
      if (tenant == null) {
        if (other.tenant != null)
          return false;
      } else if (!tenant.equals(other.tenant))
        return false;
      return true;
    }

  }

}
