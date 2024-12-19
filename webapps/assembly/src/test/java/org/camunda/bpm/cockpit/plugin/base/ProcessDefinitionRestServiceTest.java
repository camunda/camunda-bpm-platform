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
package org.camunda.bpm.cockpit.plugin.base;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionStatisticsDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.ProcessDefinitionRestService;
import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class ProcessDefinitionRestServiceTest extends AbstractCockpitPluginTest {

  private ProcessEngineConfigurationImpl processEngineConfiguration;
  private RepositoryService repositoryService;
  private RuntimeService runtimeService;
  private ProcessDefinitionRestService resource;
  private IdentityService identityService;
  private UriInfo uriInfo;
  private final MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

  @Before
  public void setUp() throws Exception {
    super.before();

    ProcessEngine processEngine = getProcessEngine();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();
    identityService = processEngine.getIdentityService();

    resource = new ProcessDefinitionRestService(processEngine.getName());

    uriInfo = Mockito.mock(UriInfo.class);
    Mockito.doReturn(queryParameters).when(uriInfo).getQueryParameters();
  }

  @After
  public void tearDown() {
    identityService.clearAuthentication();
    processEngineConfiguration.setQueryMaxResultsLimit(Integer.MAX_VALUE);
    queryParameters.clear();
    repositoryService.createDeploymentQuery()
        .list()
        .forEach(deployment -> repositoryService.deleteDeployment(deployment.getId(), true));
  }

  private void deployProcesses(int count) {
    deployProcesses(count, "ProcessDefinition", "Process Definition");
  }

  private void deployProcesses(int count, String key, String name) {
    deployProcesses(count, key, name, 0, 0, "default");
  }

  private void deployProcesses(int count, String key, String name, int instances, int incidents, String tenantId) {
    for (int i = 0; i < count; i++) {
      String pdKey = key + (count > 1 ? "-" + i : "");
      BpmnModelInstance aProcessDefinition = Bpmn.createExecutableProcess(pdKey)
          .name(name + (count > 1 ? " " + i : ""))
          .startEvent()
          .userTask("testQuerySuspensionStateTask")
          .endEvent()
          .done();
      ProcessDefinition processDefinition = repositoryService.createDeployment()
          .addModelInstance(pdKey + ".bpmn", aProcessDefinition)
          .name(pdKey)
          .tenantId(tenantId)
          .deployWithResult()
          .getDeployedProcessDefinitions()
          .get(0);

      if (instances > 0) {
        for (int j = 0; j < instances; j++) {
          runtimeService.startProcessInstanceById(processDefinition.getId());
        }
      }

      if (incidents > 0) {
        for (int k = 0; k < incidents; k++) {
          Execution execution = runtimeService.createExecutionQuery()
              .processDefinitionId(processDefinition.getId())
              .list()
              .get(0);
          runtimeService.createIncident("custom", execution.getId(), null);
        }
      }
    }
  }

  @Test
  public void shouldThrowExceptionWhenMaxResultsLimitExceeded() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);
    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryStatistics(uriInfo, 0, 11);
      fail("Exception expected!");
    } catch (BadUserRequestException e) {
      // then
      assertThat(e).hasMessage("Max results limit of 10 exceeded!");
    }
  }

  @Test
  public void shouldThrowExceptionWhenQueryUnbounded() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);
    identityService.setAuthenticatedUserId("foo");

    try {
      // when
      resource.queryStatistics(uriInfo, null, null);
      fail("Exception expected!");
    } catch (BadUserRequestException e) {
      // then
      assertThat(e).hasMessage("An unbound number of results is forbidden!");
    }
  }

  @Test
  public void shouldReturnUnboundedResultWhenNotAuthenticated() {
    // given
    processEngineConfiguration.setQueryMaxResultsLimit(10);

    try {
      // when
      resource.queryStatistics(uriInfo, null, null);
      // then: no exception expected
    } catch (BadUserRequestException e) {
      // then
      fail("No exception expected");
    }
  }

  protected void assertProcessDefinitionStatisticsDto(ProcessDefinitionStatisticsDto actual,
                                                      ProcessDefinition expected,
                                                      int expectedInstances,
                                                      int expectedIncidents) {
    assertThat(actual.getId()).isEqualTo(expected.getId());
    assertThat(actual.getKey()).isEqualTo(expected.getKey());
    assertThat(actual.getName()).isEqualTo(expected.getName());
    assertThat(actual.getVersion()).isEqualTo(expected.getVersion());
    assertThat(actual.getTenantId()).isEqualTo(expected.getTenantId());
    assertThat(actual.getSuspensionState()).isEqualTo(
        expected.isSuspended() ? SuspensionState.SUSPENDED.getStateCode() : SuspensionState.ACTIVE.getStateCode());
    assertThat(actual.getInstances()).isEqualTo(expectedInstances);
    assertThat(actual.getIncidents()).isEqualTo(expectedIncidents);
  }

  @Test
  public void shouldAggregateResultsByKeyAndTenantId() {
    // given
    deployProcesses(1, "A", "Process A", 1, 3, "default");
    repositoryService.suspendProcessDefinitionByKey("A");
    deployProcesses(1, "A", "Process A v2", 1, 0, "default");
    deployProcesses(1, "A", "Process A v3", 2, 2, "default");
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("A")
        .latestVersion()
        .singleResult();

    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, null, null);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(1);
    assertThat(actualCount.getCount()).isEqualTo(actual.size());
    assertProcessDefinitionStatisticsDto(actual.get(0), processDefinition, 4, 5);
  }

  @Test
  public void shouldNotAggregateIfTenantIdIsDifferent() {
    // given
    deployProcesses(1, "A", "Process A", 1, 3, "tenant1");
    repositoryService.suspendProcessDefinitionByKey("A");
    deployProcesses(1, "A", "Process A v2", 1, 0, "tenant1");
    deployProcesses(1, "A", "Process A v3", 2, 2, "tenant2");
    ProcessDefinition pdTenant1 = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("A")
        .tenantIdIn("tenant1")
        .latestVersion()
        .singleResult();
    ProcessDefinition pdTenant2 = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("A")
        .tenantIdIn("tenant2")
        .latestVersion()
        .singleResult();
    queryParameters.add("sortBy", "tenantId");
    queryParameters.add("sortOrder", "asc");

    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, null, null);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(2);
    assertThat(actualCount.getCount()).isEqualTo(actual.size());
    assertProcessDefinitionStatisticsDto(actual.get(0), pdTenant1, 2, 3);
    assertProcessDefinitionStatisticsDto(actual.get(1), pdTenant2, 2, 2);
  }

  protected void verifyPaginatedResult(int firstResult, int maxResults, int expectedResults) {
    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, firstResult, maxResults);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(expectedResults);
    assertThat(actualCount.getCount()).isEqualTo(10);
    for (int i = 0; i < expectedResults; i++) {
      assertThat(actual.get(i).getKey()).isEqualTo("ProcessDefinition-" + (i + firstResult));
      assertThat(actual.get(i).getName()).isEqualTo("Process Definition " + (i + firstResult));
    }
  }

  @Test
  public void shouldReturnPaginatedResult() {
    // given
    deployProcesses(10);
    queryParameters.add("sortBy", "key");
    queryParameters.add("sortOrder", "asc");

    // verify pagination
    verifyPaginatedResult(0, 2, 2);
    verifyPaginatedResult(2, 2, 2);
    verifyPaginatedResult(2, 10, 8);
    verifyPaginatedResult(8, 10, 2);
  }

  protected void verifyFilteredResult(String filterBy, String key, String name) {
    // given
    queryParameters.clear();
    queryParameters.add(filterBy, "key".equals(filterBy) ? key : name);

    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, null, null);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(1);
    assertThat(actualCount.getCount()).isEqualTo(actual.size());
    assertThat(actual.get(0).getKey()).isEqualTo(key);
    assertThat(actual.get(0).getName()).isEqualTo(name);
  }

  @Test
  public void shouldFilterResultByKey() {
    // given
    deployProcesses(2);
    deployProcesses(2, "Something", "Descriptive Name");

    // verify filter by key
    verifyFilteredResult("key", "ProcessDefinition-0", "Process Definition 0");
    verifyFilteredResult("key", "Something-1", "Descriptive Name 1");
  }

  @Test
  public void shouldFilterResultByName() {
    // given
    deployProcesses(2);
    deployProcesses(2, "Something", "Descriptive Name");

    // verify filter by name
    verifyFilteredResult("name", "ProcessDefinition-0", "Process Definition 0");
    verifyFilteredResult("name", "Something-1", "Descriptive Name 1");
  }

  private void verifyFilteredLikeResult(String filterBy, String filterValue, int expectedResults) {
    // given
    queryParameters.clear();
    queryParameters.add(filterBy, filterValue);

    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, null, null);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(expectedResults);
    assertThat(actualCount.getCount()).isEqualTo(actual.size());
  }

  @Test
  public void shouldFilterResultByLike() {
    // given
    deployProcesses(2);
    deployProcesses(2, "AwesomeProcess", "Descriptive Process Name");

    // verify filter by keyLike
    verifyFilteredLikeResult("keyLike", "%some%", 2);
    verifyFilteredLikeResult("keyLike", "Proc%", 2);
    verifyFilteredLikeResult("keyLike", "%-1", 2);
    verifyFilteredLikeResult("keyLike", "%Process%", 4);
  }

  @Test
  public void shouldFilterResultByNameLike() {
    // given
    deployProcesses(2);
    deployProcesses(2, "AwesomeProcess", "Descriptive Process Name");

    // verify filter by nameLike
    verifyFilteredLikeResult("nameLike", "%Definition%", 2);
    verifyFilteredLikeResult("nameLike", "Descriptive%", 2);
    verifyFilteredLikeResult("nameLike", "% 1", 2);
    verifyFilteredLikeResult("nameLike", "%Process%", 4);
  }

  private void verifyOrderedResult(String sortBy, String sortOrder, List<String> expectedKeysOrder) {
    // given
    queryParameters.clear();
    queryParameters.add("sortBy", sortBy);
    queryParameters.add("sortOrder", sortOrder);

    // when
    List<ProcessDefinitionStatisticsDto> actual = resource.queryStatistics(uriInfo, null, null);
    CountResultDto actualCount = resource.getStatisticsCount(uriInfo);

    // then
    assertThat(actual).hasSize(4);
    assertThat(actualCount.getCount()).isEqualTo(actual.size());
    for (int i = 0; i < expectedKeysOrder.size(); i++) {
      String expectedKey = expectedKeysOrder.get(i);
      assertThat(actual.get(i).getKey()).isEqualTo(expectedKey);
    }
  }

  @Test
  public void shouldSortResult() {
    // given
    deployProcesses(1, "A", "Process A", 1, 3, "tenant2");
    deployProcesses(1, "B", "Process B", 3, 1, "tenant0");
    deployProcesses(1, "p1", "Process 1", 2, 2, "tenant3");
    deployProcesses(1, "p2", "Process 2", 0, 0, "tenant1");

    // verify order by key
    verifyOrderedResult("key", "asc", Arrays.asList("A", "B", "p1", "p2"));
    verifyOrderedResult("key", "desc", Arrays.asList("p2", "p1", "B", "A"));

    // verify order by name
    verifyOrderedResult("name", "asc", Arrays.asList("p1", "p2", "A", "B"));
    verifyOrderedResult("name", "desc", Arrays.asList("B", "A", "p2", "p1"));

    // verify order by tenantId
    verifyOrderedResult("tenantId", "asc", Arrays.asList("B", "p2", "A", "p1"));
    verifyOrderedResult("tenantId", "desc", Arrays.asList("p1", "A", "p2", "B"));

    // verify order by instances
    verifyOrderedResult("instances", "asc", Arrays.asList("p2", "A", "p1", "B"));
    verifyOrderedResult("instances", "desc", Arrays.asList("B", "p1", "A", "p2"));

    // verify order by incidents
    verifyOrderedResult("incidents", "asc", Arrays.asList("p2", "B", "p1", "A"));
    verifyOrderedResult("incidents", "desc", Arrays.asList("A", "p1", "B", "p2"));
  }

  @Test
  public void shouldReturnCorrectStatistics() {
    // given a process with two process instances
    deployProcesses(1, "A", "Process A", 2, 0, "tenant1");

    var processInstances = runtimeService.createExecutionQuery()
        .processDefinitionKey("A")
        .list();

    assertThat(processInstances).hasSize(2);

    var processIncidents = runtimeService.createIncidentQuery()
        .processDefinitionKeyIn("A")
        .list();

    assertIncidentSize(processIncidents, 0);

    var pi1 = processInstances.get(0);
    var pi2 = processInstances.get(1);

    // and two different (failedJob, failedExternalTask) incidents occur for each process instance

    addIncidentToProcess(pi1, "failedJob");
    addIncidentToProcess(pi1, "failedExternalTask");

    addIncidentToProcess(pi2, "failedJob");
    addIncidentToProcess(pi2, "failedExternalTask");

    processIncidents = runtimeService.createIncidentQuery()
        .processDefinitionKeyIn("A")
        .list();

    assertIncidentSize(processIncidents, 4);

    assertIncidentSize(processIncidents, "failedJob", 2);
    assertIncidentSize(processIncidents, "failedExternalTask", 2);

    // when
    var results = resource.queryStatistics(uriInfo, 0, 50);

    // then statistics should be correct
    assertThat(results)
        .withFailMessage("The result should contain one entry (process definition 'Process A')")
        .hasSize(1);

    assertThat(results.get(0).getInstances())
        .withFailMessage("The number of process instances should be 2")
        .isEqualTo(2);

    assertThat(results.get(0).getIncidents())
        .withFailMessage("The total number of incidents should be 4")
        .isEqualTo(4);
  }

  private void addIncidentToProcess(Execution processInstance, String incidentType) {
    runtimeService.createIncident(incidentType, processInstance.getId(), "someConfig", "The message of failure");
  }

  private void assertIncidentSize(List<Incident> incidents, int size) {
    assertThat(incidents.size()).isEqualTo(size);
  }

  private void assertIncidentSize(List<Incident> incidents, String incidentType, int size) {
    assertThat(incidents.stream()
        .filter(incident -> incidentType.equals(incident.getIncidentType()))
        .count())
        .isEqualTo(size);
  }
}
