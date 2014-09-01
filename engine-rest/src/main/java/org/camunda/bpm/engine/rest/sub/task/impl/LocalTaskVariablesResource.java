package org.camunda.bpm.engine.rest.sub.task.impl;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;

public class LocalTaskVariablesResource extends AbstractVariablesResource {

  public LocalTaskVariablesResource(ProcessEngine engine, String resourceId) {
    super(engine, resourceId);
  }

  @Override
  protected Map<String, Object> getVariableEntities() {
    return engine.getTaskService().getVariablesLocal(resourceId);
  }

  @Override
  protected void updateVariableEntities(Map<String, Object> modifications, List<String> deletions) {
    TaskServiceImpl taskService = (TaskServiceImpl) engine.getTaskService();
    taskService.updateVariablesLocal(resourceId, modifications, deletions);
  }

  @Override
  protected Object getVariableEntity(String variableKey) {
    return engine.getTaskService().getVariableLocal(resourceId, variableKey);
  }

  @Override
  protected void setVariableEntity(String variableKey, Object variableValue) {
    engine.getTaskService().setVariableLocal(resourceId, variableKey, variableValue);
  }

  @Override
  protected void removeVariableEntity(String variableKey) {
    engine.getTaskService().removeVariableLocal(resourceId, variableKey);
  }

  @Override
  protected String getResourceTypeName() {
    return "task";
  }

  @Override
  protected void setVariableEntityFromSerialized(String variableKey, Object serializedValue, String variableType, Map<String, Object> configuration) {
    engine.getTaskService().setVariableLocalFromSerialized(resourceId, variableKey,
        serializedValue, variableType, configuration);

  }

}
