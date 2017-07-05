package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Collection;

import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class CreateAndResolveIncidentAuthorizationTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testRule);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.UPDATE),
          grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.UPDATE_INSTANCE)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_INSTANCE, "processInstance", "userId", Permissions.UPDATE)
        )
        .succeeds(),
      scenario()
        .withAuthorizations(
          grant(Resources.PROCESS_DEFINITION, "Process", "userId", Permissions.UPDATE_INSTANCE)
        )
        .succeeds()
    );
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  public void createIncident() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process");
    ExecutionEntity execution = (ExecutionEntity) engineRule.getRuntimeService().createExecutionQuery().active().singleResult();
    
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance", processInstance.getId())
        .bindResource("processDefinition", "Process")
        .start();

    engineRule.getRuntimeService()
        .createIncident("foo", execution.getId(), execution.getActivityId(), "bar");

    // then
    authRule.assertScenario(scenario);
  }

  @Test
  public void resolveIncident() {
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceByKey("Process");
    ExecutionEntity execution = (ExecutionEntity) engineRule.getRuntimeService().createExecutionQuery().active().singleResult();

    authRule.disableAuthorization();
    Incident incident = engineRule.getRuntimeService()
        .createIncident("foo", execution.getId(), execution.getActivityId(), "bar");

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance", processInstance.getId())
        .bindResource("processDefinition", "Process")
        .start();

    // when
    engineRule.getRuntimeService().resolveIncident(incident.getId());

    // then
    authRule.assertScenario(scenario);
  }
}
