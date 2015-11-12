package org.camunda.bpm.engine.rest.sub.task.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesResource;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.List;

public class LocalTaskVariablesResource extends AbstractVariablesResource {

  public LocalTaskVariablesResource(ProcessEngine engine, String resourceId, ObjectMapper objectMapper) {
    super(engine, resourceId, objectMapper);
  }

  protected String getResourceTypeName() {
    return "task";
  }

  protected void removeVariableEntity(String variableKey) {
    engine.getTaskService().removeVariableLocal(resourceId, variableKey);
  }

  protected VariableMap getVariableEntities(boolean deserializeValues) {
    return engine.getTaskService().getVariablesLocalTyped(resourceId, deserializeValues);
  }

  protected void updateVariableEntities(VariableMap modifications, List<String> deletions) {
    TaskServiceImpl taskService = (TaskServiceImpl) engine.getTaskService();
    taskService.updateVariablesLocal(resourceId, modifications, deletions);
  }

  protected TypedValue getVariableEntity(String variableKey, boolean deserializeValue) {
    return engine.getTaskService().getVariableLocalTyped(resourceId, variableKey, deserializeValue);
  }

  protected void setVariableEntity(String variableKey, TypedValue variableValue) {
    engine.getTaskService().setVariableLocal(resourceId, variableKey, variableValue);
  }

}
