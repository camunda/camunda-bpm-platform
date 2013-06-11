package org.camunda.bpm.cockpit.plugin.base.dto;

import java.util.ArrayList;
import java.util.List;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;

/**
 *
 * @author nico.rehwaldt
 */
public class ActivityInstanceDto {

  private String activityId;
  private String parentId;
  private String processInstanceId;

  private boolean scope;
  private boolean eventScope;
  private boolean active;
  private boolean concurrent;

  public ActivityInstanceDto() {

  }

  public ActivityInstanceDto(ExecutionEntity execution) {

    parentId = execution.getParentId();
    activityId = execution.getCurrentActivityId();

    processInstanceId = execution.getProcessInstanceId();

    concurrent = execution.isConcurrent();
    scope = execution.isScope();
    eventScope = execution.isEventScope();
    active = execution.isActive();
  }

  public String getActivityId() {
    return activityId;
  }

  public String getParentId() {
    return parentId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  /**
   *
   * @param executions
   * @return
   */
  public static List<ActivityInstanceDto> wrapAll(List<Execution> executions) {

    ArrayList<ActivityInstanceDto> results = new ArrayList<ActivityInstanceDto>();

    for (Execution e: executions) {
      results.add(new ActivityInstanceDto((ExecutionEntity) e));
    }

    return results;
  }

  @Override
  public String toString() {
    return String.format("%40s | %40s | %20s | %10s | %10s | %10s | %10s", processInstanceId, parentId, activityId, scope, eventScope, active, concurrent);
  }
}
