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
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class EventSubscriptionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String SIGNAL_BOUNDARY_PROCESS_KEY = "signalBoundaryProcess";

  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneMessageBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/authorization/signalBoundaryEventProcess.bpmn20.xml").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 1);

    EventSubscription eventSubscription = query.singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processInstanceId, eventSubscription.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 1);

    EventSubscription eventSubscription = query.singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processInstanceId, eventSubscription.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 1);

    EventSubscription eventSubscription = query.singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processInstanceId, eventSubscription.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 1);

    EventSubscription eventSubscription = query.singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processInstanceId, eventSubscription.getProcessInstanceId());
  }

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 1);

    EventSubscription eventSubscription = query.singleResult();
    assertNotNull(eventSubscription);
    assertEquals(processInstanceId, eventSubscription.getProcessInstanceId());
  }

  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 7);
  }

  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    EventSubscriptionQuery query = runtimeService.createEventSubscriptionQuery();

    // then
    verifyQueryResults(query, 7);
  }

  protected void verifyQueryResults(EventSubscriptionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
