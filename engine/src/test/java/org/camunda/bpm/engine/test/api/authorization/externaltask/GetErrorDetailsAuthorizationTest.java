package org.camunda.bpm.engine.test.api.authorization.externaltask;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class GetErrorDetailsAuthorizationTest extends HandleExternalTaskAuthorizationReadOnlyTest {
  private static final String ERROR_DETAILS = "theDetails";
  protected String deploymentId;
  private String currentDetails;

  @Override
  protected void doPreProcessing(LockedExternalTask task) {
    engineRule.getExternalTaskService().handleFailure(task.getId(),task.getWorkerId(),"anError",ERROR_DETAILS,1,1000L);
  }

  @Override
  public void testExternalTaskApi(LockedExternalTask task) {
    currentDetails = engineRule.getExternalTaskService().getExternalTaskErrorDetails(task.getId());
  }

  @Override
  public void assertExternalTaskResults() {
    assertThat(currentDetails,is(ERROR_DETAILS));
  }
}
