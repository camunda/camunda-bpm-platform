package org.camunda.bpm.engine.rest.dto.externaltask;

import java.util.List;

public class SetRetriesForExternalTasksDto {

  protected List<String> externalTaskIds;
  protected ExternalTaskQueryDto externalTaskQuery;
  protected int retries;
  
  public List<String> getExternalTaskIds() {
    return externalTaskIds;
  }
  
  public void setExternalTaskIds(List<String> externalTaskIds) {
    this.externalTaskIds = externalTaskIds;
  }
  
  public ExternalTaskQueryDto getExternalTaskQuery() {
    return externalTaskQuery;
  }
  
  public void setExternalTaskQuery(ExternalTaskQueryDto externalTaskQuery) {
    this.externalTaskQuery = externalTaskQuery;
  }
  
  public int getRetries() {
    return retries;
  }
  
  public void setRetries(int retries) {
    this.retries = retries;
  }
}
