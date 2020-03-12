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
package org.camunda.bpm.engine.rest.dto.runtime;

import java.util.List;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricProcessInstanceQueryDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstancesSuspensionStateBuilder;

/**
 * @author roman.smirnov
 */
public class ProcessInstanceSuspensionStateDto extends SuspensionStateDto {

  protected String processDefinitionId;
  protected String processDefinitionKey;

  protected List<String> processInstanceIds;
  protected ProcessInstanceQueryDto processInstanceQuery;
  protected HistoricProcessInstanceQueryDto historicProcessInstanceQuery;

  protected String processDefinitionTenantId;
  protected boolean processDefinitionWithoutTenantId;

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public ProcessInstanceQueryDto getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public void setProcessInstanceQuery(ProcessInstanceQueryDto processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
  }

  public void setHistoricProcessInstanceQuery(HistoricProcessInstanceQueryDto historicProcessInstanceQuery) {
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
  }

  public HistoricProcessInstanceQueryDto getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public void setProcessDefinitionTenantId(String processDefinitionTenantId) {
    this.processDefinitionTenantId = processDefinitionTenantId;
  }

  public boolean getProcessDefinitionWithoutTenantId() {
    return processDefinitionWithoutTenantId;
  }

  public void setProcessDefinitionWithoutTenantId(boolean processDefinitionWithoutTenantId) {
    this.processDefinitionWithoutTenantId = processDefinitionWithoutTenantId;
  }

  @Override
  public void updateSuspensionState(ProcessEngine engine) {
    int params = parameterCount(processDefinitionId, processDefinitionKey);
    int syncParams = parameterCount(processInstanceIds, processInstanceQuery, historicProcessInstanceQuery);

    if (params >= 1 && syncParams >= 1) {
      String message = "Choose either a single processInstance with processDefinitionId or processDefinitionKey or a group of processInstances with processInstanceIds, procesInstanceQuery or historicProcessInstanceQuery.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    } else if (params > 1) {
      String message = "Only one of processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    } else if (params == 0 && syncParams == 0) {
      String message = "Either processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    UpdateProcessInstanceSuspensionStateBuilder updateSuspensionStateBuilder = null;
    if (params == 1) {
      updateSuspensionStateBuilder = createUpdateSuspensionStateBuilder(engine);
    } else if (syncParams >= 1) {
      updateSuspensionStateBuilder = createUpdateSuspensionStateGroupBuilder(engine);
    }

    if (getSuspended()) {
      updateSuspensionStateBuilder.suspend();
    } else {
      updateSuspensionStateBuilder.activate();
    }
  }

  protected UpdateProcessInstanceSuspensionStateBuilder createUpdateSuspensionStateBuilder(ProcessEngine engine) {
    UpdateProcessInstanceSuspensionStateSelectBuilder selectBuilder = engine.getRuntimeService().updateProcessInstanceSuspensionState();

    if (processDefinitionId != null) {
      return selectBuilder.byProcessDefinitionId(processDefinitionId);
    } else { // processDefinitionKey != null
      UpdateProcessInstanceSuspensionStateTenantBuilder tenantBuilder = selectBuilder.byProcessDefinitionKey(processDefinitionKey);
      if (processDefinitionTenantId != null) {
        tenantBuilder.processDefinitionTenantId(processDefinitionTenantId);
      } else if (processDefinitionWithoutTenantId) {
        tenantBuilder.processDefinitionWithoutTenantId();
      }
      return tenantBuilder;
    }
  }

  protected UpdateProcessInstancesSuspensionStateBuilder createUpdateSuspensionStateGroupBuilder(ProcessEngine engine) {
    UpdateProcessInstanceSuspensionStateSelectBuilder selectBuilder = engine.getRuntimeService().updateProcessInstanceSuspensionState();
    UpdateProcessInstancesSuspensionStateBuilder groupBuilder = null;
    if (processInstanceIds != null) {
      groupBuilder = selectBuilder.byProcessInstanceIds(processInstanceIds);
    }

    if (processInstanceQuery != null) {
      if (groupBuilder == null) {
        groupBuilder = selectBuilder.byProcessInstanceQuery(processInstanceQuery.toQuery(engine));
      } else {
        groupBuilder.byProcessInstanceQuery(processInstanceQuery.toQuery(engine));
      }
    }

    if (historicProcessInstanceQuery != null) {
      if (groupBuilder == null) {
        groupBuilder = selectBuilder.byHistoricProcessInstanceQuery(historicProcessInstanceQuery.toQuery(engine));
      } else {
        groupBuilder.byHistoricProcessInstanceQuery(historicProcessInstanceQuery.toQuery(engine));
      }
    }

    return groupBuilder;
  }

  protected int parameterCount(Object... o) {
    int count = 0;
    for (Object o1 : o) {
      count += (o1 != null ? 1 : 0);
    }
    return count;
  }

}
