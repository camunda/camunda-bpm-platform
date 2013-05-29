package org.camunda.bpm.cockpit.plugin.base.persistence.entity;

import java.util.Date;

public class ProcessInstanceDto {

  private String id;
  private String businessKey;
  private Date startTime;
  private long localFailedJobs;
  private long child1FailedJobs;
  private long child2FailedJobs;
  private long child3FailedJobs;
  
  public ProcessInstanceDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }
  
  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public long getLocalFailedJobs() {
    return localFailedJobs;
  }

  public void setLocalFailedJobs(long localFailedJobs) {
    this.localFailedJobs = localFailedJobs;
  }

  public long getChild1FailedJobs() {
    return child1FailedJobs;
  }

  public void setChild1FailedJobs(long child1FailedJobs) {
    this.child1FailedJobs = child1FailedJobs;
  }

  public long getChild2FailedJobs() {
    return child2FailedJobs;
  }

  public void setChild2FailedJobs(long child2FailedJobs) {
    this.child2FailedJobs = child2FailedJobs;
  }

  public long getChild3FailedJobs() {
    return child3FailedJobs;
  }

  public void setChild3FailedJobs(long child3FailedJobs) {
    this.child3FailedJobs = child3FailedJobs;
  }

}
