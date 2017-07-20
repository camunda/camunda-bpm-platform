package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * 
 * @author Anna.Pazola
 *
 */
public class ExtendLockOnExternalTaskCmd extends HandleExternalTaskCmd {

  private long newLockTime;

  public ExtendLockOnExternalTaskCmd(String externalTaskId, String workerId, long newLockTime) {
    super(externalTaskId, workerId);
    EnsureUtil.ensurePositive(BadUserRequestException.class, "lockTime", newLockTime);
    this.newLockTime = newLockTime;
  }

  @Override
  public String getErrorMessageOnWrongWorkerAccess() {
    return "The lock of the External Task " + externalTaskId + " cannot be extended by worker '" + workerId + "'";
  }

  @Override
  protected void execute(ExternalTaskEntity externalTask) {
    EnsureUtil.ensureGreaterThanOrEqual(BadUserRequestException.class, "Cannot extend a lock that expired",
        "lockExpirationTime", externalTask.getLockExpirationTime().getTime(), ClockUtil.getCurrentTime().getTime());
    externalTask.extendLock(newLockTime);
  }
}
