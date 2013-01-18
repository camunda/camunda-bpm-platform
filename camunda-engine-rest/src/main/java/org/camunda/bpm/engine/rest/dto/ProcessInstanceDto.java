package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.runtime.ProcessInstance;

@XmlRootElement(name = "data")
public class ProcessInstanceDto {
  
  private String id;
  private String definitionId;
  private String businessKey;
  private boolean ended;
  private boolean suspended;
  
  @XmlElement
  public String getId() {
    return id;
  }
  
  @XmlElement
  public String getDefinitionId() {
    return definitionId;
  }
  
  @XmlElement
  public String getBusinessKey() {
    return businessKey;
  }
  
  @XmlElement
  public boolean isEnded() {
    return ended;
  }
  
  @XmlElement
  public boolean isSuspended() {
    return suspended;
  }
  
  public static ProcessInstanceDto fromProcessInstance(ProcessInstance instance) {
    ProcessInstanceDto result = new ProcessInstanceDto();
    result.id = instance.getId();
    result.definitionId = instance.getProcessDefinitionId();
    result.businessKey = instance.getBusinessKey();
    result.ended = instance.isEnded();
    result.suspended = instance.isSuspended();
    return result;
  }
}
