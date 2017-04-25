package org.camunda.bpm.engine.spring.test.transaction.modification;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserBean {

  @Autowired
  public ProcessEngine processEngine;

  @Autowired
  RuntimeService runtimeService;

  @Autowired
  RepositoryService repositoryService;

  @Transactional
  public void completeUserTaskAndModifyInstanceInOneTransaction(ProcessInstance procInst) {
    // this method assures that the execution the process instance
    // modification is done in one transaction.

    // reset the process instance before the timer
    runtimeService.createProcessInstanceModification(procInst.getId())
      .cancelAllForActivity("TimerEvent")
      .startBeforeActivity("TimerEvent")
      .execute();
  }

}

