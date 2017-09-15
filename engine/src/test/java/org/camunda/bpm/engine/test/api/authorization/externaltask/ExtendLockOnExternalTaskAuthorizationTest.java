package org.camunda.bpm.engine.test.api.authorization.externaltask;

import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ExtendLockOnExternalTaskAuthorizationTest extends HandleExternalTaskAuthorizationTest {

  @Override
  public void testExternalTaskApi(LockedExternalTask task) {
    engineRule.getExternalTaskService().extendLock(task.getId(), "workerId", 2000L);
  }

  @Override
  public void assertExternalTaskResults() {
    ExternalTask taskWithExtendedLock = engineRule.getExternalTaskService().createExternalTaskQuery().locked().singleResult();
    Assert.assertNotNull(taskWithExtendedLock);
  }
}
