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
package org.camunda.bpm.engine.test.api.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Roman Smirnov
 *
 */
public class RedeploymentTest {

  public static final String DEPLOYMENT_NAME = "my-deployment";
  public static final String PROCESS_KEY = "process";
  public static final String PROCESS_1_KEY = "process-1";
  public static final String PROCESS_2_KEY = "process-2";
  public static final String PROCESS_3_KEY = "process-3";
  public static final String RESOURCE_NAME = "path/to/my/process.bpmn";
  public static final String RESOURCE_1_NAME = "path/to/my/process1.bpmn";
  public static final String RESOURCE_2_NAME = "path/to/my/process2.bpmn";
  public static final String RESOURCE_3_NAME = "path/to/my/process3.bpmn";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected boolean enforceHistoryTimeToLive;

  @Before
  public void setUp() {
    repositoryService = engineRule.getRepositoryService();
    enforceHistoryTimeToLive = engineRule.getProcessEngineConfiguration().isEnforceHistoryTimeToLive();
  }

  @After
  public void tearDown() {
    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(enforceHistoryTimeToLive);
  }

  @Test
  public void testRedeployInvalidDeployment() {

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources("not-existing")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById("not-existing", "an-id")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("not-existing", Collections.singletonList("an-id"))
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName("not-existing", "a-name")
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("not-existing", Collections.singletonList("a-name"))
        .deploy();
      fail("It should not be able to re-deploy an unexisting deployment");
    } catch (NotFoundException e) {
      // expected
    }
  }

  @Test
  public void testNotValidDeploymentId() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(null);
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(null, "an-id");
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(null, Collections.singletonList("an-id"));
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(null, "a-name");
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(null, Collections.singletonList("a-name"));
      fail("It should not be possible to pass a null deployment id");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testRedeployUnexistingDeploymentResource() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment.getId(), "not-existing-resource.bpmn")
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(deployment.getId(),
                                      Collections.singletonList("not-existing-resource.bpmn"))
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment.getId(), "not-existing-resource-id")
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }

    try {
      // when
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(deployment.getId(), Collections.singletonList("not-existing" +
                                                                                      "-resource-id"))
        .deploy();
      fail("It should not be possible to re-deploy a not existing deployment resource");
    } catch (NotFoundException e) {
      // then
      // expected
    }
  }

  @Test
  public void testNotValidResource() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById("an-id", null);
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", null);
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", Collections.singletonList(null));
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById("an-id", new ArrayList<>());
      fail("It should not be possible to pass a null resource id");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName("an-id", null);
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", null);
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", Collections.singletonList(null));
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName("an-id", new ArrayList<>());
      fail("It should not be possible to pass a null resource name");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testRedeployNewDeployment() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName(DEPLOYMENT_NAME);

    assertNotNull(deployment1.getId());
    verifyQueryResults(query, 1);

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId()));

    // then
    assertNotNull(deployment2);
    assertNotNull(deployment2.getId());
    assertFalse(deployment1.getId().equals(deployment2.getId()));

    verifyQueryResults(query, 2);
  }

  @Test
  public void testFailingDeploymentName() {
    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .nameFromDeployment("a-deployment-id");
      fail("Cannot set name() and nameFromDeployment().");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .nameFromDeployment("a-deployment-id")
        .name(DEPLOYMENT_NAME);
      fail("Cannot set name() and nameFromDeployment().");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testRedeployDeploymentName() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    assertEquals(DEPLOYMENT_NAME, deployment1.getName());

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .nameFromDeployment(deployment1.getId())
        .addDeploymentResources(deployment1.getId()));

    // then
    assertNotNull(deployment2);
    assertEquals(deployment1.getName(), deployment2.getName());
  }

  @Test
  public void testRedeployDeploymentDifferentName() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    assertEquals(DEPLOYMENT_NAME, deployment1.getName());

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name("my-another-deployment")
        .addDeploymentResources(deployment1.getId()));

    // then
    assertNotNull(deployment2);
    assertFalse(deployment1.getName().equals(deployment2.getName()));
  }

  @Test
  public void testRedeployDeploymentSourcePropertyNotSet() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model));

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId()));

    // then
    assertNotNull(deployment2);
    assertNull(deployment2.getSource());
  }

  @Test
  public void testRedeploySetDeploymentSourceProperty() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .source("my-deployment-source")
        .addModelInstance(RESOURCE_NAME, model));

    assertEquals("my-deployment-source", deployment1.getSource());

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .source("my-another-deployment-source"));

    // then
    assertNotNull(deployment2);
    assertEquals("my-another-deployment-source", deployment2.getSource());
  }

  @Test
  public void testRedeployDeploymentResource() {
    // given

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model));

    Resource resource2 = getResourceByName(deployment2.getId(), RESOURCE_NAME);

    // when
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId()));

    // then
    Resource resource3 = getResourceByName(deployment3.getId(), RESOURCE_NAME);
    assertNotNull(resource3);

    // id
    assertNotNull(resource3.getId());
    assertFalse(resource1.getId().equals(resource3.getId()));

    // deployment id
    assertEquals(deployment3.getId(), resource3.getDeploymentId());

    // name
    assertEquals(resource1.getName(), resource3.getName());

    // bytes
    byte[] bytes1 = resource1.getBytes();
    byte[] bytes2 = resource2.getBytes();
    byte[] bytes3 = resource3.getBytes();
    assertTrue(Arrays.equals(bytes1, bytes3));
    assertFalse(Arrays.equals(bytes2, bytes3));
  }

  @Test
  public void testRedeployAllDeploymentResources() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model2)
        .addModelInstance(RESOURCE_2_NAME, model1));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId()));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
  }

  @Test
  public void testRedeployOneDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployMultipleDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when (1)
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_3_NAME));

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesByName(deployment2.getId(), Arrays.asList(RESOURCE_1_NAME, RESOURCE_3_NAME)));

    // then (2)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 4);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 4);
  }

  @Test
  public void testRedeployOneAndMultipleDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourcesByName(deployment1.getId(), Arrays.asList(RESOURCE_2_NAME, RESOURCE_3_NAME)));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);
  }

  @Test
  public void testSameDeploymentResourceByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourcesByName(deployment1.getId(), Collections.singletonList(RESOURCE_1_NAME)));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployOneDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource.getId()));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void shouldFailOnNullHTTLForAddDeploymentResourceById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

    try {
      // when
      testRule.deploy(repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME)
          .addDeploymentResourceById(deployment1.getId(), resource.getId()));

      fail("addDeploymentResourceById should fail due to enforceHistoryTimeToLive=true and null historyTimeToLive");
    } catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
    }
  }

  @Test
  public void shouldFailOnNullHTTLForAddDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    Resource resource11 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource13 = getResourceByName(deployment1.getId(), RESOURCE_3_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    Resource resource21 = getResourceByName(deployment2.getId(), RESOURCE_1_NAME);
    Resource resource23 = getResourceByName(deployment2.getId(), RESOURCE_3_NAME);

    // when (1)
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource11.getId())
        .addDeploymentResourceById(deployment1.getId(), resource13.getId()));

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

    // when
    try {
      testRule.deploy(repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME)
          .addDeploymentResourcesById(deployment2.getId(), Arrays.asList(resource21.getId(), resource23.getId())));

      fail("addDeploymentResourcesById should fail due to enforceHistoryTimeToLive=true and null HistoryTimeToLive resources");
    } catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
    }
  }

  @Test
  public void shouldFailOnNullHTTLOnAddDeploymentResourceByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

    try {
      // when
      testRule.deploy(repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME + "-3")
          .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
          .addDeploymentResourceByName(deployment2.getId(), RESOURCE_2_NAME));

      fail("addDeploymentResourcesById should fail due to enforceHistoryTimeToLive=true and null HistoryTimeToLive resources");
    } catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
    }
  }

  @Test
  public void shouldFailOnNullHTTLOnAddDeploymentResourcesByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

    try {
      // when
      testRule.deploy(repositoryService
          .createDeployment()
          .addDeploymentResourcesById(deployment1.getId(), Collections.singletonList(resource1.getId()))
          .addDeploymentResourcesByName(deployment1.getId(), Collections.singletonList(resource2.getName())));

      fail("addDeploymentResourcesByName should fail due to enforceHistoryTimeToLive=true and null HistoryTimeToLive resources");
    } catch (Exception e) {
      assertTrue(e instanceof ProcessEngineException);
    }
  }

  @Test
  public void testRedeployMultipleDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    Resource resource11 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource13 = getResourceByName(deployment1.getId(), RESOURCE_3_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    Resource resource21 = getResourceByName(deployment2.getId(), RESOURCE_1_NAME);
    Resource resource23 = getResourceByName(deployment2.getId(), RESOURCE_3_NAME);

    // when (1)
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource11.getId())
        .addDeploymentResourceById(deployment1.getId(), resource13.getId()));

    // then (1)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);

    // when (2)
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourcesById(deployment2.getId(), Arrays.asList(resource21.getId(), resource23.getId())));

    // then (2)
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 4);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 4);
  }

  @Test
  public void testRedeployOneAndMultipleDeploymentResourcesById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);
    BpmnModelInstance model3 = createProcessWithScriptTask(PROCESS_3_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);
    Resource resource3 = getResourceByName(deployment1.getId(), RESOURCE_3_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);
    model3 = createProcessWithUserTask(PROCESS_3_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2)
        .addModelInstance(RESOURCE_3_NAME, model3));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourcesById(deployment1.getId(), Arrays.asList(resource2.getId(), resource3.getId())));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 3);
  }

  @Test
  public void testRedeploySameDeploymentResourceById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourcesById(deployment1.getId(), Collections.singletonList(resource1.getId())));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployDeploymentResourceByIdAndName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceByName(deployment1.getId(), resource2.getName()));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
  }

  @Test
  public void testRedeployDeploymentResourceByIdAndNameMultiple() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    BpmnModelInstance model2 = createProcessWithUserTask(PROCESS_2_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);
    Resource resource2 = getResourceByName(deployment1.getId(), RESOURCE_2_NAME);

    // second deployment
    model1 = createProcessWithScriptTask(PROCESS_1_KEY);
    model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1)
        .addModelInstance(RESOURCE_2_NAME, model2));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);

    // when
    testRule.deploy(repositoryService
        .createDeployment()
        .addDeploymentResourcesById(deployment1.getId(), Collections.singletonList(resource1.getId()))
        .addDeploymentResourcesByName(deployment1.getId(), Collections.singletonList(resource2.getName())));

    // then
    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 3);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 3);
  }

  @Test
  public void testRedeployFormDifferentDeployments() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId()));

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployFormDifferentDeploymentsById() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());
    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());
    Resource resource2 = getResourceByName(deployment2.getId(), RESOURCE_2_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceById(deployment2.getId(), resource2.getId()));

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployFormDifferentDeploymentsByName() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .addDeploymentResourceByName(deployment2.getId(), RESOURCE_2_NAME));

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployFormDifferentDeploymentsByNameAndId() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());
    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_1_NAME);

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResourceById(deployment1.getId(), resource1.getId())
        .addDeploymentResourceByName(deployment2.getId(), RESOURCE_2_NAME));

    assertEquals(2, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
  }

  @Test
  public void testRedeployFormDifferentDeploymentsAddsNewSource() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    assertEquals(1, repositoryService.getDeploymentResources(deployment1.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_2_NAME, model2));

    assertEquals(1, repositoryService.getDeploymentResources(deployment2.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 1);

    // when
    BpmnModelInstance model3 = createProcessWithUserTask(PROCESS_3_KEY);
    Deployment deployment3 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .addModelInstance(RESOURCE_3_NAME, model3));

    assertEquals(3, repositoryService.getDeploymentResources(deployment3.getId()).size());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_2_KEY), 2);
    verifyQueryResults(query.processDefinitionKey(PROCESS_3_KEY), 1);
  }

  @Test
  public void testRedeployFormDifferentDeploymentsSameResourceName() {
    // given
    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    // first deployment
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    // second deployment
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_1_NAME, model2));

    // when
    try {
      repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME + "-3")
          .addDeploymentResources(deployment1.getId())
          .addDeploymentResources(deployment2.getId())
          .deploy();
      fail("It should not be possible to deploy different resources with same name.");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testRedeployAndAddNewResourceWithSameName() {
    // given
    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);

    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-1")
        .addModelInstance(RESOURCE_1_NAME, model1));

    // when
    BpmnModelInstance model2 = createProcessWithReceiveTask(PROCESS_2_KEY);

    try {
      repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-2")
        .addModelInstance(RESOURCE_1_NAME, model2)
        .addDeploymentResourceByName(deployment1.getId(), RESOURCE_1_NAME)
        .deploy();
      fail("It should not be possible to deploy different resources with same name.");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testRedeployEnableDuplcateChecking() {
    // given
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    BpmnModelInstance model1 = createProcessWithServiceTask(PROCESS_1_KEY);
    Deployment deployment1 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_1_NAME, model1));

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);

    // when
    Deployment deployment2 = testRule.deploy(repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .enableDuplicateFiltering(true));

    assertEquals(deployment1.getId(), deployment2.getId());

    verifyQueryResults(query.processDefinitionKey(PROCESS_1_KEY), 1);
  }

  @Test
  public void testSimpleProcessApplicationDeployment() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = testRule.deploy(
        repositoryService
            .createDeployment(processApplication.getReference())
            .name(DEPLOYMENT_NAME)
            .addModelInstance(RESOURCE_NAME, model)
            .enableDuplicateFiltering(true));

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // when
    ProcessApplicationDeployment deployment2 = testRule.deploy(
        repositoryService
            .createDeployment(processApplication.getReference())
            .name(DEPLOYMENT_NAME)
            .addDeploymentResourceById(deployment1.getId(), resource1.getId()));

    // then
    // registration was performed:
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertTrue(deploymentIds.contains(deployment2.getId()));
  }

  @Test
  public void testRedeployProcessApplicationDeploymentResumePreviousVersions() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true));

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true));

    // when
    ProcessApplicationDeployment deployment3 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .resumePreviousVersions()
        .addDeploymentResourceById(deployment1.getId(), resource1.getId()));

    // then
    // old deployments was resumed
    ProcessApplicationRegistration registration = deployment3.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentName() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();

    // first deployment
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);
    ProcessApplicationDeployment deployment1 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true));

    Resource resource1 = getResourceByName(deployment1.getId(), RESOURCE_NAME);

    // second deployment
    model = createProcessWithUserTask(PROCESS_KEY);
    testRule.deploy(repositoryService.createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true));

    // when
    ProcessApplicationDeployment deployment3 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(DEPLOYMENT_NAME)
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addDeploymentResourceById(deployment1.getId(), resource1.getId()));

    // then
    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment3.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());
  }

  // helper ///////////////////////////////////////////////////////////

  protected void verifyQueryResults(Query<?, ?> query, int countExpected) {
    assertEquals(countExpected, query.count());
  }

  protected Resource getResourceByName(String deploymentId, String resourceName) {
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    for (Resource resource : resources) {
      if (resource.getName().equals(resourceName)) {
        return resource;
      }
    }

    return null;
  }

  protected BpmnModelInstance createProcessWithServiceTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithUserTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .userTask()
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithReceiveTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .receiveTask()
      .endEvent()
    .done();
  }

  protected BpmnModelInstance createProcessWithScriptTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .scriptTask()
        .scriptFormat("javascript")
        .scriptText("return true")
      .userTask()
      .endEvent()
    .done();
  }

}
