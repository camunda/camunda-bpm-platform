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
package org.camunda.bpm.identity.impl.ldap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.identity.ldap.util.LdapTestEnvironmentRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

@Deployment
public class ProcessDefinitionQueryWithCustomIdentityProviderTest {

  @ClassRule
  public static LdapTestEnvironmentRule ldapRule = new LdapTestEnvironmentRule();
  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  RepositoryService repositoryService;

  @Before
  public void setup() {
    repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void test_shouldFindAllProcessesForCandidateUser() {
    // given

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("oscar").list();

    // then
    assertThat(processDefinitions).hasSize(2);
    assertThat(processDefinitions.get(0).getKey()).isEqualTo("process1");
    assertThat(processDefinitions.get(1).getKey()).isEqualTo("process2");
  }

  @Test
  public void test_shouldFindNoProcessesForCandidateUser() {
    // given

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("pepe").list();
    // then
    assertThat(processDefinitions).hasSize(0);
  }

  @Test
  public void test_shouldFindAllProcessesForUserInCandidateGroup() {
    // given

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("david(IT)").list();

    // then
    assertThat(processDefinitions).hasSize(1);
    assertThat(processDefinitions.get(0).getKey()).isEqualTo("process3");
  }

  @Test
  public void test_shouldFindAllProcessesForCandidateUserInCandidateGroup() {
    // given

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("monster").list();

    // then
    assertThat(processDefinitions).hasSize(2);
    assertThat(processDefinitions.get(0).getKey()).isEqualTo("process1");
    assertThat(processDefinitions.get(1).getKey()).isEqualTo("process3");
  }

  @Test
  public void test_shouldFindNoDuplicateProcessesForCandidateUserInCandidateGroup() {
    // given

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("fozzie").list();

    // then
    assertThat(processDefinitions).hasSize(1);
    assertThat(processDefinitions.get(0).getKey()).isEqualTo("process3");
  }
}
