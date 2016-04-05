package org.camunda.bpm.engine.test.history;

import java.util.List;
import org.camunda.bpm.engine.history.HistoricIdentityLink;
import org.camunda.bpm.engine.history.HistoricIdentityLinkQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.IdentityLink;

public class HistoricIdentityLinkTestByXml extends PluggableProcessEngineTestCase{
  
  private static String PROCESS_DEFINITION_KEY_CANDIDATE_USER = "oneTaskProcessForHistoricIdentityLinkWithCanidateUser";
  private static String PROCESS_DEFINITION_KEY_CANDIDATE_GROUP = "oneTaskProcessForHistoricIdentityLinkWithCanidateGroup";
  private static String PROCESS_DEFINITION_KEY_ASSIGNEE = "oneTaskProcessForHistoricIdentityLinkWithAssignee";
  private static String PROCESS_DEFINITION_KEY_CANDIDATE_STARTER_USER = "oneTaskProcessForHistoricIdentityLinkWithCanidateStarterUsers";
  private static String PROCESS_DEFINITION_KEY_CANDIDATE_STARTER_GROUP = "oneTaskProcessForHistoricIdentityLinkWithCanidateStarterGroups";
  private static final String XML_USER = "demo";
  private static final String XML_GROUP = "demoGroups";
  private static final String XML_ASSIGNEE = "assignee";
  
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcessWithCandidateUser.bpmn20.xml" })
  public void testShouldAddTaskCandidateforAddIdentityLinkUsingXml() {

    // Pre test
    List<HistoricIdentityLink> historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY_CANDIDATE_USER);
    historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // query Test
    HistoricIdentityLinkQuery query = historyService.createHistoricIdentityLinkQuery();
    assertEquals(query.userId(XML_USER).count(), 1);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcessWithTaskAssignee.bpmn20.xml" })
  public void testShouldAddTaskAssigneeforAddIdentityLinkUsingXml() {

    // Pre test
    List<HistoricIdentityLink> historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY_ASSIGNEE);
    historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // query Test
    HistoricIdentityLinkQuery query = historyService.createHistoricIdentityLinkQuery();
    assertEquals(query.userId(XML_ASSIGNEE).count(), 1);
   
    
  }
  
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcessWithCandidateGroups.bpmn20.xml" })
  public void testShouldAddTaskCandidateGroupforAddIdentityLinkUsingXml() {

    // Pre test
    List<HistoricIdentityLink> historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 0);

    // given
    startProcessInstance(PROCESS_DEFINITION_KEY_CANDIDATE_GROUP);
    historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // query Test
    HistoricIdentityLinkQuery query = historyService.createHistoricIdentityLinkQuery();
    assertEquals(query.groupId(XML_GROUP).count(), 1);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcessWithCandidateStarterUsers.bpmn20.xml" })
  public void testShouldAddProcessCandidateStarterUserforAddIdentityLinkUsingXml() {

    // Pre test - Historical identity link is added as part of deployment
    List<HistoricIdentityLink> historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // given
    ProcessDefinition latestProcessDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_CANDIDATE_STARTER_USER)
        .singleResult();
    assertNotNull(latestProcessDef);

    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
    assertEquals(1, links.size());

    historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // query Test
    HistoricIdentityLinkQuery query = historyService.createHistoricIdentityLinkQuery();
    assertEquals(query.userId(XML_USER).count(), 1);
  }
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcessWithCandidateStarterGroups.bpmn20.xml" })
  public void testShouldAddProcessCandidateStarterGroupforAddIdentityLinkUsingXml() {

    // Pre test - Historical identity link is added as part of deployment
    List<HistoricIdentityLink> historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // given
    ProcessDefinition latestProcessDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey(PROCESS_DEFINITION_KEY_CANDIDATE_STARTER_GROUP)
        .singleResult();
    assertNotNull(latestProcessDef);

    List<IdentityLink> links = repositoryService.getIdentityLinksForProcessDefinition(latestProcessDef.getId());
    assertEquals(1, links.size());

    historicIdentityLinks = historyService.createHistoricIdentityLinkQuery().list();
    assertEquals(historicIdentityLinks.size(), 1);

    // query Test
    HistoricIdentityLinkQuery query = historyService.createHistoricIdentityLinkQuery();
    assertEquals(query.groupId(XML_GROUP).count(), 1);
  }
  protected ProcessInstance startProcessInstance(String key) {
    return runtimeService.startProcessInstanceByKey(key);
  }
}
