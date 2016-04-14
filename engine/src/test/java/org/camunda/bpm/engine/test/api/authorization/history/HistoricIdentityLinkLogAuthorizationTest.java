package org.camunda.bpm.engine.test.api.authorization.history;

import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import java.util.concurrent.Callable;
import org.camunda.bpm.engine.history.HistoricIdentityLinkLogQuery;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

public class HistoricIdentityLinkLogAuthorizationTest extends AuthorizationTest {
  protected static final String ONE_PROCESS_KEY = "demoAssigneeProcess";
  protected static final String CASE_KEY = "oneTaskCase";
  protected String deploymentId;

  public void setUp() throws Exception {
    deploymentId = createDeployment(null, "org/camunda/bpm/engine/test/api/authorization/oneTaskProcess.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/authorization/oneTaskCase.cmmn").getId();
    super.setUp();
  }

  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // historic identity link query (standalone task) - Authorization

  public void testQueryForStandaloneTaskHistoricIdentityLinkWithAuthrorization() {
    // given
    disableAuthorization();

    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);

    disableAuthorization();
    taskService.deleteTask("newTask", true);
    enableAuthorization();
  }

  public void testQueryForTaskHistoricIdentityLinkWithoutUserPermission() {
    // given
    disableAuthorization();
    startProcessInstance(ONE_PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryForTaskHistoricIdentityLinkWithUserPermission() {
    // given
    disableAuthorization();
    startProcessInstanceByKey(ONE_PROCESS_KEY);

    // if
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);

    enableAuthorization();
    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryCaseTask() {
    // given
    createCaseInstanceByKey(CASE_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    enableAuthorization();
    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testMixedQuery() {

    disableAuthorization();
    // given
    startProcessInstanceByKey(ONE_PROCESS_KEY);
    startProcessInstanceByKey(ONE_PROCESS_KEY);
    startProcessInstanceByKey(ONE_PROCESS_KEY);

    createCaseInstanceByKey(CASE_KEY);
    taskService.addCandidateUser(taskService.createTaskQuery().list().get(3).getId(), "dUserId");
    createCaseInstanceByKey(CASE_KEY);
    taskService.addCandidateUser(taskService.createTaskQuery().list().get(4).getId(), "eUserId");

    createTaskAndAssignUser("one");
    createTaskAndAssignUser("two");
    createTaskAndAssignUser("three");
    createTaskAndAssignUser("four");
    createTaskAndAssignUser("five");

    enableAuthorization();

    // when
    HistoricIdentityLinkLogQuery query = historyService.createHistoricIdentityLinkLogQuery();
    // then
    verifyQueryResults(query, 7);

    disableAuthorization();

    query = historyService.createHistoricIdentityLinkLogQuery();
    // then
    verifyQueryResults(query, 10);

    // if
    createGrantAuthorization(PROCESS_DEFINITION, ONE_PROCESS_KEY, userId, READ_HISTORY);
    enableAuthorization();
    query = historyService.createHistoricIdentityLinkLogQuery();
    // then
    verifyQueryResults(query, 10);

    deleteTask("one", true);
    deleteTask("two", true);
    deleteTask("three", true);
    deleteTask("four", true);
    deleteTask("five", true);
  }

  public void createTaskAndAssignUser(String taskId) {
    Task task = taskService.newTask(taskId);
    task.setAssignee("demo");
    taskService.saveTask(task);
  }

  protected Deployment createDeployment(final String name, final String... resources) {
    return runWithoutAuthorization(new Callable<Deployment>() {
      public Deployment call() throws Exception {
        DeploymentBuilder builder = repositoryService.createDeployment();
        for (String resource : resources) {
          builder.addClasspathResource(resource);
        }
        return builder.deploy();
      }
    });
  }

  protected void deleteDeployment(String deploymentId) {
    deleteDeployment(deploymentId, true);
  }

  protected void deleteDeployment(final String deploymentId, final boolean cascade) {
    Authentication authentication = identityService.getCurrentAuthentication();
    try {
      identityService.clearAuthentication();
      runWithoutAuthorization(new Callable<Void>() {
        public Void call() throws Exception {
          repositoryService.deleteDeployment(deploymentId, cascade);
          return null;
        }
      });
    } finally {
      if (authentication != null) {
        identityService.setAuthentication(authentication);
      }
    }
  }

  protected ProcessInstance startProcessInstance(String key) {
    return runtimeService.startProcessInstanceByKey(key);
  }
}
