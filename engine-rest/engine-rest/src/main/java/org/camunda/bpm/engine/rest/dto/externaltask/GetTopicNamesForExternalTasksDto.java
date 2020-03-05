package org.camunda.bpm.engine.rest.dto.externaltask;

import org.camunda.bpm.engine.ProcessEngine;

import java.util.Collections;
import java.util.List;

public class GetTopicNamesForExternalTasksDto {

  public List<String> execute(ProcessEngine processEngine){
    processEngine.getExternalTaskService();
    return Collections.emptyList();
  }
}
