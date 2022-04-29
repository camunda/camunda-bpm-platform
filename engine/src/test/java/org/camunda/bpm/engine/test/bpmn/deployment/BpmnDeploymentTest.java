/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.bpmn.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentHandlerFactory;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.camunda.commons.testing.WatchLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


/**
 * @author Joram Barrez
 * @author Thorben Lindhauer
 */
public class BpmnDeploymentTest extends PluggableProcessEngineTest {

  protected static final String CMD_LOGGER = "org.camunda.bpm.engine.cmd";
  
  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  protected DeploymentHandlerFactory defaultDeploymentHandlerFactory;
  protected DeploymentHandlerFactory customDeploymentHandlerFactory;
  
  @Before
  public void setUp() throws Exception {
    defaultDeploymentHandlerFactory = processEngineConfiguration.getDeploymentHandlerFactory();
    customDeploymentHandlerFactory = new VersionedDeploymentHandlerFactory();
  }

  @After
  public void tearDown() throws Exception {
    processEngineConfiguration.setDeploymentHandlerFactory(defaultDeploymentHandlerFactory);
  }

  @Deployment
  @Test
  public void testGetBpmnXmlFileThroughService() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources.size()).isEqualTo(1);
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition.getResourceName()).isEqualTo(bpmnResourceName);
    assertThat(processDefinition.getDiagramResourceName()).isNull();
    assertThat(processDefinition.hasStartFormKey()).isFalse();

    ReadOnlyProcessDefinition readOnlyProcessDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinition.getId());
    assertThat(readOnlyProcessDefinition.getDiagramResourceName()).isNull();

    // verify content
    InputStream deploymentInputStream = repositoryService.getResourceAsStream(deploymentId, bpmnResourceName);
    String contentFromDeployment = readInputStreamToString(deploymentInputStream);
    assertThat(contentFromDeployment.length() > 0).isTrue();
    assertThat(contentFromDeployment.contains("process id=\"emptyProcess\"")).isTrue();

    InputStream fileInputStream = ReflectUtil.getResourceAsStream("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml");
    String contentFromFile = readInputStreamToString(fileInputStream);
    assertThat(contentFromDeployment).isEqualTo(contentFromFile);
  }

  private String readInputStreamToString(InputStream inputStream) {
    byte[] bytes = IoUtil.readInputStream(inputStream, "input stream");
    return new String(bytes);
  }

  public void FAILING_testViolateProcessDefinitionIdMaximumLength() {
    // given
    DeploymentBuilder deployment = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/processWithLongId.bpmn20.xml");
    // when
    assertThatThrownBy(() -> testRule.deploy(deployment))
      .hasMessageContaining("id can be maximum 64 characters");
    // then
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  @Test
  public void testDeploySameFileTwice() {
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName)
        .name("twice"));

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources.size()).isEqualTo(1);
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName)
        .name("twice"));
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(1);
  }

  @Test
  public void shouldNotFilterDuplicateWithSameFileDeployedTwiceWithoutDeploymentName() {
    // given
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName));
    // when
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName));
    // then
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList.size()).isEqualTo(2);
  }

  @Test
  @WatchLogger(loggerNames = CMD_LOGGER, level = "WARN")
  public void shouldLogWarningForDuplicateFilteringWithoutName() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("model.bpmn", model);

    // when
    testRule.deploy(deploymentBuilder);
    
    // then
    assertThat(loggingRule.getFilteredLog(CMD_LOGGER, "Deployment name set to null. Filtering duplicates will not work properly.").size()).isEqualTo(1);
  }
  
  @Test
  @WatchLogger(loggerNames = CMD_LOGGER, level = "WARN")
  public void shouldLogWarningForDuplicateFilteringWithoutPreviousDeploymentName() {
    // given
    BpmnModelInstance model = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

    DeploymentWithDefinitions deployment = testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("model.bpmn", model));
    
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addDeploymentResources(deployment.getId());

    // when
    testRule.deploy(deploymentBuilder);
    
    // then
    assertThat(loggingRule.getFilteredLog(CMD_LOGGER, "Deployment name set to null. Filtering duplicates will not work properly.").size()).isEqualTo(1);
  }
  
  @Test
  public void testDuplicateFilteringDefaultBehavior() {
    // given
    BpmnModelInstance oldModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("3").done();
    BpmnModelInstance newModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("1").done();

    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("model", oldModel)
      .name("defaultDeploymentHandling"));

    // when
    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("model", newModel)
      .name("defaultDeploymentHandling"));

    // then
    long deploymentCount = repositoryService.createDeploymentQuery().count();
    assertThat(deploymentCount).isEqualTo(2);
  }

  @Test
  public void testDuplicateFilteringCustomBehavior() {
    // given
    processEngineConfiguration.setDeploymentHandlerFactory(customDeploymentHandlerFactory);
    BpmnModelInstance oldModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("1").startEvent().done();
    BpmnModelInstance newModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("2").startEvent().done();

    DeploymentWithDefinitions deployment1 = testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addModelInstance("model.bpmn", oldModel)
        .name("customDeploymentHandling"));

    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addModelInstance("model.bpmn", newModel)
        .name("customDeploymentHandling"));

    // when
    DeploymentWithDefinitions deployment3 = testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addModelInstance("model.bpmn", oldModel)
        .name("customDeploymentHandling"));

    // then
    long deploymentCount = repositoryService.createDeploymentQuery().count();
    assertThat(deploymentCount).isEqualTo(2);
    assertThat(deployment3.getId()).isEqualTo(deployment1.getId());
  }

  @Test
  public void testPartialChangesDeployAll() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").startEvent().done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").startEvent().done();
    DeploymentWithDefinitions deployment1 = testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(false)
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .name("twice"));

    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deployment1.getId());
    assertThat(deploymentResources.size()).isEqualTo(2);

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().endEvent().done();

    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(false)
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .name("twice"));
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList.size()).isEqualTo(2);

    // there should be new versions of both processes
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count()).isEqualTo(2);
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count()).isEqualTo(2);
  }

  @Test
  public void testPartialChangesDeployChangedOnly() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").startEvent().done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").startEvent().done();
    DeploymentWithDefinitions deployment1 = testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .name("thrice"));

    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deployment1.getId());
    assertThat(deploymentResources.size()).isEqualTo(2);

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().endEvent().done();

    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .name("thrice"));

    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList.size()).isEqualTo(2);

    // there should be only one version of process 1
    ProcessDefinition process1Definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").singleResult();
    assertThat(process1Definition).isNotNull();
    assertThat(process1Definition.getVersion()).isEqualTo(1);
    assertThat(process1Definition.getDeploymentId()).isEqualTo(deployment1.getId());

    // there should be two versions of process 2
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count()).isEqualTo(2);

    BpmnModelInstance anotherChangedModel2 = Bpmn.createExecutableProcess("process2").startEvent().sequenceFlowId("flow").endEvent().done();

    // testing with a third deployment to ensure the change check is not only performed against
    // the last version of the deployment
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(true)
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", anotherChangedModel2)
        .name("thrice"));

    // there should still be one version of process 1
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count()).isEqualTo(1);

    // there should be three versions of process 2
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count()).isEqualTo(3);
  }

  @Test
  public void testPartialChangesRedeployOldVersion() {
    // deployment 1 deploys process version 1
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").startEvent().done();
    testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("process1.bpmn20.xml", model1)
      .name("deployment"));

    // deployment 2 deploys process version 2
    BpmnModelInstance changedModel1 = Bpmn.createExecutableProcess("process1").startEvent().endEvent().done();
    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", changedModel1)
      .name("deployment"));

    // deployment 3 deploys process version 1 again
    testRule.deploy(repositoryService.createDeployment()
      .enableDuplicateFiltering(true)
      .addModelInstance("process1.bpmn20.xml", model1)
      .name("deployment"));

    // should result in three process definitions
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count()).isEqualTo(3);
  }

  @Test
  public void testDeployTwoProcessesWithDuplicateIdAtTheSameTime() {
    // given
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    String bpmnResourceName2 = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService2.bpmn20.xml";
    // when
    assertThatThrownBy(() -> testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName)
        .addClasspathResource(bpmnResourceName2)
        .name("duplicateAtTheSameTime")));
    // then
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  @Test
  public void testDeployDifferentFiles() {
    String bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml";
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName)
        .name("twice"));

    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
    List<String> deploymentResources = repositoryService.getDeploymentResourceNames(deploymentId);

    // verify bpmn file name
    assertThat(deploymentResources.size()).isEqualTo(1);
    assertThat(deploymentResources.get(0)).isEqualTo(bpmnResourceName);

    bpmnResourceName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml";
    testRule.deploy(repositoryService.createDeployment()
        .enableDuplicateFiltering(false)
        .addClasspathResource(bpmnResourceName)
        .name("twice"));
    List<org.camunda.bpm.engine.repository.Deployment> deploymentList = repositoryService.createDeploymentQuery().list();
    assertThat(deploymentList.size()).isEqualTo(2);
  }

  @Test
  public void testDiagramCreationDisabled() {
    testRule.deploy(repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/parse/BpmnParseTest.testParseDiagramInterchangeElements.bpmn20.xml"));

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

    assertThat(processDefinitionEntity).isNotNull();
    assertThat(processDefinitionEntity.getActivities().size()).isEqualTo(7);

    // Check that no diagram has been created
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(processDefinitionEntity.getDeploymentId());
    assertThat(resourceNames.size()).isEqualTo(1);
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg"
  })
  @Test
  public void testProcessDiagramResource() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    assertThat(processDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.bpmn20.xml");
    assertThat(processDefinition.hasStartFormKey()).isTrue();

    String diagramResourceName = processDefinition.getDiagramResourceName();
    assertThat(diagramResourceName).isEqualTo("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");

    InputStream diagramStream = repositoryService
        .getResourceAsStream(processDefinition.getDeploymentId(),
                             "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramResource.jpg");
    byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertThat(diagramBytes.length).isEqualTo(33343);
  }

  @Deployment(resources={
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.bpmn20.xml",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg",
          "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg"
        })
  @Test
  public void testMultipleDiagramResourcesProvided() {
    ProcessDefinition processA = repositoryService.createProcessDefinitionQuery().processDefinitionKey("a").singleResult();
    ProcessDefinition processB = repositoryService.createProcessDefinitionQuery().processDefinitionKey("b").singleResult();
    ProcessDefinition processC = repositoryService.createProcessDefinitionQuery().processDefinitionKey("c").singleResult();

    assertThat(processA.getDiagramResourceName()).isEqualTo("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.a.jpg");
    assertThat(processB.getDiagramResourceName()).isEqualTo("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.b.jpg");
    assertThat(processC.getDiagramResourceName()).isEqualTo("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testMultipleDiagramResourcesProvided.c.jpg");
  }

  @Deployment
  @Test
  public void testProcessDefinitionDescription() {
    String id = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(id);
    assertThat(processDefinition.getDescription()).isEqualTo("This is really good process documentation!");
  }

  @Test
  public void testDeployInvalidExpression() {
    // given
    // ACT-1391: Deploying a process with invalid expressions inside should cause the deployment to fail, since
    // the process is not deployed and useless
    DeploymentBuilder deployment = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testInvalidExpression.bpmn20.xml");
    // when
    assertThatThrownBy(() -> testRule.deploy(deployment))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("ENGINE-01009 Error while parsing process")
      .withFailMessage("Expected exception when deploying process with invalid expression.");
    // then
    assertThat(repositoryService.createDeploymentQuery().count()).isEqualTo(0);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testGetBpmnXmlFileThroughService.bpmn20.xml"})
  @Test
  public void testDeploymentIdOfResource() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();

    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    assertThat(resources.size()).isEqualTo(1);

    Resource resource = resources.get(0);
    assertThat(resource.getDeploymentId()).isEqualTo(deploymentId);
  }

  @Test
  public void testDeployBpmnModelInstance() throws Exception {

    // given
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo").startEvent().userTask().endEvent().done();

    // when
    testRule.deploy(repositoryService.createDeployment()
        .addModelInstance("foo.bpmn", modelInstance));

    // then
    assertThat(repositoryService.createProcessDefinitionQuery().processDefinitionResourceName("foo.bpmn").singleResult()).isNotNull();
  }

  @Test
  public void testDeployAndGetProcessDefinition() throws Exception {

    // given process model
    final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("foo").startEvent().userTask().endEvent().done();

    // when process model is deployed
    DeploymentWithDefinitions deployment = testRule.deploy(
        repositoryService.createDeployment()
            .addModelInstance("foo.bpmn", modelInstance));

    // then deployment contains deployed process definitions
    List<ProcessDefinition> deployedProcessDefinitions = deployment.getDeployedProcessDefinitions();
    assertThat(deployedProcessDefinitions.size()).isEqualTo(1);
    assertThat(deployment.getDeployedCaseDefinitions()).isNull();;
    assertThat(deployment.getDeployedDecisionDefinitions()).isNull();;
    assertThat(deployment.getDeployedDecisionRequirementsDefinitions()).isNull();;

    // and persisted process definition is equal to deployed process definition
    ProcessDefinition persistedProcDef = repositoryService.createProcessDefinitionQuery()
                                                          .processDefinitionResourceName("foo.bpmn")
                                                          .singleResult();
    assertThat(deployedProcessDefinitions.get(0).getId()).isEqualTo(persistedProcDef.getId());
  }

  @Test
  public void testDeployNonExecutableProcess() throws Exception {

    // given non executable process definition
    final BpmnModelInstance modelInstance = Bpmn.createProcess("foo").startEvent().userTask().endEvent().done();

    // when process model is deployed
    DeploymentWithDefinitions deployment = testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("foo.bpmn", modelInstance));

    // then deployment contains no deployed process definition
    assertThat(deployment.getDeployedProcessDefinitions()).isNull();

    // and there exist no persisted process definitions
    assertThat(repositoryService.createProcessDefinitionQuery()
                                .processDefinitionResourceName("foo.bpmn")
                                .singleResult()).isNull();;
  }

}
