package org.camunda.bpm.engine.rest.dto.externaltask;

public class ExtendLockOnExternalTaskDto {

  private String workerId;
  private long newDuration;

  public String getWorkerId() {
    return workerId;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public long getNewDuration() {
    return newDuration;
  }

  public void setNewDuration(long newDuration) {
    this.newDuration = newDuration;
  }

}
