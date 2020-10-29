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

import java.util.List;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.Deployment;

@Deployment
public class ProcessDefinitionQueryWithCustomIdentityProviderTest extends LdapIdentityProviderTest{

  public void test_shouldFindAllProcessesForCandidateUser() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("oscar").list();

    assertEquals(2, processDefinitions.size());
    assertEquals("process1", processDefinitions.get(0).getKey());
    assertEquals("process2", processDefinitions.get(1).getKey());
  }

  public void test_shouldFindNoProcessesForCandidateUser() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("pepe").list();
    assertEquals(0, processDefinitions.size());
  }

  public void test_shouldFindAllProcessesForUserInCandidateGroup() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("david(IT)").list();
    assertEquals(1, processDefinitions.size());
    assertEquals("process3", processDefinitions.get(0).getKey());
  }

  public void test_shouldFindAllProcessesForCandidateUserInCandidateGroup() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("monster").list();
    assertEquals(2, processDefinitions.size());
    assertEquals("process1", processDefinitions.get(0).getKey());
    assertEquals("process3", processDefinitions.get(1).getKey());
  }

  public void test_shouldFindNoDuplicateProcessesForCandidateUserInCandidateGroup() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startableByUser("fozzie").list();
    assertEquals(1, processDefinitions.size());
    assertEquals("process3", processDefinitions.get(0).getKey());
  }
}
