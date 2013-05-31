package org.camunda.bpm.engine.rest.sub.runtime.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;

public class ExecutionVariablesResource extends AbstractVariablesResource {

  private String resourceTypeName;
  
  public ExecutionVariablesResource(ProcessEngine engine, String resourceId, boolean isProcessInstance) {
    super(engine, resourceId);
    if (isProcessInstance) {
      resourceTypeName = "process instance";
    } else {
      resourceTypeName = "execution";
    }
  }

  @Override
  protected Map<String, Object> getVariableEntities() {
    return engine.getRuntimeService().getVariables(resourceId);
  }

  @Override
  protected void updateVariableEntities(Map<String, Object> modifications, List<String> deletions) {
    RuntimeServiceImpl runtimeService = (RuntimeServiceImpl) engine.getRuntimeService();
    runtimeService.updateVariables(resourceId, modifications, deletions);
  }
  
  @Override
  protected Object getVariableEntity(String variableKey) {
    return engine.getRuntimeService().getVariable(resourceId, variableKey);
  }

  @Override
  protected void setVariableEntity(String variableKey, Object variableValue) {
    engine.getRuntimeService().setVariable(resourceId, variableKey, variableValue);
  }

  @Override
  protected void removeVariableEntity(String variableKey) {
    engine.getRuntimeService().removeVariable(resourceId, variableKey);
  }

  @Override
  protected String getResourceTypeName() {
    return resourceTypeName;
  }

}
