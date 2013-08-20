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
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.SuspensionStateDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.runtime.ProcessInstanceResource;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessInstanceResourceImpl implements ProcessInstanceResource {

  private ProcessEngine engine;
  private String processInstanceId;
  
  public ProcessInstanceResourceImpl(ProcessEngine engine, String processInstanceId) {
    this.engine = engine;
    this.processInstanceId = processInstanceId;
  }
  
  @Override
  public ProcessInstanceDto getProcessInstance() {
    RuntimeService runtimeService = engine.getRuntimeService();
    ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    
    if (instance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Process instance with id " + processInstanceId + " does not exist");
    }
    
    ProcessInstanceDto result = ProcessInstanceDto.fromProcessInstance(instance);
    return result;
  }
  
  @Override
  public void deleteProcessInstance() {
    RuntimeService runtimeService = engine.getRuntimeService();
    try {
      runtimeService.deleteProcessInstance(processInstanceId, null);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "Process instance with id " + processInstanceId + " does not exist");
    }
    
  }

  @Override
  public VariableResource getVariablesResource() {
    return new ExecutionVariablesResource(engine, processInstanceId, true);
  }

  @Override
  public ActivityInstanceDto getActivityInstanceTree() {
    RuntimeService runtimeService = engine.getRuntimeService();
    
    ActivityInstance activityInstance = null;
    
    try {
      activityInstance = runtimeService.getActivityInstance(processInstanceId);
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.INTERNAL_SERVER_ERROR, e, e.getMessage());
    }
    
    if (activityInstance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Process instance with id " + processInstanceId + " does not exist");
    }
    
    ActivityInstanceDto result = ActivityInstanceDto.fromActivityInstance(activityInstance);
    return result;
  }

  @Override
  public void updateSuspensionState(SuspensionStateDto dto) {
	  RuntimeService runtimeService = engine.getRuntimeService();
	  
	  try {
		 if(dto.getSuspended()) {
			 runtimeService.suspendProcessInstanceById(processInstanceId);	 
		 } else {
			 runtimeService.activateProcessInstanceById(processInstanceId);	 
		 } 		 
      } catch (ProcessEngineException e){
		    throw new InvalidRequestException(Status.NOT_FOUND, e, "Process instance with id " + processInstanceId + " does not exist");
	  } 
  }
}
