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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.List;


/**
 * @author Joram Barrez
 */
public class ProcessDefinitionQueryTest extends AbstractDefinitionQueryTest {

  private String deploymentThreeId;

  protected String getResourceOnePath() {
    return "org/camunda/bpm/engine/test/repository/one.bpmn20.xml";
  }

  protected String getResourceTwoPath() {
    return "org/camunda/bpm/engine/test/repository/two.bpmn20.xml";
  }

  protected String getResourceThreePath() {
    return "org/camunda/bpm/engine/test/repository/three_.bpmn20.xml";
  }

  @Override
  protected void setUp() throws Exception {
    deploymentThreeId = repositoryService.createDeployment().name("thirdDeployment").addClasspathResource(getResourceThreePath()).deploy().getId();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentThreeId, true);
  }

  public void testProcessDefinitionProperties() {
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .orderByProcessDefinitionName().asc()
      .orderByProcessDefinitionVersion().asc()
      .orderByProcessDefinitionCategory().asc()
      .list();

    ProcessDefinition processDefinition = processDefinitions.get(0);
    assertEquals("one", processDefinition.getKey());
    assertEquals("One", processDefinition.getName());
    assertEquals("Desc one", processDefinition.getDescription());
    assertTrue(processDefinition.getId().startsWith("one:1"));
    assertEquals("Examples", processDefinition.getCategory());

    processDefinition = processDefinitions.get(1);
    assertEquals("one", processDefinition.getKey());
    assertEquals("One", processDefinition.getName());
    assertEquals("Desc one", processDefinition.getDescription());
    assertTrue(processDefinition.getId().startsWith("one:2"));
    assertEquals("Examples", processDefinition.getCategory());

    processDefinition = processDefinitions.get(2);
    assertEquals("two", processDefinition.getKey());
    assertEquals("Two", processDefinition.getName());
    assertNull(processDefinition.getDescription());
    assertTrue(processDefinition.getId().startsWith("two:1"));
    assertEquals("Examples2", processDefinition.getCategory());
  }

  public void testQueryByDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentOneId);
    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidDeploymentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().deploymentId("invalid");
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().deploymentId(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("Two");
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionName("One");
    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidName() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("invalid");
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionName(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%w%");
    verifyQueryResults(query, 1);
    query = query.processDefinitionNameLike("%z\\_%");
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionNameLike("%invalid%");
    verifyQueryResults(query, 0);
  }

  /**
   * CAM-8014
   *
   * Verify that search by name like returns results with case-insensitive
   */
  public void testQueryByNameLikeCaseInsensitive() {
    ProcessDefinitionQuery queryCaseInsensitive = repositoryService.createProcessDefinitionQuery()
      .processDefinitionNameLike("%OnE%");
    verifyQueryResults(queryCaseInsensitive, 2);
  }

  public void testQueryByKey() {
    // process one
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one");
    verifyQueryResults(query, 2);

    // process two
    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two");
    verifyQueryResults(query, 1);
  }

  public void testQueryByKeys() {

    // empty list
    assertTrue(repositoryService.createProcessDefinitionQuery().processDefinitionKeysIn("a", "b").list().isEmpty());


    // collect all definition keys
    List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
    String[] processDefinitionKeys = new String[list.size()];
    for (int i = 0; i < processDefinitionKeys.length; i++) {
      processDefinitionKeys[i] = list.get(i).getKey();
    }

    List<ProcessDefinition> keyInList = repositoryService.createProcessDefinitionQuery().processDefinitionKeysIn(processDefinitionKeys).list();
    for (ProcessDefinition processDefinition : keyInList) {
      boolean found = false;
      for (ProcessDefinition otherProcessDefinition : list) {
        if(otherProcessDefinition.getKey().equals(processDefinition.getKey())) {
          found = true; break;
        }
      }
      if(!found) {
        fail("Expected to find process definition "+processDefinition);
      }
    }

    assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionKey("dummyKey").processDefinitionKeysIn(processDefinitionKeys).count());
  }

  public void testQueryByInvalidKey() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("invalid");
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionKey(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%o%");
    verifyQueryResults(query, 3);
    query = query.processDefinitionKeyLike("%z\\_%");
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidKeyLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike("%invalid%");
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionKeyLike(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByResourceNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%ee\\_%");
    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidResourceNameLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike("%invalid%");
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionResourceNameLike(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByCategory() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategory("Examples");
    verifyQueryResults(query, 2);
  }

  public void testQueryByCategoryLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%Example%");
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%amples2");
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionCategoryLike("%z\\_%");
    verifyQueryResults(query, 1);
  }

  public void testQueryByVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(2);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1);
    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionVersion(3);
    verifyQueryResults(query, 0);

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionVersion(-1).list();
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionVersion(null).list();
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByKeyAndVersion() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(1);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(2);
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").processDefinitionVersion(3);
    verifyQueryResults(query, 0);
  }

  public void testQueryByLatest() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().latestVersion();
    verifyQueryResults(query, 3);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("one").latestVersion();
    verifyQueryResults(query, 1);

    query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("two").latestVersion();
    verifyQueryResults(query, 1);
  }

  public void testInvalidUsageOfLatest() {
    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionId("test").latestVersion().list();
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }

    try {
      repositoryService.createProcessDefinitionQuery().processDefinitionVersion(1).latestVersion().list();
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }

    try {
      repositoryService.createProcessDefinitionQuery().deploymentId("test").latestVersion().list();
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQuerySorting() {

    // asc

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc();
    verifyQueryResults(query, 4);

    // desc

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionId().desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByDeploymentId().desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().desc();
    verifyQueryResults(query, 4);

    // Typical use case
    query = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionKey().asc().orderByProcessDefinitionVersion().desc();
    List<ProcessDefinition> processDefinitions = query.list();
    assertEquals(4, processDefinitions.size());

    assertEquals("one", processDefinitions.get(0).getKey());
    assertEquals(2, processDefinitions.get(0).getVersion());
    assertEquals("one", processDefinitions.get(1).getKey());
    assertEquals(1, processDefinitions.get(1).getVersion());
    assertEquals("two", processDefinitions.get(2).getKey());
    assertEquals(1, processDefinitions.get(2).getVersion());
  }

  public void testQueryByMessageSubscription() {
    Deployment deployment = repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processWithNewBookingMessage.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/processWithNewInvoiceMessage.bpmn20.xml")
    .deploy();

    assertEquals(1,repositoryService.createProcessDefinitionQuery()
      .messageEventSubscriptionName("newInvoiceMessage")
      .count());

    assertEquals(1,repositoryService.createProcessDefinitionQuery()
      .messageEventSubscriptionName("newBookingMessage")
      .count());

    assertEquals(0,repositoryService.createProcessDefinitionQuery()
      .messageEventSubscriptionName("bogus")
      .count());

    repositoryService.deleteDeployment(deployment.getId());
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentId() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("failingProcess")
        .count());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .incidentId(incident.getId());

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidIncidentId() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    verifyQueryResults(query.incidentId("invalid"), 0);

    try {
      query.incidentId(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentType() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("failingProcess")
        .count());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .incidentType(incident.getIncidentType());

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidIncidentType() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    verifyQueryResults(query.incidentType("invalid"), 0);

    try {
      query.incidentType(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessage() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("failingProcess")
        .count());

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    Incident incident = runtimeService.createIncidentQuery().processInstanceId(processInstance.getId()).singleResult();

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .incidentMessage(incident.getIncidentMessage());

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidIncidentMessage() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    verifyQueryResults(query.incidentMessage("invalid"), 0);

    try {
      query.incidentMessage(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByIncidentMessageLike() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("failingProcess")
        .count());

    runtimeService.startProcessInstanceByKey("failingProcess");

    executeAvailableJobs();

    List<Incident> incidentList = runtimeService.createIncidentQuery().list();
    assertEquals(1, incidentList.size());

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .incidentMessageLike("%expected%");

    verifyQueryResults(query, 1);

    query = repositoryService
        .createProcessDefinitionQuery()
        .incidentMessageLike("%\\_expected%");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidIncidentMessageLike() {
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    verifyQueryResults(query.incidentMessageLike("invalid"), 0);

    try {
      query.incidentMessageLike(null);
      fail();
    } catch (ProcessEngineException e) {
      // Expected Exception
    }
  }

  public void testQueryByProcessDefinitionIds() {

    // empty list
    assertTrue(repositoryService.createProcessDefinitionQuery().processDefinitionIdIn("a", "b").list().isEmpty());


    // collect all ids
    List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
    String[] ids = new String[list.size()];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = list.get(i).getId();
    }

    List<ProcessDefinition> idInList = repositoryService.createProcessDefinitionQuery().processDefinitionIdIn(ids).list();
    for (ProcessDefinition processDefinition : idInList) {
      boolean found = false;
      for (ProcessDefinition otherProcessDefinition : list) {
        if(otherProcessDefinition.getId().equals(processDefinition.getId())) {
          found = true; break;
        }
      }
      if(!found) {
        fail("Expected to find process definition "+processDefinition);
      }
    }

    assertEquals(0, repositoryService.createProcessDefinitionQuery().processDefinitionId("dummyId").processDefinitionIdIn(ids).count());
  }

  public void testQueryByLatestAndName() {
    String firstDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml")
        .deploy()
        .getId();

    String secondDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml")
        .deploy()
        .getId();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    query
      .processDefinitionName("First Test Process")
      .latestVersion();

    verifyQueryResults(query, 1);

    ProcessDefinition result = query.singleResult();

    assertEquals("First Test Process", result.getName());
    assertEquals(2, result.getVersion());

    repositoryService.deleteDeployment(firstDeployment, true);
    repositoryService.deleteDeployment(secondDeployment, true);

  }

  public void testQueryByLatestAndName_NotFound() {
    String firstDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml")
        .deploy()
        .getId();

    String secondDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/second-process.bpmn20.xml")
        .deploy()
        .getId();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    query
      .processDefinitionName("First Test Process")
      .latestVersion();

    verifyQueryResults(query, 0);

    repositoryService.deleteDeployment(firstDeployment, true);
    repositoryService.deleteDeployment(secondDeployment, true);

  }

  public void testQueryByLatestAndNameLike() {
    String firstDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml")
        .deploy()
        .getId();

    String secondDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/second-process.bpmn20.xml")
        .deploy()
        .getId();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    query
      .processDefinitionNameLike("%Test Process")
      .latestVersion();

    verifyQueryResults(query, 1);

    ProcessDefinition result = query.singleResult();

    assertEquals("Second Test Process", result.getName());
    assertEquals(2, result.getVersion());

    query
      .processDefinitionNameLike("%Test%")
      .latestVersion();

    verifyQueryResults(query, 1);

    result = query.singleResult();

    assertEquals("Second Test Process", result.getName());
    assertEquals(2, result.getVersion());

    query
      .processDefinitionNameLike("Second%")
      .latestVersion();

    result = query.singleResult();

    assertEquals("Second Test Process", result.getName());
    assertEquals(2, result.getVersion());

    repositoryService.deleteDeployment(firstDeployment, true);
    repositoryService.deleteDeployment(secondDeployment, true);
  }

  public void testQueryByLatestAndNameLike_NotFound() {
    String firstDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml")
        .deploy()
        .getId();

    String secondDeployment = repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/second-process.bpmn20.xml")
        .deploy()
        .getId();

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    query
      .processDefinitionNameLike("First%")
      .latestVersion();

    verifyQueryResults(query, 0);

    repositoryService.deleteDeployment(firstDeployment, true);
    repositoryService.deleteDeployment(secondDeployment, true);
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByVersionTag() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
      .versionTag("ver_tag_2")
      .count());
  }

  @org.camunda.bpm.engine.test.Deployment(resources={"org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml"})
  public void testQueryByVersionTagLike() {
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
      .versionTagLike("ver\\_tag\\_%")
      .count());
  }

  @org.camunda.bpm.engine.test.Deployment(resources={
    "org/camunda/bpm/engine/test/api/repository/failingProcessCreateOneIncident.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/repository/VersionTagTest.testParsingVersionTag.bpmn20.xml"
  })
  public void testQueryOrderByVersionTag() {
    List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
      .versionTagLike("ver%tag%")
      .orderByVersionTag()
      .asc()
      .list();

    assertEquals("ver_tag_2", processDefinitionList.get(1).getVersionTag());
  }


}
