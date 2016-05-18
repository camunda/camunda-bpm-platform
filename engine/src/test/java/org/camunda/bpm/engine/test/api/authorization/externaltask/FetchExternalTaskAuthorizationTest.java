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
package org.camunda.bpm.engine.test.api.authorization.externaltask;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.util.List;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Thorben Lindhauer
 *
 */
public class FetchExternalTaskAuthorizationTest extends AuthorizationTest {

  public static final String WORKER_ID = "workerId";
  public static final long LOCK_TIME = 10000L;

  protected String deploymentId;

  protected String instance1Id;
  protected String instance2Id;

  @Override
  protected void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/externaltask/twoExternalTaskProcess.bpmn20.xml").getId();

    instance1Id = startProcessInstanceByKey("oneExternalTaskProcess").getId();
    instance2Id = startProcessInstanceByKey("twoExternalTaskProcess").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testFetchWithoutAuthorization() {
    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  public void testFetchWithReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  public void testFetchWithUpdateOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  public void testFetchWithReadAndUpdateOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ, UPDATE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(1, tasks.size());
    assertEquals(instance1Id, tasks.get(0).getProcessInstanceId());
  }

  public void testFetchWithReadInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  public void testFetchWithUpdateInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, UPDATE_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(0, tasks.size());
  }

  public void testFetchWithReadAndUpdateInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE, UPDATE_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(1, tasks.size());
    assertEquals(instance1Id, tasks.get(0).getProcessInstanceId());
  }

  public void testFetchWithReadOnProcessInstanceAndUpdateInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, UPDATE_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(1, tasks.size());
    assertEquals(instance1Id, tasks.get(0).getProcessInstanceId());
  }

  public void testFetchWithUpdateOnProcessInstanceAndReadInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(1, tasks.size());
    assertEquals(instance1Id, tasks.get(0).getProcessInstanceId());
  }

  public void testFetchWithReadAndUpdateOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ, UPDATE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(2, tasks.size());
  }

  public void testQueryWithReadAndUpdateInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE, UPDATE_INSTANCE);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(2, tasks.size());
  }

  public void testQueryWithReadProcessInstanceAndUpdateInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(5, WORKER_ID)
      .topic("externalTaskTopic", LOCK_TIME)
      .execute();

    // then
    assertEquals(1, tasks.size());
    assertEquals(instance1Id, tasks.get(0).getProcessInstanceId());
  }

}
