package org.camunda.bpm.engine.test.api.authorization.externaltask;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Please note that if you want to reuse Rule and other fields you should create abstract class
 * and pack it there.
 *
 * @see HandleExternalTaskAuthorizationTest
 *
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class GetErrorDetailsAuthorizationTest {
  private static final String ERROR_DETAILS = "theDetails";
  protected String deploymentId;
  private String currentDetails;

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withoutAuthorizations()
            .failsDueToRequired(
                grant(Resources.PROCESS_INSTANCE, "processInstanceId", "userId", Permissions.READ),
                grant(Resources.PROCESS_DEFINITION, "oneExternalTaskProcess", "userId", Permissions.READ_INSTANCE)),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_INSTANCE, "processInstanceId", "userId", Permissions.READ))
            .succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.READ))
            .succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "processDefinitionKey", "userId", Permissions.READ_INSTANCE))
            .succeeds(),
        scenario()
            .withAuthorizations(
                grant(Resources.PROCESS_DEFINITION, "*", "userId", Permissions.READ_INSTANCE))
            .succeeds()
    );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
  public void testCompleteExternalTask() {

    // given
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("oneExternalTaskProcess");
    List<LockedExternalTask> tasks = engineRule.getExternalTaskService()
        .fetchAndLock(5, "workerId")
        .topic("externalTaskTopic", 5000L)
        .execute();

    LockedExternalTask task = tasks.get(0);

    //preconditions method
    engineRule.getExternalTaskService().handleFailure(task.getId(),task.getWorkerId(),"anError",ERROR_DETAILS,1,1000L);

    // when
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstanceId", processInstance.getId())
        .bindResource("processDefinitionKey", "oneExternalTaskProcess")
        .start();

    //execution method
    currentDetails = engineRule.getExternalTaskService().getExternalTaskErrorDetails(task.getId());

    // then
    if (authRule.assertScenario(scenario)) {
      //assertion method
      assertThat(currentDetails,is(ERROR_DETAILS));
    }
  }

}
