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

package org.camunda.bpm.engine.test.api.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.bpmn.deployer.BpmnDeployer;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.history.event.UserOperationLogEntryEventEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.TimerActivateProcessDefinitionHandler;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.pvm.PvmActivity;
import org.camunda.bpm.engine.impl.pvm.PvmTransition;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.bpmn.tasklistener.util.RecorderTaskListener;
import org.camunda.bpm.engine.test.util.TestExecutionListener;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;

/**
 * @author Frederik Heremans
 * @author Joram Barrez
 * @author Roman Smirnov
 */
public class RepositoryServiceTest extends PluggableProcessEngineTestCase {

  private static final String NAMESPACE = "xmlns='http://www.omg.org/spec/BPMN/20100524/MODEL'";
  private static final String TARGET_NAMESPACE = "targetNamespace='" + BpmnParse.CAMUNDA_BPMN_EXTENSIONS_NS + "'";

  @Override
  public void tearDown() throws Exception {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    commandExecutor.execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getHistoricJobLogManager().deleteHistoricJobLogsByHandlerType(TimerActivateProcessDefinitionHandler.TYPE);
        return null;
      }
    });
  }

  private void checkDeployedBytes(InputStream deployedResource, byte[] utf8Bytes) throws IOException {
    byte[] deployedBytes = new byte[utf8Bytes.length];
    deployedResource.read(deployedBytes);

    for (int i = 0; i < utf8Bytes.length; i++) {
      assertEquals(utf8Bytes[i], deployedBytes[i]);
    }
  }

  public void testUTF8DeploymentMethod() throws IOException {
    //given utf8 charset
    Charset utf8Charset = Charset.forName("UTF-8");
    Charset defaultCharset = processEngineConfiguration.getDefaultCharset();
    processEngineConfiguration.setDefaultCharset(utf8Charset);

    //and model instance with umlauts
    String umlautsString = "äöüÄÖÜß";
    String resourceName = "deployment.bpmn";
    BpmnModelInstance instance = Bpmn.createExecutableProcess("umlautsProcess").startEvent(umlautsString).done();
    String instanceAsString = Bpmn.convertToString(instance);

    //when instance is deployed via addString method
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
                                                                               .addString(resourceName, instanceAsString)
                                                                               .deploy();

    //then bytes are saved in utf-8 format
    InputStream inputStream = repositoryService.getResourceAsStream(deployment.getId(), resourceName);
    byte[] utf8Bytes = instanceAsString.getBytes(utf8Charset);
    checkDeployedBytes(inputStream, utf8Bytes);
    repositoryService.deleteDeployment(deployment.getId());


    //when model instance is deployed via addModelInstance method
    deployment = repositoryService.createDeployment().addModelInstance(resourceName, instance).deploy();

    //then also the bytes are saved in utf-8 format
    inputStream = repositoryService.getResourceAsStream(deployment.getId(), resourceName);
    checkDeployedBytes(inputStream, utf8Bytes);

    repositoryService.deleteDeployment(deployment.getId());
    processEngineConfiguration.setDefaultCharset(defaultCharset);
  }

  @Deployment(resources = {
  "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testStartProcessInstanceById() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertNotNull(processDefinition.getId());
  }

  @Deployment(resources={
    "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testFindProcessDefinitionById() {
    List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, definitions.size());

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(definitions.get(0).getId()).singleResult();
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertNotNull(processDefinition);
    assertEquals("oneTaskProcess", processDefinition.getKey());
    assertEquals("The One Task Process", processDefinition.getName());

    processDefinition = repositoryService.getProcessDefinition(definitions.get(0).getId());
    assertEquals("This is a process for testing purposes", processDefinition.getDescription());
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Try to delete the deployment
    try {
      repositoryService.deleteDeployment(processDefinition.getDeploymentId());
      fail("Exception expected");
    } catch (ProcessEngineException pee) {
      // Exception expected when deleting deployment with running process
      assert(pee.getMessage().contains("Deletion of process definition without cascading failed."));
    }
  }

  public void testDeleteDeploymentSkipCustomListeners() {
    DeploymentBuilder deploymentBuilder =
        repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeleteProcessInstanceSkipCustomListeners.bpmn20.xml");

    String deploymentId = deploymentBuilder.deploy().getId();

    runtimeService.startProcessInstanceByKey("testProcess");

    repositoryService.deleteDeployment(deploymentId, true, false);
    assertEquals(1, TestExecutionListener.collectedEvents.size());
    TestExecutionListener.reset();

    deploymentId = deploymentBuilder.deploy().getId();

    runtimeService.startProcessInstanceByKey("testProcess");

    repositoryService.deleteDeployment(deploymentId, true, true);
    assertTrue(TestExecutionListener.collectedEvents.isEmpty());
    TestExecutionListener.reset();

  }

  public void testDeleteDeploymentSkipCustomTaskListeners() {
    DeploymentBuilder deploymentBuilder =
        repositoryService
          .createDeployment()
          .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeleteProcessInstanceSkipCustomTaskListeners.bpmn20.xml");

    String deploymentId = deploymentBuilder.deploy().getId();

    runtimeService.startProcessInstanceByKey("testProcess");

    RecorderTaskListener.getRecordedEvents().clear();

    repositoryService.deleteDeployment(deploymentId, true, false);
    assertEquals(1, RecorderTaskListener.getRecordedEvents().size());
    RecorderTaskListener.clear();

    deploymentId = deploymentBuilder.deploy().getId();

    runtimeService.startProcessInstanceByKey("testProcess");

    repositoryService.deleteDeployment(deploymentId, true, true);
    assertTrue(RecorderTaskListener.getRecordedEvents().isEmpty());
    RecorderTaskListener.clear();
  }

  public void testDeleteDeploymentSkipIoMappings() {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeleteDeploymentSkipIoMappings.bpmn20.xml");

    String deploymentId = deploymentBuilder.deploy().getId();
    runtimeService.startProcessInstanceByKey("ioMappingProcess");

    // Try to delete the deployment
    try {
      repositoryService.deleteDeployment(deploymentId, true, false, true);
    } catch (Exception e) {
      throw new ProcessEngineException("Exception is not expected when deleting deployment with running process", e);
    }
  }

  public void testDeleteDeploymentWithoutSkipIoMappings() {
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeleteDeploymentSkipIoMappings.bpmn20.xml");

    String deploymentId = deploymentBuilder.deploy().getId();
    runtimeService.startProcessInstanceByKey("ioMappingProcess");

    // Try to delete the deployment
    try {
      repositoryService.deleteDeployment(deploymentId, true, false, false);
      fail("Exception expected");
    } catch (Exception e) {
      // Exception expected when deleting deployment with running process
      // assert (e.getMessage().contains("Exception when output mapping is executed"));
      assertTextPresent("Exception when output mapping is executed", e.getMessage());
    }

    repositoryService.deleteDeployment(deploymentId, true, false, true);
  }

  public void testDeleteDeploymentNullDeploymentId() {
    try {
      repositoryService.deleteDeployment(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }

  public void testDeleteDeploymentCascadeNullDeploymentId() {
    try {
      repositoryService.deleteDeployment(null, true);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testDeleteDeploymentCascadeWithRunningInstances() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    runtimeService.startProcessInstanceById(processDefinition.getId());

    // Try to delete the deployment, no exception should be thrown
    repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/repository/one.cmmn"})
  public void testDeleteDeploymentClearsCache() {

    // fetch definition ids
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String caseDefinitionId = repositoryService.createCaseDefinitionQuery().singleResult().getId();
    // fetch CMMN model to be placed to in the cache
    repositoryService.getCmmnModelInstance(caseDefinitionId);

    DeploymentCache deploymentCache = processEngineConfiguration.getDeploymentCache();

    // ensure definitions and models are part of the cache
    assertNotNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionId));
    assertNotNull(deploymentCache.getBpmnModelInstanceCache().get(processDefinitionId));
    assertNotNull(deploymentCache.getCaseDefinitionCache().get(caseDefinitionId));
    assertNotNull(deploymentCache.getCmmnModelInstanceCache().get(caseDefinitionId));

    // when the deployment is deleted
    repositoryService.deleteDeployment(deploymentId, true);

    // then the definitions and models are removed from the cache
    assertNull(deploymentCache.getProcessDefinitionCache().get(processDefinitionId));
    assertNull(deploymentCache.getBpmnModelInstanceCache().get(processDefinitionId));
    assertNull(deploymentCache.getCaseDefinitionCache().get(caseDefinitionId));
    assertNull(deploymentCache.getCmmnModelInstanceCache().get(caseDefinitionId));
  }

  public void testFindDeploymentResourceNamesNullDeploymentId() {
    try {
      repositoryService.getDeploymentResourceNames(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }
  }

  public void testFindDeploymentResourcesNullDeploymentId() {
    try {
      repositoryService.getDeploymentResources(null);
      fail("ProcessEngineException expected");
    }
    catch (ProcessEngineException e) {
      assertTextPresent("deploymentId is null", e.getMessage());
    }
  }

  public void testDeploymentWithDelayedProcessDefinitionActivation() {

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    Date inThreeDays = new Date(startTime.getTime() + (3 * 24 * 60 * 60 * 1000));

    // Deploy process, but activate after three days
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
            .addClasspathResource("org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
            .activateProcessDefinitionsOn(inThreeDays)
            .deploy();

    assertEquals(1, repositoryService.createDeploymentQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());

    // Shouldn't be able to start a process instance
    try {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }

    List<Job> jobs = managementService.createJobQuery().list();
    managementService.executeJob(jobs.get(0).getId());
    managementService.executeJob(jobs.get(1).getId());

    assertEquals(1, repositoryService.createDeploymentQuery().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().active().count());

    // Should be able to start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testDeploymentWithDelayedProcessDefinitionAndJobDefinitionActivation() {

    Date startTime = new Date();
    ClockUtil.setCurrentTime(startTime);
    Date inThreeDays = new Date(startTime.getTime() + (3 * 24 * 60 * 60 * 1000));

    // Deploy process, but activate after three days
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/api/oneAsyncTask.bpmn")
            .activateProcessDefinitionsOn(inThreeDays)
            .deploy();

    assertEquals(1, repositoryService.createDeploymentQuery().count());

    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().active().count());

    assertEquals(1, managementService.createJobDefinitionQuery().count());
    assertEquals(1, managementService.createJobDefinitionQuery().suspended().count());
    assertEquals(0, managementService.createJobDefinitionQuery().active().count());

    // Shouldn't be able to start a process instance
    try {
      runtimeService.startProcessInstanceByKey("oneTaskProcess");
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresentIgnoreCase("suspended", e.getMessage());
    }

    Job job = managementService.createJobQuery().singleResult();
    managementService.executeJob(job.getId());

    assertEquals(1, repositoryService.createDeploymentQuery().count());

    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
    assertEquals(0, repositoryService.createProcessDefinitionQuery().suspended().count());
    assertEquals(1, repositoryService.createProcessDefinitionQuery().active().count());

    assertEquals(1, managementService.createJobDefinitionQuery().count());
    assertEquals(0, managementService.createJobDefinitionQuery().suspended().count());
    assertEquals(1, managementService.createJobDefinitionQuery().active().count());

    // Should be able to start process instance
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // Cleanup
    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingResourceInExistingDeployment() {
    // Get hold of the deployment id
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService.createDeploymentQuery().singleResult();

    try {
      repositoryService.getResourceAsStream(deployment.getId(), "org/camunda/bpm/engine/test/api/unexistingProcess.bpmn.xml");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no resource found with name", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetResourceAsStreamUnexistingDeployment() {

    try {
      repositoryService.getResourceAsStream("unexistingdeployment", "org/camunda/bpm/engine/test/api/unexistingProcess.bpmn.xml");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no resource found with name", ae.getMessage());
    }
  }


  public void testGetResourceAsStreamNullArguments() {
    try {
      repositoryService.getResourceAsStream(null, "resource");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("deploymentId is null", ae.getMessage());
    }

    try {
      repositoryService.getResourceAsStream("deployment", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("resourceName is null", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/one.cmmn" })
  public void testGetCaseDefinition() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    CaseDefinition caseDefinition = query.singleResult();
    String caseDefinitionId = caseDefinition.getId();

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertNotNull(definition);
    assertEquals(caseDefinitionId, definition.getId());
  }

  public void testGetCaseDefinitionByInvalidId() {
    try {
      repositoryService.getCaseDefinition("invalid");
    } catch (NotFoundException e) {
      assertTextPresent("no deployed case definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getCaseDefinition(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("caseDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/one.cmmn" })
  public void testGetCaseModel() throws Exception {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    CaseDefinition caseDefinition = query.singleResult();
    String caseDefinitionId = caseDefinition.getId();

    InputStream caseModel = repositoryService.getCaseModel(caseDefinitionId);

    assertNotNull(caseModel);

    byte[] readInputStream = IoUtil.readInputStream(caseModel, "caseModel");
    String model = new String(readInputStream, "UTF-8");

    assertTrue(model.contains("<case id=\"one\" name=\"One\">"));

    IoUtil.closeSilently(caseModel);
  }

  public void testGetCaseModelByInvalidId() throws Exception {
    try {
      repositoryService.getCaseModel("invalid");
    } catch (ProcessEngineException e) {
      assertTextPresent("no deployed case definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getCaseModel(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("caseDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/one.dmn" })
  public void testGetDecisionDefinition() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    DecisionDefinition decisionDefinition = query.singleResult();
    String decisionDefinitionId = decisionDefinition.getId();

    DecisionDefinition definition = repositoryService.getDecisionDefinition(decisionDefinitionId);

    assertNotNull(definition);
    assertEquals(decisionDefinitionId, definition.getId());
  }

  public void testGetDecisionDefinitionByInvalidId() {
    try {
      repositoryService.getDecisionDefinition("invalid");
      fail();
    } catch (NotFoundException e) {
      assertTextPresent("no deployed decision definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getDecisionDefinition(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("decisionDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/drg.dmn" })
  public void testGetDecisionRequirementsDefinition() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    DecisionRequirementsDefinition decisionRequirementsDefinition = query.singleResult();
    String decisionRequirementsDefinitionId = decisionRequirementsDefinition.getId();

    DecisionRequirementsDefinition definition = repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);

    assertNotNull(definition);
    assertEquals(decisionRequirementsDefinitionId, definition.getId());
  }

  public void testGetDecisionRequirementsDefinitionByInvalidId() {
    try {
      repositoryService.getDecisionRequirementsDefinition("invalid");
      fail();
    } catch (Exception e) {
      assertTextPresent("no deployed decision requirements definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getDecisionRequirementsDefinition(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("decisionRequirementsDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/one.dmn" })
  public void testGetDecisionModel() throws Exception {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    DecisionDefinition decisionDefinition = query.singleResult();
    String decisionDefinitionId = decisionDefinition.getId();

    InputStream decisionModel = repositoryService.getDecisionModel(decisionDefinitionId);

    assertNotNull(decisionModel);

    byte[] readInputStream = IoUtil.readInputStream(decisionModel, "decisionModel");
    String model = new String(readInputStream, "UTF-8");

    assertTrue(model.contains("<decision id=\"one\" name=\"One\">"));

    IoUtil.closeSilently(decisionModel);
  }

  public void testGetDecisionModelByInvalidId() throws Exception {
    try {
      repositoryService.getDecisionModel("invalid");
    } catch (ProcessEngineException e) {
      assertTextPresent("no deployed decision definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getDecisionModel(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("decisionDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/drg.dmn" })
  public void testGetDecisionRequirementsModel() throws Exception {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    DecisionRequirementsDefinition decisionRequirementsDefinition = query.singleResult();
    String decisionRequirementsDefinitionId = decisionRequirementsDefinition.getId();

    InputStream decisionRequirementsModel = repositoryService.getDecisionRequirementsModel(decisionRequirementsDefinitionId);

    assertNotNull(decisionRequirementsModel);

    byte[] readInputStream = IoUtil.readInputStream(decisionRequirementsModel, "decisionRequirementsModel");
    String model = new String(readInputStream, "UTF-8");

    assertTrue(model.contains("<definitions id=\"dish\" name=\"Dish\" namespace=\"test-drg\""));
    IoUtil.closeSilently(decisionRequirementsModel);
  }

  public void testGetDecisionRequirementsModelByInvalidId() throws Exception {
    try {
      repositoryService.getDecisionRequirementsModel("invalid");
    } catch (ProcessEngineException e) {
      assertTextPresent("no deployed decision requirements definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getDecisionRequirementsModel(null);
      fail();
    } catch (NotValidException e) {
      assertTextPresent("decisionRequirementsDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/repository/drg.dmn",
                           "org/camunda/bpm/engine/test/repository/drg.png" })
  public void testGetDecisionRequirementsDiagram() throws Exception {

    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    DecisionRequirementsDefinition decisionRequirementsDefinition = query.singleResult();
    String decisionRequirementsDefinitionId = decisionRequirementsDefinition.getId();

    InputStream actualDrd = repositoryService.getDecisionRequirementsDiagram(decisionRequirementsDefinitionId);

    assertNotNull(actualDrd);
  }

  public void testGetDecisionRequirementsDiagramByInvalidId() throws Exception {
    try {
      repositoryService.getDecisionRequirementsDiagram("invalid");
    } catch (ProcessEngineException e) {
      assertTextPresent("no deployed decision requirements definition found with id 'invalid'", e.getMessage());
    }

    try {
      repositoryService.getDecisionRequirementsDiagram(null);
    } catch (ProcessEngineException e) {
      assertTextPresent("decisionRequirementsDefinitionId is null", e.getMessage());
    }
  }

  public void testDeployRevisedProcessAfterDeleteOnOtherProcessEngine() {

    // Setup both process engines
    ProcessEngine processEngine1 = new StandaloneProcessEngineConfiguration()
      .setProcessEngineName("reboot-test-schema")
      .setDatabaseSchemaUpdate(org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
      .setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000")
      .setJobExecutorActivate(false)
      .buildProcessEngine();
    RepositoryService repositoryService1 = processEngine1.getRepositoryService();

    ProcessEngine processEngine2 = new StandaloneProcessEngineConfiguration()
      .setProcessEngineName("reboot-test")
      .setDatabaseSchemaUpdate(org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE)
      .setJdbcUrl("jdbc:h2:mem:activiti-process-cache-test;DB_CLOSE_DELAY=1000")
      .setJobExecutorActivate(false)
      .buildProcessEngine();
    RepositoryService repositoryService2 = processEngine2.getRepositoryService();
    RuntimeService runtimeService2 = processEngine2.getRuntimeService();
    TaskService taskService2 = processEngine2.getTaskService();

    // Deploy first version of process: start->originalTask->end on first process engine
    String deploymentId = repositoryService1.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeployRevisedProcessAfterDeleteOnOtherProcessEngine.v1.bpmn20.xml")
      .deploy()
      .getId();

    // Start process instance on second engine
    String processDefinitionId = repositoryService2.createProcessDefinitionQuery().singleResult().getId();
    runtimeService2.startProcessInstanceById(processDefinitionId);
    Task task = taskService2.createTaskQuery().singleResult();
    assertEquals("original task", task.getName());

    // Delete the deployment on second process engine
    repositoryService2.deleteDeployment(deploymentId, true);
    assertEquals(0, repositoryService2.createDeploymentQuery().count());
    assertEquals(0, runtimeService2.createProcessInstanceQuery().count());

    // deploy a revised version of the process: start->revisedTask->end on first process engine
    //
    // Before the bugfix, this would set the cache on the first process engine,
    // but the second process engine still has the original process definition in his cache.
    // Since there is a deployment delete in between, the new generated process definition id is the same
    // as in the original deployment, making the second process engine using the old cached process definition.
    deploymentId = repositoryService1.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/RepositoryServiceTest.testDeployRevisedProcessAfterDeleteOnOtherProcessEngine.v2.bpmn20.xml")
      .deploy()
      .getId();

    // Start process instance on second process engine -> must use revised process definition
    processDefinitionId = repositoryService2.createProcessDefinitionQuery().singleResult().getId();
    runtimeService2.startProcessInstanceByKey("oneTaskProcess");
    task = taskService2.createTaskQuery().singleResult();
    assertEquals("revised task", task.getName());

    // cleanup
    repositoryService1.deleteDeployment(deploymentId, true);
    processEngine1.close();
    processEngine2.close();
  }

  public void testDeploymentPersistence() {
    org.camunda.bpm.engine.repository.Deployment deployment = repositoryService
      .createDeployment()
      .name("strings")
      .addString("org/camunda/bpm/engine/test/test/HelloWorld.string", "hello world")
      .addString("org/camunda/bpm/engine/test/test/TheAnswer.string", "42")
      .deploy();

    List<org.camunda.bpm.engine.repository.Deployment> deployments
      = repositoryService.createDeploymentQuery().list();
    assertEquals(1, deployments.size());
    deployment = deployments.get(0);

    assertEquals("strings", deployment.getName());
    assertNotNull(deployment.getDeploymentTime());

    String deploymentId = deployment.getId();
    List<String> resourceNames = repositoryService.getDeploymentResourceNames(deploymentId);
    Set<String> expectedResourceNames = new HashSet<String>();
    expectedResourceNames.add("org/camunda/bpm/engine/test/test/HelloWorld.string");
    expectedResourceNames.add("org/camunda/bpm/engine/test/test/TheAnswer.string");
    assertEquals(expectedResourceNames, new HashSet<String>(resourceNames));

    InputStream resourceStream = repositoryService.getResourceAsStream(deploymentId, "org/camunda/bpm/engine/test/test/HelloWorld.string");
    assertTrue(Arrays.equals("hello world".getBytes(), IoUtil.readInputStream(resourceStream, "test")));

    resourceStream = repositoryService.getResourceAsStream(deploymentId, "org/camunda/bpm/engine/test/test/TheAnswer.string");
    assertTrue(Arrays.equals("42".getBytes(), IoUtil.readInputStream(resourceStream, "test")));

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionPersistence() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml")
      .deploy()
      .getId();

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();

    assertEquals(2, processDefinitions.size());

    repositoryService.deleteDeployment(deploymentId);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/dmn/Example.dmn"})
  public void testDecisionDefinitionUpdateTimeToLive() {
    //given
    DecisionDefinition decisionDefinition = findOnlyDecisionDefinition();

    //when
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), 6);

    //then
    decisionDefinition = findOnlyDecisionDefinition();
    assertEquals(6, decisionDefinition.getHistoryTimeToLive().intValue());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/dmn/Example.dmn"})
  public void testDecisionDefinitionUpdateTimeToLiveNull() {
    //given
    DecisionDefinition decisionDefinition = findOnlyDecisionDefinition();

    //when
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), null);

    //then
    decisionDefinition = (DecisionDefinitionEntity) repositoryService.getDecisionDefinition(decisionDefinition.getId());
    assertEquals(null, decisionDefinition.getHistoryTimeToLive());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/dmn/Example.dmn"})
  public void testDecisionDefinitionUpdateTimeToLiveNegative() {
    //given
    DecisionDefinition decisionDefinition = findOnlyDecisionDefinition();

    //when
    try {
      repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), -1);
      fail("Exception is expected, that negative value is not allowed.");
    } catch (BadUserRequestException ex) {
      assertTrue(ex.getMessage().contains("greater than"));
    }

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionUpdateTimeToLive() {
    //given
    ProcessDefinition processDefinition = findOnlyProcessDefinition();

    //when
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), 6);

    //then
    processDefinition = findOnlyProcessDefinition();
    assertEquals(6, processDefinition.getHistoryTimeToLive().intValue());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionUpdateTimeToLiveNull() {
    //given
    ProcessDefinition processDefinition = findOnlyProcessDefinition();

    //when
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), null);

    //then
    processDefinition = findOnlyProcessDefinition();
    assertEquals(null, processDefinition.getHistoryTimeToLive());

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionUpdateTimeToLiveNegative() {
    //given
    ProcessDefinition processDefinition = findOnlyProcessDefinition();

    //when
    try {
      repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), -1);
      fail("Exception is expected, that negative value is not allowed.");
    } catch (BadUserRequestException ex) {
      assertTrue(ex.getMessage().contains("greater than"));
    }

  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"})
  public void testProcessDefinitionUpdateTimeToLiveUserOperationLog() {
    //given
    ProcessDefinition processDefinition = findOnlyProcessDefinition();
    Integer timeToLiveOrgValue = processDefinition.getHistoryTimeToLive();
    processEngine.getIdentityService().setAuthenticatedUserId("userId");

    //when
    Integer timeToLiveNewValue = 6;
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinition.getId(), timeToLiveNewValue);

    //then
    List<UserOperationLogEntry> opLogEntries = processEngine.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(1, opLogEntries.size());
    final UserOperationLogEntryEventEntity userOperationLogEntry = (UserOperationLogEntryEventEntity)opLogEntries.get(0);

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_UPDATE_HISTORY_TIME_TO_LIVE, userOperationLogEntry.getOperationType());
    assertEquals(processDefinition.getKey(), userOperationLogEntry.getProcessDefinitionKey());
    assertEquals(processDefinition.getId(), userOperationLogEntry.getProcessDefinitionId());
    assertEquals("historyTimeToLive", userOperationLogEntry.getProperty());
    assertEquals(timeToLiveOrgValue, Integer.valueOf(userOperationLogEntry.getOrgValue()));
    assertEquals(timeToLiveNewValue, Integer.valueOf(userOperationLogEntry.getNewValue()));

  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testUpdateHistoryTimeToLive() {
    // given
    // there exists a deployment containing a case definition with key "oneTaskCase"

    CaseDefinition caseDefinition = findOnlyCaseDefinition();

    // when
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinition.getId(), 6);

    // then
    caseDefinition = findOnlyCaseDefinition();

    assertEquals(6, caseDefinition.getHistoryTimeToLive().intValue());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testUpdateHistoryTimeToLiveNull() {
    // given
    // there exists a deployment containing a case definition with key "oneTaskCase"

    CaseDefinition caseDefinition = findOnlyCaseDefinition();

    // when
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinition.getId(), null);

    // then
    caseDefinition = findOnlyCaseDefinition();

    assertEquals(null, caseDefinition.getHistoryTimeToLive());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testUpdateHistoryTimeToLiveNegative() {
    // given
    // there exists a deployment containing a case definition with key "oneTaskCase"

    CaseDefinition caseDefinition = findOnlyCaseDefinition();

    // when
    try {
      repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinition.getId(), -1);
      fail("Exception is expected, that negative value is not allowed.");
    } catch (BadUserRequestException ex) {
      assertTrue(ex.getMessage().contains("greater than"));
    }
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  public void testUpdateHistoryTimeToLiveInCache() {
    // given
    // there exists a deployment containing a case definition with key "oneTaskCase"

    CaseDefinition caseDefinition = findOnlyCaseDefinition();

    // assume
    assertNull(caseDefinition.getHistoryTimeToLive());

    // when
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinition.getId(), 10);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinition.getId());
    assertEquals(Integer.valueOf(10), definition.getHistoryTimeToLive());
  }

  private CaseDefinition findOnlyCaseDefinition() {
    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().list();
    assertNotNull(caseDefinitions);
    assertEquals(1, caseDefinitions.size());
    return caseDefinitions.get(0);
  }

  private ProcessDefinition findOnlyProcessDefinition() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertNotNull(processDefinitions);
    assertEquals(1, processDefinitions.size());
    return processDefinitions.get(0);
  }

  private DecisionDefinition findOnlyDecisionDefinition() {
    List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().list();
    assertNotNull(decisionDefinitions);
    assertEquals(1, decisionDefinitions.size());
    return decisionDefinitions.get(0);
  }

  public void testProcessDefinitionIntrospection() {
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml")
      .deploy()
      .getId();

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ReadOnlyProcessDefinition processDefinition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(procDefId);

    assertEquals(procDefId, processDefinition.getId());
    assertEquals("Process One", processDefinition.getName());
    assertEquals("the first process", processDefinition.getProperty("documentation"));

    PvmActivity start = processDefinition.findActivity("start");
    assertNotNull(start);
    assertEquals("start", start.getId());
    assertEquals("S t a r t", start.getProperty("name"));
    assertEquals("the start event", start.getProperty("documentation"));
    assertEquals(Collections.EMPTY_LIST, start.getActivities());
    List<PvmTransition> outgoingTransitions = start.getOutgoingTransitions();
    assertEquals(1, outgoingTransitions.size());
    assertEquals("${a == b}", outgoingTransitions.get(0).getProperty(BpmnParse.PROPERTYNAME_CONDITION_TEXT));

    PvmActivity end = processDefinition.findActivity("end");
    assertNotNull(end);
    assertEquals("end", end.getId());

    PvmTransition transition = outgoingTransitions.get(0);
    assertEquals("flow1", transition.getId());
    assertEquals("Flow One", transition.getProperty("name"));
    assertEquals("The only transitions in the process", transition.getProperty("documentation"));
    assertSame(start, transition.getSource());
    assertSame(end, transition.getDestination());

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testProcessDefinitionQuery() {
    String deployment1Id = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml")
      .deploy()
      .getId();

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .orderByProcessDefinitionName().asc().orderByProcessDefinitionVersion().asc()
      .list();

    assertEquals(2, processDefinitions.size());

    String deployment2Id = repositoryService
            .createDeployment()
            .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml")
            .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml")
            .deploy()
            .getId();

    assertEquals(4, repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionName().asc().count());
    assertEquals(2, repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionName().asc().count());

    deleteDeployments(Arrays.asList(deployment1Id, deployment2Id));
  }

  public void testGetProcessDefinitions() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 1' isExecutable='true' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 2' isExecutable='true' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report 3' isExecutable='true' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 1' isExecutable='true' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='EN' name='Expense Note 2' isExecutable='true' />" + "</definitions>")));

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .orderByProcessDefinitionKey().asc()
      .orderByProcessDefinitionVersion().desc()
      .list();

    assertNotNull(processDefinitions);

    assertEquals(5, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note 2", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("EN:2"));
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(1);
    assertEquals("EN", processDefinition.getKey());
    assertEquals("Expense Note 1", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("EN:1"));
    assertEquals(1, processDefinition.getVersion());

    processDefinition = processDefinitions.get(2);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 3", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("IDR:3"));
    assertEquals(3, processDefinition.getVersion());

    processDefinition = processDefinitions.get(3);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 2", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("IDR:2"));
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(4);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report 1", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("IDR:1"));
    assertEquals(1, processDefinition.getVersion());

    deleteDeployments(deploymentIds);
  }

  public void testDeployIdenticalProcessDefinitions() {
    List<String> deploymentIds = new ArrayList<String>();
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' isExecutable='true' />" + "</definitions>")));
    deploymentIds.add(deployProcessString(("<definitions " + NAMESPACE + " " + TARGET_NAMESPACE + ">" + "  <process id='IDR' name='Insurance Damage Report' isExecutable='true' />" + "</definitions>")));

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .orderByProcessDefinitionKey().asc()
      .orderByProcessDefinitionVersion().desc()
      .list();

    assertNotNull(processDefinitions);
    assertEquals(2, processDefinitions.size());

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("IDR:2"));
    assertEquals(2, processDefinition.getVersion());

    processDefinition = processDefinitions.get(1);
    assertEquals("IDR", processDefinition.getKey());
    assertEquals("Insurance Damage Report", processDefinition.getName());
    assertTrue(processDefinition.getId().startsWith("IDR:1"));
    assertEquals(1, processDefinition.getVersion());

    deleteDeployments(deploymentIds);
  }

  private String deployProcessString(String processString) {
    String resourceName = "xmlString." + BpmnDeployer.BPMN_RESOURCE_SUFFIXES[0];
    return repositoryService.createDeployment().addString(resourceName, processString).deploy().getId();
  }

  private void deleteDeployments(Collection<String> deploymentIds) {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId);
    }
  }

}
