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
package org.camunda.bpm.engine.rest.dto.management;

import java.util.Date;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * @author roman.smirnov
 */
public class JobDefinitionSuspensionStateDto extends SuspensionStateDto {

  private String executionDate;
  private boolean includeJobs;
  private String jobDefinitionId;
  private String processDefinitionId;
  private String processDefinitionKey;

  private String processDefinitionTenantId;
  private boolean processDefinitionWithoutTenantId;

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public void setJobDefinitionId(String jobDefinitionId) {
    this.jobDefinitionId = jobDefinitionId;
  }

  public void setExecutionDate(String executionDate) {
    this.executionDate = executionDate;
  }

  public void setIncludeJobs(boolean includeJobs) {
    this.includeJobs = includeJobs;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public void setProcessDefinitionTenantId(String processDefinitionTenantId) {
    this.processDefinitionTenantId = processDefinitionTenantId;
  }

  public void setProcessDefinitionWithoutTenantId(boolean processDefinitionWithoutTenantId) {
    this.processDefinitionWithoutTenantId = processDefinitionWithoutTenantId;
  }

  @Override
  public void updateSuspensionState(ProcessEngine engine) {
    int params = (jobDefinitionId != null ? 1 : 0)
               + (processDefinitionId != null ? 1 : 0)
               + (processDefinitionKey != null ? 1 : 0);

    if (params > 1) {
      String message = "Only one of jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);

    } else if (params == 0) {
      String message = "Either jobDefinitionId, processDefinitionId or processDefinitionKey should be set to update the suspension state.";
      throw new InvalidRequestException(Status.BAD_REQUEST, message);
    }

    UpdateJobDefinitionSuspensionStateBuilder updateSuspensionStateBuilder = createUpdateSuspensionStateBuilder(engine);

    if (executionDate != null && !executionDate.equals("")) {
      Date delayedExecutionDate = DateTimeUtil.parseDate(executionDate);

      updateSuspensionStateBuilder.executionDate(delayedExecutionDate);
    }

    updateSuspensionStateBuilder.includeJobs(includeJobs);

    if (getSuspended()) {
      updateSuspensionStateBuilder.suspend();
    } else {
      updateSuspensionStateBuilder.activate();
    }
  }

  protected UpdateJobDefinitionSuspensionStateBuilder createUpdateSuspensionStateBuilder(ProcessEngine engine) {
    UpdateJobDefinitionSuspensionStateSelectBuilder selectBuilder = engine.getManagementService().updateJobDefinitionSuspensionState();

    if (jobDefinitionId != null) {
      return selectBuilder.byJobDefinitionId(jobDefinitionId);

    } else if (processDefinitionId != null) {
      return selectBuilder.byProcessDefinitionId(processDefinitionId);

    } else {
      UpdateJobDefinitionSuspensionStateTenantBuilder tenantBuilder = selectBuilder.byProcessDefinitionKey(processDefinitionKey);

      if (processDefinitionTenantId != null) {
        tenantBuilder.processDefinitionTenantId(processDefinitionTenantId);

      } else if (processDefinitionWithoutTenantId) {
        tenantBuilder.processDefinitionWithoutTenantId();
      }

      return tenantBuilder;
    }
  }

}
