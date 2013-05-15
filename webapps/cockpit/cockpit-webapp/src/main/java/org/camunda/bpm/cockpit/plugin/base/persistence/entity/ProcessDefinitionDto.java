package org.camunda.bpm.cockpit.plugin.base.persistence.entity;

public class ProcessDefinitionDto {
  
  private String id;
  private String name;
  private String key;
  private String deploymentId;
  private long suspensionState;
  private long failedJobs;
  private long version;
  
  public ProcessDefinitionDto() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  public long getSuspensionState() {
    return suspensionState;
  }

  public void setSuspensionState(long suspensionState) {
    this.suspensionState = suspensionState;
  }

  public long getFailedJobs() {
    return failedJobs;
  }

  public void setFailedJobs(long failedJobs) {
    this.failedJobs = failedJobs;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
  
}
