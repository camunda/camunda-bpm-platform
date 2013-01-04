package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.repository.ProcessDefinition;

@XmlRootElement(name = "definition")
public class ProcessDefinitionDto {

  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public static ProcessDefinitionDto fromProcessDefinition(ProcessDefinition definition) {
    ProcessDefinitionDto dto = new ProcessDefinitionDto();
    dto.id = definition.getId();
    
    return dto;
  }
}
