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
package org.camunda.bpm.engine.spring.test.autodeployment;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.test.PvmTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SpringAutoDeployTest extends PvmTestCase {

  protected static final String CTX_PATH
    = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployTest-context.xml";
  protected static final String CTX_CREATE_DROP_CLEAN_DB
    = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployTest-create-drop-clean-db-context.xml";
  protected static final String CTX_DYNAMIC_DEPLOY_PATH
  = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployTest-dynamic-deployment-context.xml";

  protected static final String CTX_CMMN_PATH
    = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployCmmnTest-context.xml";

  protected static final String CTX_CMMN_BPMN_TOGETHER_PATH
      = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployCmmnBpmnTest-context.xml";

  protected static final String CTX_DEPLOY_CHANGE_ONLY_PATH
      = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployDeployChangeOnlyTest-context.xml";

  protected static final String CTX_TENANT_ID_PATH
      = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployTenantIdTest-context.xml";

  protected static final String CTX_CUSTOM_NAME_PATH
      = "org/camunda/bpm/engine/spring/test/autodeployment/SpringAutoDeployCustomNameTest-context.xml";


  protected ClassPathXmlApplicationContext applicationContext;
  protected RepositoryService repositoryService;

  protected void createAppContext(String path) {
    this.applicationContext = new ClassPathXmlApplicationContext(path);
    this.repositoryService = applicationContext.getBean(RepositoryService.class);
  }

  protected void tearDown() throws Exception {
    DynamicResourceProducer.clearResources();
    removeAllDeployments();
    this.applicationContext.close();
    this.applicationContext = null;
    this.repositoryService = null;
    super.tearDown();
  }

  public void testBasicActivitiSpringIntegration() {
    createAppContext(CTX_PATH);
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();

    Set<String> processDefinitionKeys = new HashSet<>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processDefinitionKeys.add(processDefinition.getKey());
    }

    Set<String> expectedProcessDefinitionKeys = new HashSet<>();
    expectedProcessDefinitionKeys.add("a");
    expectedProcessDefinitionKeys.add("b");
    expectedProcessDefinitionKeys.add("c");

    assertEquals(expectedProcessDefinitionKeys, processDefinitionKeys);
  }

  public void testNoRedeploymentForSpringContainerRestart() throws Exception {
    createAppContext(CTX_PATH);
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertEquals(1, deploymentQuery.count());
    ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
    assertEquals(3, processDefinitionQuery.count());

    // Creating a new app context with same resources doesn't lead to more deployments
    ((AbstractXmlApplicationContext) applicationContext).destroy();
    applicationContext = new ClassPathXmlApplicationContext(CTX_PATH);
    assertEquals(1, deploymentQuery.count());
    assertEquals(3, processDefinitionQuery.count());
  }

  public void testAutoDeployCmmn() {
    createAppContext(CTX_CMMN_PATH);

    List<CaseDefinition> definitions = repositoryService.createCaseDefinitionQuery().list();

    assertEquals(1, definitions.size());
  }

  public void testAutoDeployCmmnAndBpmnTogether() {
    createAppContext(CTX_CMMN_BPMN_TOGETHER_PATH);

    long caseDefs = repositoryService.createCaseDefinitionQuery().count();
    long procDefs = repositoryService.createProcessDefinitionQuery().count();

    assertEquals(1, caseDefs);
    assertEquals(3, procDefs);
  }

  // when deployChangeOnly=true, new deployment should be created only for the changed resources
  public void testDeployChangeOnly() throws Exception {
    // given
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("model1").startEvent("oldId").endEvent().done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("model1").startEvent("newId").endEvent().done();
    BpmnModelInstance model3 = Bpmn.createExecutableProcess("model2").startEvent().endEvent().done();

    DynamicResourceProducer.addResource("a.bpmn", model1);
    DynamicResourceProducer.addResource("b.bpmn", model3);

    createAppContext(CTX_DEPLOY_CHANGE_ONLY_PATH);

    // assume
    assertEquals(1, repositoryService.createDeploymentQuery().count());

    // when
    ((AbstractXmlApplicationContext) applicationContext).destroy();

    DynamicResourceProducer.clearResources();
    DynamicResourceProducer.addResource("a.bpmn", model2);
    DynamicResourceProducer.addResource("b.bpmn", model3);

    applicationContext = new ClassPathXmlApplicationContext(CTX_DEPLOY_CHANGE_ONLY_PATH);
    repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");

    // then
    assertEquals(2, repositoryService.createDeploymentQuery().count());
    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());
  }

  // Updating the bpmn20 file should lead to a new deployment when restarting the Spring container
  public void testResourceRedeploymentAfterProcessDefinitionChange() throws Exception {
    // given
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("model1").startEvent("oldId").endEvent().done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("model1").startEvent("newId").endEvent().done();
    BpmnModelInstance model3 = Bpmn.createExecutableProcess("model2").startEvent().endEvent().done();

    DynamicResourceProducer.addResource("a.bpmn", model1);
    DynamicResourceProducer.addResource("b.bpmn", model3);

    createAppContext(CTX_DYNAMIC_DEPLOY_PATH);
    assertEquals(1, repositoryService.createDeploymentQuery().count());
    ((AbstractXmlApplicationContext)applicationContext).destroy();

    // when
    DynamicResourceProducer.clearResources();
    DynamicResourceProducer.addResource("a.bpmn", model2);
    DynamicResourceProducer.addResource("b.bpmn", model3);

    applicationContext = new ClassPathXmlApplicationContext(CTX_DYNAMIC_DEPLOY_PATH);
    repositoryService = (RepositoryService) applicationContext.getBean("repositoryService");

    // then
    // Assertions come AFTER the file write! Otherwise the process file is messed up if the assertions fail.
    assertEquals(2, repositoryService.createDeploymentQuery().count());
    assertEquals(4, repositoryService.createProcessDefinitionQuery().count());
  }

  public void testAutoDeployWithCreateDropOnCleanDb() {
    createAppContext(CTX_CREATE_DROP_CLEAN_DB);
    assertEquals(1, repositoryService.createDeploymentQuery().count());
    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());
  }

  public void testAutoDeployTenantId() {
    createAppContext(CTX_TENANT_ID_PATH);

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.tenantIdIn("tenant1").count());
  }

  public void testAutoDeployWithoutTenantId() {
    createAppContext(CTX_CMMN_BPMN_TOGETHER_PATH);

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.withoutTenantId().count());
  }

  public void testAutoDeployCustomName() {
    createAppContext(CTX_CUSTOM_NAME_PATH);

    assertEquals(1, repositoryService.createProcessDefinitionQuery().count());
  }

  // --Helper methods ----------------------------------------------------------

  private void removeAllDeployments() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}