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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ProcessDefinitionCandidateTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String CANDIDATE_STARTER_USER = "org/camunda/bpm/engine/test/api/repository/ProcessDefinitionCandidateTest.testCandidateStarterUser.bpmn20.xml";
  protected static final String CANDIDATE_STARTER_USERS = "org/camunda/bpm/engine/test/api/repository/ProcessDefinitionCandidateTest.testCandidateStarterUsers.bpmn20.xml";

  protected static final String CANDIDATE_STARTER_GROUP = "org/camunda/bpm/engine/test/api/repository/ProcessDefinitionCandidateTest.testCandidateStarterGroup.bpmn20.xml";
  protected static final String CANDIDATE_STARTER_GROUPS = "org/camunda/bpm/engine/test/api/repository/ProcessDefinitionCandidateTest.testCandidateStarterGroups.bpmn20.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;

  @Before
  public void setUp() throws Exception {
    repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void shouldPropagateTenantIdToCandidateStarterUser() {
    // when
    DeploymentBuilder builder = repositoryService.createDeployment()
      .addClasspathResource(CANDIDATE_STARTER_USER)
      .tenantId(TENANT_ONE);
    testRule.deploy(builder);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, links.size());

    IdentityLink link = links.get(0);
    assertNotNull(link.getTenantId());
    assertEquals(TENANT_ONE, link.getTenantId());
  }

  @Test
  public void shouldPropagateTenantIdToCandidateStarterUsers() {
    // when
    DeploymentBuilder builder = repositoryService.createDeployment()
      .addClasspathResource(CANDIDATE_STARTER_USERS)
      .tenantId(TENANT_ONE);
    testRule.deploy(builder);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(3, links.size());

    for (IdentityLink link : links) {
      assertNotNull(link.getTenantId());
      assertEquals(TENANT_ONE, link.getTenantId());
    }
  }

  @Test
  public void shouldPropagateTenantIdToCandidateStarterGroup() {
    // when
    DeploymentBuilder builder = repositoryService.createDeployment()
      .addClasspathResource(CANDIDATE_STARTER_GROUP)
      .tenantId(TENANT_ONE);
    testRule.deploy(builder);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(1, links.size());

    IdentityLink link = links.get(0);
    assertNotNull(link.getTenantId());
    assertEquals(TENANT_ONE, link.getTenantId());
  }

  @Test
  public void shouldPropagateTenantIdToCandidateStarterGroups() {
    // when
    DeploymentBuilder builder = repositoryService.createDeployment()
      .addClasspathResource(CANDIDATE_STARTER_GROUPS)
      .tenantId(TENANT_ONE);
    testRule.deploy(builder);

    // then
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());
    assertEquals(3, links.size());

    for (IdentityLink link : links) {
      assertNotNull(link.getTenantId());
      assertEquals(TENANT_ONE, link.getTenantId());
    }
  }
}
