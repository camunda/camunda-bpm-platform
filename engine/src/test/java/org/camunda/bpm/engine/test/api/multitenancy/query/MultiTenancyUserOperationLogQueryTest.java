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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyUserOperationLogQueryTest {
//
//  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
//
//  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
//
//  protected HistoryService historyService;
//  protected RuntimeService runtimeService;
//  protected RepositoryService repositoryService;
//  protected TaskService taskService;
//
//  @Rule
//  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
//
//  protected static final String A_USER_ID = "aUserId";
//
//  protected final static String TENANT_NULL = null;
//  protected final static String TENANT_1 = "tenant1";
//  protected final static String TENANT_2 = "tenant2";
//  protected final static String TENANT_3 = "tenant3";
//
//
//  protected ProcessInstance process;
//  @Before
//  public void init() {
//    taskService = engineRule.getTaskService();
//    repositoryService = engineRule.getRepositoryService();
//    historyService = engineRule.getHistoryService();
//    runtimeService = engineRule.getRuntimeService();
//
//    // create sample identity link
//    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
//    .startEvent()
//    .userTask("task").camundaCandidateUsers(A_USER_ID)
//    .endEvent()
//    .done();
//
//    // deploy tenants
//    testRule.deployForTenant(TENANT_NULL, oneTaskProcess);
//    testRule.deployForTenant(TENANT_1, oneTaskProcess);
//    testRule.deployForTenant(TENANT_2, oneTaskProcess);
//    testRule.deployForTenant(TENANT_3, oneTaskProcess);
//  }
//  @Test
//  public void testQueryProcessInstanceOperationsById() {
//    // given
//    process = startProcessInstanceForTenant(TENANT_1);
//
//    // when
//    runtimeService.suspendProcessInstanceById(process.getId());
//    runtimeService.activateProcessInstanceById(process.getId());
//
//    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");
//
//    // then
//    assertEquals(4, query().entityType(PROCESS_INSTANCE).count());
//    assertThat(query().tenantIdIn(TENANT_1).count()).isEqualTo(4L);
//
//  }
//  protected UserOperationLogQuery query() {
//    return historyService.createUserOperationLogQuery();
//  }
//  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
//    return runtimeService.createProcessInstanceByKey("testProcess")
//        .processDefinitionTenantId(tenant)
//        .execute();
//  }
}
