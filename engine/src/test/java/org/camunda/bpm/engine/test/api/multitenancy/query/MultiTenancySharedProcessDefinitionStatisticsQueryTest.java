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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancySharedProcessDefinitionStatisticsQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String ONE_TASK_PROCESS_DEFINITION_KEY = "oneTaskProcess";
  protected static final String FAILED_JOBS_PROCESS_DEFINITION_KEY = "ExampleProcess";
  
  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {

      TenantIdTestProvider tenantIdProvider = new TenantIdTestProvider(TENANT_ONE);
      configuration.setTenantIdProvider(tenantIdProvider);

      return configuration;
    }
  };

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RuntimeService runtimeService;

  protected ManagementService managementService;

  protected IdentityService identityService;

  protected BpmnModelInstance oneTaskProcess;
  
  @Rule
  public RuleChain tenantRuleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    oneTaskProcess = Bpmn.createExecutableProcess(ONE_TASK_PROCESS_DEFINITION_KEY)
      .startEvent().userTask().done();
  
    runtimeService = engineRule.getRuntimeService();
    identityService = engineRule.getIdentityService();
    managementService = engineRule.getManagementService();
    
  }
  
  @Test
  public void activeProcessInstancesCountWithUserBelongsToNoTenant() {

    testRule.deploy(oneTaskProcess);

    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);

    identityService.setAuthentication("user", null, null);
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user must see only the process instances that belongs to no tenant
    assertEquals(1, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void activeProcessInstancesCountWithUserBelongsToTenant() {

    testRule.deploy(oneTaskProcess);
    
    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user can see the process instances that belongs to tenant1 and instances that have no tenant  
    assertEquals(2, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void activeProcessInstancesCountWithUserBelongsToMultipleTenant() {

    testRule.deploy(oneTaskProcess);

    startProcessInstances(ONE_TASK_PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));
    
    List<ProcessDefinitionStatistics> processDefinitionsStatistics = managementService
      .createProcessDefinitionStatisticsQuery()
      .list();
 
    // then
    assertEquals(1, processDefinitionsStatistics.size());
    // user can see all the active process instances 
    assertEquals(3, processDefinitionsStatistics.get(0).getInstances());
    
  }

  @Test
  public void failedJobsCountWithUserBelongsToNoTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");
    
    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(1, processDefinitionsStatistics.get(0).getFailedJobs());
    
  }

  @Test
  public void failedJobsCountWithUserBelongsToTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(2, processDefinitionsStatistics.get(0).getFailedJobs());
  }

  @Test
  public void failedJobsCountWithUserBelongsToNultipleTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    assertEquals(3, processDefinitionsStatistics.get(0).getFailedJobs());
  }

  @Test
  public void incidentsCountWithUserBelongsToNoTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, null);

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(1, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithUserBelongsToTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithUserBelongsToMultipleTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(3, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsCountWithIncidentTypeAndUserBelongsToTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeIncidentsForType("failedJob")
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionsStatistics.get(0).getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  @Test
  public void incidentsFailedJobsAndIncidentsCountWithUserBelongsToTenant() {

    testRule.deploy("org/camunda/bpm/engine/test/api/multitenancy/statisticsQueryWithFailedJobs.bpmn20.xml");

    startProcessInstances(FAILED_JOBS_PROCESS_DEFINITION_KEY);

    executeAvailableJobs();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<ProcessDefinitionStatistics> processDefinitionsStatistics =
        managementService
        .createProcessDefinitionStatisticsQuery()
        .includeFailedJobs()
        .includeIncidents()
        .list();

    // then
    assertEquals(1, processDefinitionsStatistics.size());
    ProcessDefinitionStatistics processDefinitionStatistics = processDefinitionsStatistics.get(0);
    assertEquals(2, processDefinitionStatistics.getInstances());
    assertEquals(2, processDefinitionStatistics.getFailedJobs());
    
    List<IncidentStatistics> incidentStatistics = processDefinitionStatistics.getIncidentStatistics();
    assertEquals(1, incidentStatistics.size());
    assertEquals(2, incidentStatistics.get(0).getIncidentCount());
  }

  protected void startProcessInstances(String key) {
    setTenantIdProvider(null);
    runtimeService.startProcessInstanceByKey(key);

    setTenantIdProvider(TENANT_ONE);
    runtimeService.startProcessInstanceByKey(key);
    
    setTenantIdProvider(TENANT_TWO);
    runtimeService.startProcessInstanceByKey(key);  
  }

  protected void setTenantIdProvider(String tenantId) {
    TenantIdTestProvider tenantIdProvider = (TenantIdTestProvider)(engineRule.getProcessEngineConfiguration().getTenantIdProvider());
    tenantIdProvider.setTenantIdProviderForProcessInstance(tenantId);
  }

  protected void executeAvailableJobs(){
    executeAvailableJobs(0, Integer.MAX_VALUE, true);
  }
  
  protected void executeAvailableJobs(int jobsExecuted, int expectedExecutions, boolean ignoreLessExecutions) {
    List<Job> jobs = managementService.createJobQuery().withRetriesLeft().list();

    if (jobs.isEmpty()) {
      assertTrue("executed less jobs than expected. expected <" + expectedExecutions + "> actual <" + jobsExecuted + ">",
          jobsExecuted == expectedExecutions || ignoreLessExecutions);
      return;
    }

    for (Job job : jobs) {
      try {
        managementService.executeJob(job.getId());
        jobsExecuted += 1;
      } catch (Exception e) {}
    }

    assertTrue("executed more jobs than expected. expected <" + expectedExecutions + "> actual <" + jobsExecuted + ">",
        jobsExecuted <= expectedExecutions);

    executeAvailableJobs(jobsExecuted, expectedExecutions, ignoreLessExecutions);
  }

  public static class TenantIdTestProvider implements TenantIdProvider {

    public String tenantId;

    public TenantIdTestProvider(String tenantId) {
      this.tenantId = tenantId;
    }

    public void setTenantIdProviderForProcessInstance(String tenantId) {
      this.tenantId = tenantId;
    }
    
    @Override
    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return tenantId;
    }

    @Override
    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }

    @Override
    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return null;
    }
  }
}
