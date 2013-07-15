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

import java.text.ParseException;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.runtime.EventSubscriptionResource;
import org.camunda.bpm.engine.rest.sub.runtime.ExecutionResource;
import org.camunda.bpm.engine.rest.util.DtoUtil;
import org.camunda.bpm.engine.runtime.Execution;

public class ExecutionResourceImpl implements ExecutionResource {

  private ProcessEngine engine;
  private String executionId;
  
  public ExecutionResourceImpl(ProcessEngine engine, String executionId) {
    this.engine = engine;
    this.executionId = executionId;
  }

  @Override
  public ExecutionDto getExecution() {
    RuntimeService runtimeService = engine.getRuntimeService();
    Execution execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
    
    if (execution == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Execution with id " + executionId + " does not exist");
    }
    
    return ExecutionDto.fromExecution(execution);
  }

  @Override
  public void signalExecution(ExecutionTriggerDto triggerDto) {
    RuntimeService runtimeService = engine.getRuntimeService();
    try {
      Map<String, Object> variables = DtoUtil.toMap(triggerDto.getVariables());
      runtimeService.signal(executionId, variables);
      
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot signal execution " + executionId + ": " + e.getMessage());
      
    } catch (NumberFormatException e) {
      String errorMessage = String.format("Cannot signal execution %s due to number format exception: %s", executionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);
      
    } catch (ParseException e) {
      String errorMessage = String.format("Cannot signal execution %s due to parse exception: %s", executionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, e, errorMessage);      
    
    } catch (IllegalArgumentException e) {
      String errorMessage = String.format("Cannot signal execution %s: %s", executionId, e.getMessage());
      throw new RestException(Status.BAD_REQUEST, errorMessage);  
    }
  }

  @Override
  public VariableResource getLocalVariables() {
    return new LocalExecutionVariablesResource(engine, executionId);
  }

  @Override
  public EventSubscriptionResource getMessageEventSubscription(String messageName) {
    return new MessageEventSubscriptionResource(engine, executionId, messageName);
  }

}
