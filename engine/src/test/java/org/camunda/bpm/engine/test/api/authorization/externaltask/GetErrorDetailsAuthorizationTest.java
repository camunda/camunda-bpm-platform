package org.camunda.bpm.engine.test.api.authorization.externaltask;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;

import java.util.List;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
public class GetErrorDetailsAuthorizationTest extends AuthorizationTest {
  private static final String ERROR_DETAILS = "theDetails";
  protected String deploymentId;

  protected String externalTaskId;
  protected String instance1Id;

  @Override
  protected void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml").getId();
    instance1Id = runtimeService.startProcessInstanceByKey("oneExternalTaskProcess").getId();

    List<LockedExternalTask> tasks = externalTaskService
        .fetchAndLock(5, "workerId")
        .topic("externalTaskTopic", 5000L)
        .execute();

    LockedExternalTask task = tasks.get(0);
    externalTaskId = task.getId();
    externalTaskService.handleFailure(task.getId(),task.getWorkerId(),"anError",ERROR_DETAILS,1,1000L);
    
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }


  public void testQueryWithoutAuthorization() {
    // when
    try {
      externalTaskService.getExternalTaskErrorDetails(externalTaskId);
    } catch (AuthorizationException e) {
      //expected
    }
  }

  public void testQueryWithReadOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    String errorDetails = externalTaskService.getExternalTaskErrorDetails(externalTaskId);

    // then
    assertThat(errorDetails,is(notNullValue()));
    assertThat(errorDetails,is(ERROR_DETAILS));
  }

  public void testQueryWithReadOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    String errorDetails = externalTaskService.getExternalTaskErrorDetails(externalTaskId);

    // then
    assertThat(errorDetails,is(notNullValue()));
    assertThat(errorDetails,is(ERROR_DETAILS));
  }

  public void testQueryWithReadInstanceOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);

    // when
    String errorDetails = externalTaskService.getExternalTaskErrorDetails(externalTaskId);

    // then
    assertThat(errorDetails,is(notNullValue()));
    assertThat(errorDetails,is(ERROR_DETAILS));
  }

  public void testQueryWithReadInstanceOnAnyProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    String errorDetails = externalTaskService.getExternalTaskErrorDetails(externalTaskId);

    // then
    assertThat(errorDetails,is(notNullValue()));
    assertThat(errorDetails,is(ERROR_DETAILS));
  }

  public void testQueryWithReadInstanceWithMultiple() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_DEFINITION, "oneExternalTaskProcess", userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, instance1Id, userId, READ);

    // when
    String errorDetails = externalTaskService.getExternalTaskErrorDetails(externalTaskId);

    // then
    assertThat(errorDetails,is(notNullValue()));
    assertThat(errorDetails,is(ERROR_DETAILS));
  }
}
