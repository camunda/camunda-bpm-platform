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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;


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
      .deploy()
      .getId();

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
    ClockUtil.setCurrentTime(sdf.parse("01/01/2001 01:01:01.000"));
    deploymentTwoId = repositoryService
      .createDeployment()
      .name("org/camunda/bpm/engine/test/repository/two.bpmn20.xml")
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
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentName("org/camunda/bpm/engine/test/repository/two.bpmn20.xml");
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
    
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
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
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    Date before = sdf.parse("03/02/2002 02:02:02.000");
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentBefore(before);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertTrue("wrong deployment found", query.singleResult().getName().contains("two"));
    
    try {
      repositoryService.createDeploymentQuery().deploymentBefore(null);
      fail();
    } catch (ProcessEngineException e) {}
  }
  
  public void testQueryDeploymentAfter() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    Date after = sdf.parse("03/02/2002 02:02:02.000");
    DeploymentQuery query = repositoryService.createDeploymentQuery().deploymentAfter(after);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertTrue("wrong deployment found", query.singleResult().getName().contains("one"));
    
    try {
      repositoryService.createDeploymentQuery().deploymentAfter(null);
      fail();
    } catch (ProcessEngineException e) {}
  }

  public void testQueryDeploymentBetween() throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    Date after = sdf.parse("03/02/2000 02:02:02.000");
    Date before = sdf.parse("05/05/2005 05:05:05.000");
    DeploymentQuery query = repositoryService
        .createDeploymentQuery()
        .deploymentAfter(after)
        .deploymentBefore(before);
    assertEquals(1, query.list().size());
    assertEquals(1, query.count());
    assertTrue("wrong deployment found", query.singleResult().getName().contains("two"));
  }

  public void testVerifyDeploymentProperties() {
    List<Deployment> deployments = repositoryService.createDeploymentQuery()
      .orderByDeploymentName()
      .asc()
      .list();
    
    Deployment deploymentOne = deployments.get(0);
    assertEquals("org/camunda/bpm/engine/test/repository/one.bpmn20.xml", deploymentOne.getName());
    assertEquals(deploymentOneId, deploymentOne.getId());

    Deployment deploymentTwo = deployments.get(1);
    assertEquals("org/camunda/bpm/engine/test/repository/two.bpmn20.xml", deploymentTwo.getName());
    assertEquals(deploymentTwoId, deploymentTwo.getId());
    
    deployments = repositoryService.createDeploymentQuery()
      .deploymentNameLike("%one%")
       .orderByDeploymentName()
      .asc()
      .list();
    
    assertEquals("org/camunda/bpm/engine/test/repository/one.bpmn20.xml", deployments.get(0).getName());
    assertEquals(1, deployments.size());

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

  }

}
