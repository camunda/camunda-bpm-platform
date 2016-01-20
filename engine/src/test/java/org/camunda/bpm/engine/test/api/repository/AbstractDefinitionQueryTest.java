/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.query.Query;

public abstract class AbstractDefinitionQueryTest extends PluggableProcessEngineTestCase {

  protected String deploymentOneId;
  protected String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .name("firstDeployment")
      .addClasspathResource(getResourceOnePath())
      .addClasspathResource(getResourceTwoPath())
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .name("secondDeployment")
      .addClasspathResource(getResourceOnePath())
      .deploy()
      .getId();

    super.setUp();
  }

  protected abstract String getResourceOnePath();

  protected abstract String getResourceTwoPath();

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  protected void verifyQueryResults(Query query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  private void verifySingleResultFails(Query query) {
    try {
      query.singleResult();
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      // expected exception
    }
  }
}
