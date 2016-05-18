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
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryAuthorizationTest extends AuthorizationTest {

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

  public void testQueryWithoutAuthorization() {
    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 1);
    assertEquals(instance1Id, query.list().get(0).getProcessInstanceId());
  }

  public void testQueryWithReadOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithReadInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 1);
    assertEquals(instance1Id, query.list().get(0).getProcessInstanceId());
  }

  public void testQueryWithReadInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithReadInstanceWithMultiple() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    ExternalTaskQuery query = externalTaskService.createExternalTaskQuery();

    // then
    verifyQueryResults(query, 2);
  }
}
