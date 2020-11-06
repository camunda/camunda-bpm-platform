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
package org.camunda.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate;
import org.camunda.bpm.engine.test.api.delegate.AssertingJavaDelegate.DelegateExecutionAsserter;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MultiTenancyJobExecutorTest {

  protected static final String TENANT_ID = "tenant1";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Test
  public void setAuthenticatedTenantForTimerStartEvent() {
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess("process")
        .startEvent()
          .timerWithDuration("PT1M")
        .serviceTask()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .userTask()
        .endEvent()
      .done());

    AssertingJavaDelegate.addAsserts(hasAuthenticatedTenantId(TENANT_ID));

    ClockUtil.setCurrentTime(tomorrow());
    testRule.waitForJobExecutorToProcessAllJobs();

    assertThat(engineRule.getTaskService().createTaskQuery().count()).isEqualTo(1L);
  }

  @Test
  public void setAuthenticatedTenantForIntermediateTimerEvent() {
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess("process")
        .startEvent()
        .intermediateCatchEvent()
          .timerWithDuration("PT1M")
        .serviceTask()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .endEvent()
      .done());

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    AssertingJavaDelegate.addAsserts(hasAuthenticatedTenantId(TENANT_ID));

    ClockUtil.setCurrentTime(tomorrow());
    testRule.waitForJobExecutorToProcessAllJobs();
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void setAuthenticatedTenantForAsyncJob() {
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaAsyncBefore()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .endEvent()
      .done());

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    AssertingJavaDelegate.addAsserts(hasAuthenticatedTenantId(TENANT_ID));

    testRule.waitForJobExecutorToProcessAllJobs();
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void dontSetAuthenticatedTenantForJobWithoutTenant() {
    testRule.deploy(Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaAsyncBefore()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .endEvent()
      .done());

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    AssertingJavaDelegate.addAsserts(hasNoAuthenticatedTenantId());

    testRule.waitForJobExecutorToProcessAllJobs();
    testRule.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void dontSetAuthenticatedTenantWhileManualJobExecution() {
    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask()
          .camundaAsyncBefore()
          .camundaClass(AssertingJavaDelegate.class.getName())
        .endEvent()
      .done());

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("process");

    AssertingJavaDelegate.addAsserts(hasNoAuthenticatedTenantId());

    testRule.executeAvailableJobs();
    testRule.assertProcessEnded(processInstance.getId());
  }

  protected static DelegateExecutionAsserter hasAuthenticatedTenantId(final String expectedTenantId) {
    return new DelegateExecutionAsserter() {

      @Override
      public void doAssert(DelegateExecution execution) {
        IdentityService identityService = execution.getProcessEngineServices().getIdentityService();

        Authentication currentAuthentication = identityService.getCurrentAuthentication();
        assertThat(currentAuthentication).isNotNull();
        assertThat(currentAuthentication.getTenantIds()).contains(expectedTenantId);
      }
    };
  }

  protected static DelegateExecutionAsserter hasNoAuthenticatedTenantId() {
    return new DelegateExecutionAsserter() {

      @Override
      public void doAssert(DelegateExecution execution) {
        IdentityService identityService = execution.getProcessEngineServices().getIdentityService();

        Authentication currentAuthentication = identityService.getCurrentAuthentication();
        assertThat(currentAuthentication).isNull();
      }
    };
  }

  protected Date tomorrow() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, 1);

    return calendar.getTime();
  }

  @After
  public void tearDown() throws Exception {
    AssertingJavaDelegate.clear();
  }

}
