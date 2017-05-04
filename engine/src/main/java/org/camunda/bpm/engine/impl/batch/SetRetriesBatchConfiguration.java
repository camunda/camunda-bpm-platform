package org.camunda.bpm.engine.impl.batch;

import java.util.List;

public class SetRetriesBatchConfiguration extends BatchConfiguration {

  protected int retries;
  
  public SetRetriesBatchConfiguration(List<String> ids, int retries) {
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
