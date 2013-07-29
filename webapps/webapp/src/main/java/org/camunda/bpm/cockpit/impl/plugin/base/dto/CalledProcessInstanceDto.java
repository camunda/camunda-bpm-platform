package org.camunda.bpm.cockpit.impl.plugin.base.dto;

public class CalledProcessInstanceDto extends ProcessInstanceDto {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  
  protected String callActivityInstanceId;
  protected String callActivityId;
  
  public CalledProcessInstanceDto() {}

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCallActivityInstanceId() {
    return callActivityInstanceId;
  }

  public String getCallActivityId() {
    return callActivityId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }
  
}
