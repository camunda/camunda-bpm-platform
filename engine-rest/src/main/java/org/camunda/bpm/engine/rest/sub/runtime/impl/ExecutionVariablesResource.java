package org.camunda.bpm.engine.rest.sub.runtime.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.List;

public class ExecutionVariablesResource extends AbstractVariablesResource {

  private String resourceTypeName;

  public ExecutionVariablesResource(ProcessEngine engine, String resourceId, boolean isProcessInstance, ObjectMapper objectMapper) {
    super(engine, resourceId, objectMapper);
    if (isProcessInstance) {
      resourceTypeName = "process instance";
    } else {
      resourceTypeName = "execution";
    }
  }

  protected String getResourceTypeName() {
    return resourceTypeName;
  }

  protected void updateVariableEntities(VariableMap modifications, List<String> deletions) {
    RuntimeServiceImpl runtimeService = (RuntimeServiceImpl) engine.getRuntimeService();
    runtimeService.updateVariables(resourceId, modifications, deletions);
  }

  protected void removeVariableEntity(String variableKey) {
    engine.getRuntimeService().removeVariable(resourceId, variableKey);
  }

  protected VariableMap getVariableEntities(boolean deserializeValues) {
    return engine.getRuntimeService().getVariablesTyped(resourceId, deserializeValues);
  }

  protected TypedValue getVariableEntity(String variableKey, boolean deserializeValue) {
    return engine.getRuntimeService().getVariableTyped(resourceId, variableKey, deserializeValue);
  }

  protected void setVariableEntity(String variableKey, TypedValue variableValue) {
    engine.getRuntimeService().setVariable(resourceId, variableKey, variableValue);
  }

}
