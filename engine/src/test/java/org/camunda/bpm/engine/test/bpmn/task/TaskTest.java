package org.camunda.bpm.engine.test.bpmn.task;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class TaskTest extends PluggableProcessEngineTestCase {

    @Deployment
    public void testInputOutput() {
      ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");
      assertEquals("test_val", runtimeService.getVariable(processInstance.getId(), "test_var"));
    }

}
