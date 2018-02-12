/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.bpmn.deployment;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;


/**
 * @author Joram Barrez
 * @author Thorben Lindhauer
 */
public class BpmnDeploymentTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testGetBpmnXmlFileThroughService() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    assertEquals(bpmnResourceName, deploymentResources.get(0));

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertEquals(bpmnResourceName, processDefinition.getResourceName());
    assertNull(processDefinition.getDiagramResourceName());
    assertFalse(processDefinition.hasStartFormKey());

    ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinition.getId());
    assertNull(readOnlyProcessDefinition.getDiagramResourceName());

    // verify content
    InputStream deploymentInputStream = repositoryService.getResourceAsStream(deploymentId, bpmnResourceName);
    String contentFromDeployment = readInputStreamToString(deploymentInputStream);
    assertTrue(contentFromDeployment.length() > 0);
    assertTrue(contentFromDeployment.contains("process id=\"emptyProcess\""));

    InputStream fileInputStream = ReflectUtil.getResourceAsStream("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml");
    String contentFromFile = readInputStreamToString(fileInputStream);
    assertEquals(contentFromFile, contentFromDeployment);
  }

  private String readInputStreamToString(InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, "input stream");
    return new String(bytes);
  }

  public void FAILING_testViolateProcessDefinitionIdMaximumLength() {
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/processWithLongId.bpmn20.xml")
        .deploy();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("id can be maximum 64 characters", e.getMessage());
    }

    // Verify that nothing is deployed
    assertEquals(0, repositoryService.createDeploymentQuery().count());
  }

  public void testDeploySameFileTwice() {
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    assertEquals(bpmnResourceName, deploymentResources.get(0));

    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertEquals(1, deploymentList.size());

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testPartialChangesDeployAll() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment()
      .enableDuplicateFiltering()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .name("twice")
      .deploy();

    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deployment1.getId());
    assertEquals(2, deploymentResources.size());

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().done();

    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment()
      .enableDuplicateFiltering()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .name("twice")
      .deploy();
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deploymentList.size());

    // there should be new versions of both processes
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count());

    repositoryService.deleteDeployment(deployment1.getId());
    repositoryService.deleteDeployment(deployment2.getId());
  }

  public void testPartialChangesDeployChangedOnly() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .name("thrice")
      .deploy();

    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deployment1.getId());
    assertEquals(2, deploymentResources.size());

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().done();

    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .name("thrice")
      .deploy();

    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deploymentList.size());

    // there should be only one version of process 1
    ProcessDefinition process1Definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").singleResult();
    assertNotNull(process1Definition);
    assertEquals(1, process1Definition.getVersion());
    assertEquals(deployment1.getId(), process1Definition.getDeploymentId());

    // there should be two versions of process 2
    assertEquals(2, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count());

    BpmnModelInstance anotherChangedModel2 = Bpmn.createExecutableProcess("process2").startEvent().endEvent().done();

    // testing with a third deployment to ensure the change check is not only performed against
    // the last version of the deployment
    org.camunda.bpm.engine.repository.Deployment deployment3 = repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", anotherChangedModel2)
        .name("thrice")
        .deploy();

    // there should still be one version of process 1
    assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count());

    // there should be three versions of process 2
    assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count());

    repositoryService.deleteDeployment(deployment1.getId());
    repositoryService.deleteDeployment(deployment2.getId());
    repositoryService.deleteDeployment(deployment3.getId());
  }


  public void testPartialChangesRedeployOldVersion() {
    // deployment 1 deploys process version 1
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment()
      .addModelInstance("process1.bpmn20.xml", model1)
      .name("deployment")
      .deploy();

    // deployment 2 deploys process version 2
    BpmnModelInstance changedModel1 = Bpmn.createExecutableProcess("process1").startEvent().done();
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", changedModel1)
      .name("deployment")
      .deploy();

    // deployment 3 deploys process version 1 again
    org.camunda.bpm.engine.repository.Deployment deployment3 = repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", model1)
      .name("deployment")
      .deploy();

    // should result in three process definitions
    assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count());

    repositoryService.deleteDeployment(deployment1.getId());
    repositoryService.deleteDeployment(deployment2.getId());
    repositoryService.deleteDeployment(deployment3.getId());
  }

  public void testDeployTwoProcessesWithDuplicateIdAtTheSameTime() {
    try {
      String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
      String bpmnResourceName2 = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService2.bpmn20.xml";
      repositoryService.createDeployment().enableDuplicateFiltering()
              .addClasspathResource(bpmnResourceName)
              .addClasspathResource(bpmnResourceName2)
              .name("duplicateAtTheSameTime").deploy();
      fail();
    } catch (Exception e) {
      // Verify that nothing is deployed
      assertEquals(0, repositoryService.createDeploymentQuery().count());
    }
  }

  public void testDeployDifferentFiles() {
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering(false).addClasspathResource(bpmnResourceName).name("twice").deploy();

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    assertEquals(bpmnResourceName, deploymentResources.get(0));

    bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deploymentList.size());

    deleteDeployments(deploymentList);
  }

  public void testDiagramCreationDisabled() {
    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseDiagramInterchangeElements.bpmn20.xml").deploy();

    // Graphical information is not yet exposed publicly, so we need to do some plumbing
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    ProcessDefinitionEntity processDefinitionEntity = commandExecutor.execute(new Command<ProcessDefinitionEntity>() {
      @Override
      public ProcessDefinitionEntity execute(CommandContext commandContext) {
        return Context.getProcessEngineConfiguration()
                      .getDeploymentCache()
                      .findDeployedLatestProcessDefinitionByKey("myProcess");
      }
    });

    assertNotNull(processDefinitionEntity);
    assertEquals(7, processDefinitionEntity.getActivities().size());

    // Check that no diagram has been created
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(processDefinitionEntity.getDeploymentId());
    assertEquals(1, resourceNames.size());

    repositoryService.deleteDeployment(repositoryService.createDeploymentQuery().singleResult().getId(), true);
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg"
  })
  public void testProcessDiagramResource() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertEquals("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml", processDefinition.getResourceName());
    assertTrue(processDefinition.hasStartFormKey());

    String diagramResourceName = processDefinition.getDiagramResourceName();
    assertEquals("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg", diagramResourceName);

    InputStream diagramStream = repositoryService.getResourceAsStream(deploymentId, "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");
    byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertEquals(33343, diagramBytes.length);
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg"
        })
  public void testMultipleDiagramResourcesProvided() {
    ProcessDefinition processA = repositoryService.createProcessDefinitionQuery().processDefinitionKey("a").singleResult();
    ProcessDefinition processB = repositoryService.createProcessDefinitionQuery().processDefinitionKey("b").singleResult();
    ProcessDefinition processC = repositoryService.createProcessDefinitionQuery().processDefinitionKey("c").singleResult();

    assertEquals("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg", processA.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg", processB.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg", processC.getDiagramResourceName());
  }

  @Deployment
  public void testProcessDefinitionDescription() {
    String id = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(id);
    assertEquals("This is really good process documentation!", processDefinition.getDescription());
  }

  public void testDeployInvalidExpression() {
    // ACT-1391: Deploying a process with invalid expressions inside should cause the deployment to fail, since
    // the process is not deployed and useless...
    try {
      repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testInvalidExpression.bpmn20.xml")
        .deploy();

      fail("Expected exception when deploying process with invalid expression.");
    }
    catch(ProcessEngineException expected) {
      // Check if no deployments are made
      assertEquals(0, repositoryService.createDeploymentQuery().count());
      assertTextPresent("ENGINE-01009 Error while parsing process", expected.getMessage());
    }
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml"})
  public void testDeploymentIdOfResource() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();

    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    assertEquals(1, resources.size());

    Resource resource = resources.get(0);
    assertEquals(deploymentId, resource.getDeploymentId());
  }

  private void deleteDeployments(List<org.camunda.bpm.engine.repository.Deployment> deploymentList) {
    for (org.camunda.bpm.engine.repository.Deployment deployment : deploymentList) {
      repositoryService.deleteDeployment(deployment.getId());
    }
  }

  public void testDeployBpmnModelInstance() throws Exception {

    // given
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo").startEvent().userTask().endEvent().done();

    // when
    deploymentWithBuilder(repositoryService.createDeployment()
        .addModelInstance("foo.bpmn", modelInstance));

    // then
    assertNotNull(repositoryService.createProcessDefinitionQuery().processDefinitionResourceName("foo.bpmn").singleResult());
  }

  public void testDeployAndGetProcessDefinition() throws Exception {

    // given process model
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo").startEvent().userTask().endEvent().done();

    // when process model is deployed
    DeploymentWithDefinitions deployment = repositoryService.createDeployment()
      .addModelInstance("foo.bpmn", modelInstance).deployWithResult();
    deploymentIds.add(deployment.getId());

    // then deployment contains deployed process definitions
    List<ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();
    assertEquals(1, deployedProcessDefinitions.size());
    assertNull(deployment.getDeployedCaseDefinitions());
    assertNull(deployment.getDeployedDecisionDefinitions());
    assertNull(deployment.getDeployedDecisionRequirementsDefinitions());

    // and persisted process definition is equal to deployed process definition
    ProcessDefinition persistedProcDef = repositoryService.createProcessDefinitionQuery()
                                                          .processDefinitionResourceName("foo.bpmn")
                                                          .singleResult();
    assertEquals(persistedProcDef.getId(), deployedProcessDefinitions.get(0).getId());
  }

  public void testDeployNonExecutableProcess() throws Exception {

    // given non executable process definition
    final BpmnModelInstance modelInstance = Bpmn.createProcess("foo").startEvent().userTask().endEvent().done();

    // when process model is deployed
    DeploymentWithDefinitions deployment = repositoryService.createDeployment()
      .addModelInstance("foo.bpmn", modelInstance).deployWithResult();
    deploymentIds.add(deployment.getId());

    // then deployment contains no deployed process definition
    assertNull(deployment.getDeployedProcessDefinitions());

    // and there exist no persisted process definitions
    assertNull(repositoryService.createProcessDefinitionQuery()
                                .processDefinitionResourceName("foo.bpmn")
                                .singleResult());
  }

}
