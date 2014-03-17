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
package org.camunda.bpm.engine.rest.sub.management;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionDto;
import org.camunda.bpm.engine.rest.dto.management.JobDefinitionSuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobRetriesDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;

/**
 * @author roman.smirnov
 */
public class JobDefinitionResourceImpl implements JobDefinitionResource {

  protected ProcessEngine engine;
  protected String jobDefinitionId;

  public JobDefinitionResourceImpl(ProcessEngine engine, String jobDefinitionId) {
    this.engine = engine;
    this.jobDefinitionId = jobDefinitionId;
  }

  public JobDefinitionDto getJobDefinition() {
    ManagementService managementService = engine.getManagementService();
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().jobDefinitionId(jobDefinitionId).singleResult();

    if (jobDefinition == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Job Definition with id " + jobDefinitionId + " does not exist");
    }

    return JobDefinitionDto.fromJobDefinition(jobDefinition);
  }

  public void updateSuspensionState(JobDefinitionSuspensionStateDto dto) {
    try {
      dto.setJobDefinitionId(jobDefinitionId);
      dto.updateSuspensionState(engine);

    } catch (IllegalArgumentException e) {
      String message = String.format("The suspension state of Job Definition with id %s could not be updated due to: %s", jobDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.BAD_REQUEST, e, message);
    }

  }

  public void setJobRetries(JobRetriesDto dto) {
    try {
      ManagementService managementService = engine.getManagementService();
      managementService.setJobRetriesByJobDefinitionId(jobDefinitionId, dto.getRetries());
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

}
