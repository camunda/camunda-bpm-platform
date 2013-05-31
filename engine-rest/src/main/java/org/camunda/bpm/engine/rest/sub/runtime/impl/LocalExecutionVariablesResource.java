package org.camunda.bpm.engine.rest.sub.runtime.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;

public class LocalExecutionVariablesResource extends AbstractVariablesResource {

  public LocalExecutionVariablesResource(ProcessEngine engine, String resourceId) {
    super(engine, resourceId);
  }

  @Override
  protected Map<String, Object> getVariableEntities() {
    return engine.getRuntimeService().getVariablesLocal(resourceId);
  }

  @Override
  protected void updateVariableEntities(Map<String, Object> modifications, List<String> deletions) {
    RuntimeServiceImpl runtimeService = (RuntimeServiceImpl) engine.getRuntimeService();
    runtimeService.updateVariablesLocal(resourceId, modifications, deletions);
  }
  
  @Override
  protected Object getVariableEntity(String variableKey) {
    return engine.getRuntimeService().getVariableLocal(resourceId, variableKey);
  }

  @Override
  protected void setVariableEntity(String variableKey, Object variableValue) {
    engine.getRuntimeService().setVariableLocal(resourceId, variableKey, variableValue);
  }

  @Override
  protected void removeVariableEntity(String variableKey) {
    engine.getRuntimeService().removeVariableLocal(resourceId, variableKey);
  }

  @Override
  protected String getResourceTypeName() {
    return "execution";
  }

}
