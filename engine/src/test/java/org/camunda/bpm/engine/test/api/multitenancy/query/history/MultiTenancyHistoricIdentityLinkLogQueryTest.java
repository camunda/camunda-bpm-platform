package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import java.util.List;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLog;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
*
* @author Deivarayan Azhagappan
*
*/

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricIdentityLinkLogQueryTest extends PluggableProcessEngineTestCase{

  protected final static String TENANT_1 = "tenant1";
  protected final static String TENANT_2 = "tenant2";
  protected final static String TENANT_3 = "tenant3";
  
  private static final String A_USER_ID = "aUserId";
  
  private static final String GROUP_1 = "Group1";
  private static final String USER_1 = "User1";
  
  private static final String IDENTITY_LINK_ADD="add";
  private static final String IDENTITY_LINK_DELETE="delete";
  
  private static String PROCESS_DEFINITION_KEY = "oneTaskProcess";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
    .startEvent()
    .userTask("task").camundaCandidateUsers(A_USER_ID)
    .endEvent()
    .done();
    
    deploymentForTenant(TENANT_1, oneTaskProcess);
    deploymentForTenant(TENANT_2, oneTaskProcess);
    deploymentForTenant(TENANT_3, oneTaskProcess);
  }

  public void testAddHistoricIdentityLinkForSingleTenant() {

    startProcessInstanceForTenant(TENANT_1);
    
    // Query test
    HistoricIdentityLinkLog historicIdentityLink = historyService
        .createHistoricIdentityLinkLogQuery()
        .singleResult();
    
    assertEquals(historicIdentityLink.getUserId(), A_USER_ID);
    assertEquals(historicIdentityLink.getType(), IdentityLinkType.CANDIDATE);
    assertEquals(historicIdentityLink.getTenantId(), TENANT_1);
    assertEquals(historicIdentityLink.getOperationType(), IDENTITY_LINK_ADD);
    
    taskService.deleteCandidateUser(historicIdentityLink.getTaskId(), A_USER_ID);
  }

  public void testAddandDeleteHistoricIdentityLinkForSingleTenant() {

    startProcessInstanceForTenant(TENANT_1);
    
    HistoricIdentityLinkLog historicIdentityLink = historyService
        .createHistoricIdentityLinkLogQuery()
        .singleResult();
    
    taskService.deleteCandidateUser(historicIdentityLink.getTaskId(), A_USER_ID);
    
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 2);
    assertEquals(query.userId(A_USER_ID).count(), 2);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);
    
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.operationType(IDENTITY_LINK_DELETE).count(), 1);
  }

  public void testHistoricIdentityLinkForMultipleTenant() {
    startProcessInstanceForTenant(TENANT_1);
    
    // Query test
    HistoricIdentityLinkLog historicIdentityLink = historyService
        .createHistoricIdentityLinkLogQuery()
        .singleResult();
    
    assertEquals(historicIdentityLink.getUserId(), A_USER_ID);
    assertEquals(historicIdentityLink.getType(), IdentityLinkType.CANDIDATE);
    assertEquals(historicIdentityLink.getTenantId(), TENANT_1);
    assertEquals(historicIdentityLink.getOperationType(), IDENTITY_LINK_ADD);
    
    // start process instance for another tenant
    startProcessInstanceForTenant(TENANT_2);
    
    // Query test
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 1);
    
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.userId(A_USER_ID).count(), 2);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 2);
    assertEquals(query.type(IdentityLinkType.CANDIDATE).count(), 2);
    
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 1);
  }
  
  @SuppressWarnings("deprecation")
  public void testQueryAddAndRemoveHistoricIdentityLinksForProcessDefinitionWithTenantId() throws Exception {
    String resourceName = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
    deploymentForTenant(TENANT_1, resourceName);
    deploymentForTenant(TENANT_2, resourceName);

    ProcessDefinition processDefinition1 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list().get(0);
    ProcessDefinition processDefinition2 = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY).list().get(1);

    assertNotNull(processDefinition1);
    assertNotNull(processDefinition2);

    // Add candidate group with process definition 1
    repositoryService.addCandidateStarterGroup(processDefinition1.getId(), GROUP_1);

    // Add candidate group with process definition 2
    repositoryService.addCandidateStarterGroup(processDefinition2.getId(), GROUP_1);

    List<HistoricIdentityLinkLog> historicIdentityLinks = historyService
        .createHistoricIdentityLinkLogQuery()
        .list();
    
    assertEquals(historicIdentityLinks.size(), 2);

    // Query test based on tenant 1
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 1);
    assertEquals(query.processDefinitionId(processDefinition1.getId()).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);
    assertEquals(query.groupId(GROUP_1).count(), 1);

    // Query test based on tenant 2
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 1);
    assertEquals(query.processDefinitionId(processDefinition2.getId()).count(), 1);
    assertEquals(query.operationType(IDENTITY_LINK_ADD).count(), 1);
    assertEquals(query.groupId(GROUP_1).count(), 1);

    // Add candidate user for process definition 1
    repositoryService.addCandidateStarterUser(processDefinition1.getId(), USER_1);

    // Add candidate user for process definition 2
    repositoryService.addCandidateStarterUser(processDefinition2.getId(), USER_1);

    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 2);
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 2);

    // Delete candiate group with process definition 1
    repositoryService.deleteCandidateStarterGroup(processDefinition1.getId(), GROUP_1);

    // Delete candiate group with process definition 2
    repositoryService.deleteCandidateStarterGroup(processDefinition2.getId(), GROUP_1);

    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 3);
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 3);

    // Delete candidate user for process definition 1
    repositoryService.deleteCandidateStarterUser(processDefinition1.getId(), USER_1);

    // Delete candidate user for process definition 2
    repositoryService.deleteCandidateStarterUser(processDefinition2.getId(), USER_1);

    // Query test
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_1).count(), 4);
    query = historyService.createHistoricIdentityLinkLogQuery();
    assertEquals(query.tenantIdIn(TENANT_2).count(), 4);
  }

  @SuppressWarnings("deprecation")
  public void testIdentityLinksForProcessDefinitionWithTenantId() throws Exception {
    String resourceName = "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml";
    deploymentForTenant(TENANT_1, resourceName);
    deploymentForTenant(TENANT_2, resourceName);

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

  public void testSingleQueryForMultipleTenant() {
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
