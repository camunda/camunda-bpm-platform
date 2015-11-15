package org.camunda.bpm.engine.impl.test;


import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.xml.ModelInstance;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TestHelperDeployModelInstanceTest {

  @Rule
  public final ProcessEngineRule processEngineRule = new ProcessEngineRule();

  public static class MyModel implements TestHelper.ModelInstanceSupplier {

    @Override
    public BpmnModelInstance get() {
      return Bpmn.createExecutableProcess("dummyProcess").startEvent().userTask("foo").endEvent().done();
    }
  }


  @Test
  @Deployment(modelInstances = MyModel.class)
  public void testDeployAndRemoveModelInstance() {
    ProcessDefinition processDefinition = processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("dummyProcess").singleResult();

    assertNotNull("process not deployed", processDefinition);

    processEngineRule.getRuntimeService().startProcessInstanceByKey("dummyProcess");
  }


  /**
   * deploy twice to see if cleanup is succesful.
   */
  @Test
  @Deployment(modelInstances = MyModel.class)
  public void testDeployAndRemoveModelInstanceAgain() {
    ProcessDefinition processDefinition = processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("dummyProcess").singleResult();

    assertNotNull("process not deployed", processDefinition);
  }
}