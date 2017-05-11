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

package org.camunda.bpm.engine.test.api.repository;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;


/**
 * @author Tom Baeyens
 * @author Ingo Richtsmeier
 */
public class DeploymentQueryTest extends PluggableProcessEngineTestCase {

  private String deploymentOneId;
  private String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
      .createDeployment()
      .name("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
      .source(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE)
      .deploy()
      .getId();

    deploymentTwoId = repositoryService
      .createDeployment()
      .name("org/camunda/bpm/engine/test/repository/two_.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/repository/two.bpmn20.xml")
      .deploy()
      .getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentOneId, true);
    repositoryService.deleteDeployment(deploymentTwoId, true);
  }

  public void testQueryNoCriteria() {
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId(deploymentOneId);
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidDeploymentId() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentId("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      repositoryService.createDeploymentQuery().deploymentId(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("org/camunda/bpm/engine/test/repository/two_.bpmn20.xml");
    assertNotNull(query.singleResult());
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidName() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      repositoryService.createDeploymentQuery().deploymentName(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("%camunda%");
    assertEquals(2, query.list().size());
    assertEquals(2, query.count());

    query = repositoryService.createDeploymentQuery().deploymentNameLike("%two\\_%");
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertEquals("org/camunda/bpm/engine/test/repository/two_.bpmn20.xml", query.singleResult().getName());
  }

  public void testQueryByInvalidNameLike() {
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentNameLike("invalid");
    assertNull(query.singleResult());
    assertEquals(0, query.list().size());
    assertEquals(0, query.count());

    try {
      repositoryService.createDeploymentQuery().deploymentNameLike(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryByDeploymentBefore() throws Exception {
    Date later = DateTimeUtil.now().plus(10 * 3600).toDate();
    Date earlier = DateTimeUtil.now().minus(10 * 3600).toDate();

    long count = repositoryService.createDeploymentQuery().deploymentBefore(later).count();
    assertEquals(2, count);

    count = repositoryService.createDeploymentQuery().deploymentBefore(earlier).count();
    assertEquals(0, count);

    try {
      repositoryService.createDeploymentQuery().deploymentBefore(null);
      fail("Exception expected");
    } catch (NullValueException e) {
      // expected
    }
  }

  public void testQueryDeploymentAfter() throws Exception {
    Date later = DateTimeUtil.now().plus(10 * 3600).toDate();
    Date earlier = DateTimeUtil.now().minus(10 * 3600).toDate();

    long count = repositoryService.createDeploymentQuery().deploymentAfter(later).count();
    assertEquals(0, count);

    count = repositoryService.createDeploymentQuery().deploymentAfter(earlier).count();
    assertEquals(2, count);

    try {
      repositoryService.createDeploymentQuery().deploymentAfter(null);
      fail("Exception expected");
    } catch (NullValueException e) {
      // expected
    }
  }

  public void testQueryBySource() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .deploymentSource(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByNullSource() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .deploymentSource(null);

    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
  }

  public void testQueryByInvalidSource() {
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .deploymentSource("invalid");

    assertEquals(0, query.list().size());
    assertEquals(0, query.count());
  }

  public void testQueryDeploymentBetween() throws Exception {
    Date later = DateTimeUtil.now().plus(10 * 3600).toDate();
    Date earlier = DateTimeUtil.now().minus(10 * 3600).toDate();

    long count = repositoryService
        .createDeploymentQuery()
        .deploymentAfter(earlier)
        .deploymentBefore(later).count();
    assertEquals(2, count);

    count = repositoryService
      .createDeploymentQuery()
      .deploymentAfter(later)
      .deploymentBefore(later)
      .count();
    assertEquals(0, count);

    count = repositoryService
      .createDeploymentQuery()
      .deploymentAfter(earlier)
      .deploymentBefore(earlier)
      .count();
    assertEquals(0, count);

    count = repositoryService
        .createDeploymentQuery()
        .deploymentAfter(later)
        .deploymentBefore(earlier)
        .count();
    assertEquals(0, count);
  }

  public void testVerifyDeploymentProperties() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
      .orderByDeploymentName()
      .asc()
      .list();

    Deployment deploymentOne = deployments.get(0);
    assertEquals("org/camunda/bpm/engine/test/repository/one.bpmn20.xml", deploymentOne.getName());
    assertEquals(deploymentOneId, deploymentOne.getId());
    assertEquals(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE, deploymentOne.getSource());
    assertNull(deploymentOne.getTenantId());

    Deployment deploymentTwo = deployments.get(1);
    assertEquals("org/camunda/bpm/engine/test/repository/two_.bpmn20.xml", deploymentTwo.getName());
    assertEquals(deploymentTwoId, deploymentTwo.getId());
    assertNull(deploymentTwo.getSource());
    assertNull(deploymentTwo.getTenantId());
  }

  public void testQuerySorting() {
    assertEquals(2, repositoryService.createDeploymentQuery()
        .orderByDeploymentName()
        .asc()
        .list()
        .size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderByDeploymentId()
      .asc()
      .list()
      .size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderByDeploymenTime()
      .asc()
      .list()
      .size());

    assertEquals(2, repositoryService.createDeploymentQuery()
      .orderByDeploymentTime()
      .asc()
      .list()
      .size());
  }

}
