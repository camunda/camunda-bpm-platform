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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;


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
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);
    
    // verify bpmn file name
    assertEquals(1, deploymentResources.size());
    assertEquals(bpmnResourceName, deploymentResources.get(0));
    
    bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml";
    repositoryService.createDeployment().enableDuplicateFiltering().addClasspathResource(bpmnResourceName).name("twice").deploy();
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deploymentList.size());
    
    for (org.camunda.bpm.engine.repository.Deployment deployment : deploymentList) {
      repositoryService.deleteDeployment(deployment.getId());
    }
  }
  
  public void testDiagramCreationDisabled() {
    repositoryService.createDeployment().addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseDiagramInterchangeElements.bpmn20.xml").deploy();

    // Graphical information is not yet exposed publicly, so we need to do some plumbing
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    ProcessDefinitionEntity processDefinitionEntity = commandExecutor.execute(new Command<ProcessDefinitionEntity>() {
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
      assertTrue(expected.getMessage().startsWith("Error while parsing process: "));
    }
  }
  
  /**
   * Just assures that diagram creation actually creates something and does not crash.
   * No qualitative evaluation of created diagram.
   * @throws IOException 
   */
  public void testProcessDiagramCreation() throws IOException {
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    config.setCreateDiagramOnDeploy(true);
    ProcessEngine engine = config.buildProcessEngine();
    String deploymentId = engine.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramCreation.bpmn20.xml")
      .deploy().getId();
    
    ProcessDefinition definition = engine.getRepositoryService().createProcessDefinitionQuery().singleResult();
    String expectedDiagramName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramCreation.processDiagramProcess.png";
    assertEquals(expectedDiagramName, definition.getDiagramResourceName());
    
    InputStream diagramStream = engine.getRepositoryService().getProcessDiagram(definition.getId());
    assertNotNull(diagramStream);
    diagramStream.close();
    
    // clean db
    engine.getRepositoryService().deleteDeployment(deploymentId);
  }
  
}
