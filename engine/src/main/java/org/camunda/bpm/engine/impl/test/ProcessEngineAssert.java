package org.camunda.bpm.engine.impl.test;

import junit.framework.AssertionFailedError;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class ProcessEngineAssert {
  
  public static void assertProcessEnded(ProcessEngine processEngine, String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();
    
    if (processInstance!=null) {
      throw new AssertionFailedError("expected finished process instance '"+processInstanceId+"' but it was still in the db"); 
    }
  }
}
