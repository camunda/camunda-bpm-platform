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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.EventSubscriptionQueryImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancySignalReceiveTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final BpmnModelInstance SIGNAL_START_PROCESS = Bpmn.createExecutableProcess("signalStart")
      .startEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();

  protected static final BpmnModelInstance SIGNAL_CATCH_PROCESS = Bpmn.createExecutableProcess("signalCatch")
      .startEvent()
      .intermediateCatchEvent()
        .signal("signal")
      .userTask()
      .endEvent()
      .done();

  private EventSubscriptionQueryImpl createEventSubscriptionQuery() {
    return new EventSubscriptionQueryImpl(processEngineConfiguration.getCommandExecutorTxRequired());
  }

  public void testSendSignalToStartEventForNonTenant() {
    deployment(SIGNAL_START_PROCESS);
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testSendSignalToStartEventForTenant() {
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testSendSignalToStartEventWithoutTenantIdForNonTenant() {
    deployment(SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count(), is(1L));
  }

  public void testSendSignalToStartEventWithoutTenantIdForTenant() {
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS);

    runtimeService.createSignalEvent("signal").send();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSendSignalToIntermediateCatchEventForNonTenant() {
    deployment(SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));
  }

  public void testSendSignalToIntermediateCatchEventForTenant() {
    deploymentForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testSendSignalToIntermediateCatchEventWithoutTenantIdForNonTenant() {
    deployment(SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.count(), is(1L));
  }

  public void testSendSignalToIntermediateCatchEventWithoutTenantIdForTenant() {
    deploymentForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    runtimeService.createSignalEvent("signal").send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testSendSignalToStartAndIntermediateCatchEventForNonTenant() {
    deployment(SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();

    runtimeService.createSignalEvent("signal").withoutTenantId().send();

    List<Task> tasks = taskService.createTaskQuery().list();
    assertThat(tasks.size(), is(2));
    assertThat(tasks.get(0).getTenantId(), is(nullValue()));
    assertThat(tasks.get(1).getTenantId(), is(nullValue()));
  }

  public void testSendSignalToStartAndIntermediateCatchEventForTenant() {
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_START_PROCESS, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    runtimeService.createSignalEvent("signal").tenantId(TENANT_ONE).send();
    runtimeService.createSignalEvent("signal").tenantId(TENANT_TWO).send();

    TaskQuery query = taskService.createTaskQuery();
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
  }

  public void testFailToSendSignalToStartEventForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, SIGNAL_START_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    try {
      runtimeService.createSignalEvent("signal").send();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot deliver a signal with name 'signal' to multiple tenants."));
    }
  }

  public void testFailToSendSignalToIntermediateCatchEventForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_CATCH_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("signalCatch").processDefinitionTenantId(TENANT_TWO).execute();

    try {
      runtimeService.createSignalEvent("signal").send();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot deliver a signal with name 'signal' to multiple tenants."));
    }
  }

  public void testFailToSendSignalToStartAndIntermediateCatchEventForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, SIGNAL_CATCH_PROCESS);
    deploymentForTenant(TENANT_TWO, SIGNAL_START_PROCESS);

    runtimeService.createProcessInstanceByKey("signalCatch").execute();

    try {
      runtimeService.createSignalEvent("signal").send();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot deliver a signal with name 'signal' to multiple tenants."));
    }
  }

  public void testFailToSendSignalWithExecutionIdForTenant() {

    try {
      runtimeService.createSignalEvent("signal").executionId("id").tenantId(TENANT_ONE).send();

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id when deliver a signal to a single execution."));
    }
  }

  public void testIntermediateSignalThrowTenantXCatchTenantX(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testIntermediateSignalThrowTenantXCatchTenantNull(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testIntermediateSignalThrowTenantXCatchTenantY(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantY=2
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId("2").deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testIntermediateSignalThrowTenantNullCatchTenantNull(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testIntermediateSignalThrowTenantNullCatchTenantX(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testIntermediateSignalThrowTenantNullCatchTenantY(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.catchAlertSignal.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantY=2
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/signal/SignalEventTests.throwAlertSignal.bpmn20.xml").
        tenantId("2").deploy();

    runtimeService.startProcessInstanceByKey("throwSignal");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantXCatchTenantX(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantXCatchTenantNull(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantXCatchTenantY(){

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantY=2
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId("2").deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantNullCatchTenantNull(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(0, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantNullCatchTenantX(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantX=1
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId("1").deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }

  public void testEndSignalThrowTenantNullCatchTenantY(){

    // TenantNull=null
    org.camunda.bpm.engine.repository.Deployment deployment1 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.catchSignalEndEvent.bpmn20.xml").
        tenantId(null).deploy();

    runtimeService.startProcessInstanceByKey("catchSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    // TenantY=2
    org.camunda.bpm.engine.repository.Deployment deployment2 = repositoryService.createDeployment().
        addClasspathResource("org/camunda/bpm/engine/test/bpmn/event/end/SignalEndEventTest.processWithSignalEndEvent.bpmn20.xml").
        tenantId("2").deploy();

    runtimeService.startProcessInstanceByKey("processWithSignalEndEvent");

    assertEquals(1, createEventSubscriptionQuery().count());
    assertEquals(1, runtimeService.createProcessInstanceQuery().count());

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);

  }


}
