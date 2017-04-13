package org.camunda.bpm.engine.impl.batch.externaltask;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;

public class SetExternalTaskRetriesBatchConfiguration extends BatchConfiguration {

  protected int retries;
  
  public SetExternalTaskRetriesBatchConfiguration(List<String> ids, int retries) {
    super(ids);
    this.retries = retries;
  }
  
  public int getRetries() {
    return retries;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

}
