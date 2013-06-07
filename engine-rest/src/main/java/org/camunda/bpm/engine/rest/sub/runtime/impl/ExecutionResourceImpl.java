package org.camunda.bpm.engine.rest.sub.runtime.impl;

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
    Map<String, Object> variables = DtoUtil.toMap(triggerDto.getVariables());
    try {
      runtimeService.signal(executionId, variables);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot signal execution " + executionId + ": " + e.getMessage());
    }
    
  }

  @Override
  public VariableResource getLocalVariables() {
    return new LocalExecutionVariablesResource(engine, executionId);
  }

  @Override
  public void triggerMessageEvent(String messageName, ExecutionTriggerDto triggerDto) {
    RuntimeService runtimeService = engine.getRuntimeService();
    
    Map<String, Object> variables = DtoUtil.toMap(triggerDto.getVariables());
    
    try {
      runtimeService.messageEventReceived(messageName, executionId, variables);
    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, "Cannot trigger message " + messageName +
          " for execution " + executionId + ": " + e.getMessage());
    }
  }




}
