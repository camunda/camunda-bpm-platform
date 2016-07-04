package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static org.junit.Assert.*;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
*
* @author Deivarayan Azhagappan
*
*/
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricIdentityLinkLogQueryTest {
  
  private static final String GROUP_1 = "Group1";
  private static final String USER_1 = "User1";
  
  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected static final String A_USER_ID = "aUserId";

  protected final static String TENANT_1 = "tenant1";
  protected final static String TENANT_2 = "tenant2";
  protected final static String TENANT_3 = "tenant3";

  @Before
  public void init() {
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    runtimeService = engineRule.getRuntimeService();

    // create sample identity link
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
    .startEvent()
    .userTask("task").camundaCandidateUsers(A_USER_ID)
    .endEvent()
    .done();

    // deploy tenants
    testRule.deployForTenant(TENANT_1, oneTaskProcess);
    testRule.deployForTenant(TENANT_2, oneTaskProcess);
    testRule.deployForTenant(TENANT_3, oneTaskProcess);  
  }

  @Test
  public void addandDeleteHistoricIdentityLinkForSingleTenant() {

    startProcessInstanceForTenant(TENANT_1);
    
    HistoricIdentityLinkLog historicIdentityLink = historyService
        .createHistoricIdentityLinkLogQuery()
        .singleResult();
    
    taskService.deleteCandidateUser(historicIdentityLink.getTaskId(), A_USER_ID);
    
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 2);
    
  }

  @Test
  public void historicIdentityLinkForMultipleTenant() {
    startProcessInstanceForTenant(TENANT_1);
    
    // Query test
    HistoricIdentityLinkLog historicIdentityLink = historyService
        .createHistoricIdentityLinkLogQuery()
        .singleResult();
    
    assertEquals(historicIdentityLink.getTenantId(), TENANT_1);
    
    // start process instance for another tenant
    startProcessInstanceForTenant(TENANT_2);
    
    // Query test
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService
    .createHistoricIdentityLinkLogQuery()
    .list();

    assertEquals(historicIdentityLinks.size(), 2);

    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 1);
    
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 1);
  }
  
  @Test
  public void addAndRemoveHistoricIdentityLinksForProcessDefinitionWithTenantId() throws Exception {
    String resourceName = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
    testRule.deployForTenant(TENANT_1, resourceName);
    testRule.deployForTenant(TENANT_2, resourceName);

    ProcessDefinition processDefinition1 = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey(PROCESS_DEFINITION_KEY)
      .list()
      .get(0);
    ProcessDefinition processDefinition2 = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey(PROCESS_DEFINITION_KEY)
      .list()
      .get(1);

    assertNotNull(processDefinition1);
    assertNotNull(processDefinition2);

    testTenantsByProcessDefinition(processDefinition1.getId());   
    testTenantsByProcessDefinition(processDefinition2.getId());
    
    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService
        .createHistoricIdentityLinkLogQuery()
        .list();
    
    assertEquals(historicIdentityLinks.size(), 8);

    // Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 4);
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 4);
  }

  @SuppressWarnings("deprecation")
  public void testTenantsByProcessDefinition(String processDefinitionId) {
    
    repositoryService.addCandidateStarterGroup(processDefinitionId, GROUP_1);
    
    repositoryService.addCandidateStarterUser(processDefinitionId, USER_1);
    
    repositoryService.deleteCandidateStarterGroup(processDefinitionId, GROUP_1);

    repositoryService.deleteCandidateStarterUser(processDefinitionId, USER_1);

  }

  @SuppressWarnings("deprecation")
  @Test
  public void identityLinksForProcessDefinitionWithTenantId() throws Exception {
    String resourceName = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
    testRule.deployForTenant(TENANT_1, resourceName);
    testRule.deployForTenant(TENANT_2, resourceName);

    ProcessDefinition processDefinition1 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .list()
        .get(0);

    assertNotNull(processDefinition1);

    // Add candidate group with process definition 1
    repositoryService.addCandidateStarterGroup(processDefinition1.getId(), GROUP_1);

    // Add candidate user for process definition 2
    repositoryService.addCandidateStarterUser(processDefinition1.getId(), USER_1);

    ProcessDefinition processDefinition2 = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .list()
        .get(1);
    
    assertNotNull(processDefinition2);

    // Add candidate group with process definition 2
    repositoryService.addCandidateStarterGroup(processDefinition2.getId(), GROUP_1);

    // Add candidate user for process definition 2
    repositoryService.addCandidateStarterUser(processDefinition2.getId(), USER_1);

    // Identity link test
    List<IdentityLink> identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition1.getId());
    assertEquals(identityLinks.size(),2);
    assertEquals(identityLinks.get(0).getTenantId(), TENANT_1);
    assertEquals(identityLinks.get(1).getTenantId(), TENANT_1);

    identityLinks = repositoryService.getIdentityLinksForProcessDefinition(processDefinition2.getId());
    assertEquals(identityLinks.size(),2);
    assertEquals(identityLinks.get(0).getTenantId(), TENANT_2);
    assertEquals(identityLinks.get(1).getTenantId(), TENANT_2);
    
  }

  @Test
  public void singleQueryForMultipleTenant() {
    startProcessInstanceForTenant(TENANT_1);
    startProcessInstanceForTenant(TENANT_2);
    startProcessInstanceForTenant(TENANT_3);

    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1, TENANT_2).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2, TENANT_3).count(), 2);

    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1, TENANT_2, TENANT_3).count(), 3);

  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }
}
