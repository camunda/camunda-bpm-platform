package org.camunda.bpm.engine.rest.dto.externaltask;

import org.camunda.bpm.engine.ProcessEngine;

import java.util.Collections;
import java.util.List;

public class GetTopicNamesForExternalTasksDto {

  protected boolean withLockedTasks;
  protected boolean withUnlockedTasks;
  protected boolean withRetriesLeft;

  public static List<String> execute(ProcessEngine processEngine){
    return processEngine.getExternalTaskService().getTopicNames();
  }
}
