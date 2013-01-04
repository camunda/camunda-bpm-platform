package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.repository.ProcessDefinition;

@XmlRootElement(name = "definition")
public class ProcessDefinitionDto {

  @XmlElement
  private String id;
  
  @XmlElement
  private String key;

  public String getId() {
    return id;
  }
  
  public String getKey() {
    return key;
  }

  public static ProcessDefinitionDto fromProcessDefinition(ProcessDefinition definition) {
    ProcessDefinitionDto dto = new ProcessDefinitionDto();
    dto.id = definition.getId();
    dto.key = definition.getKey();
    
    return dto;
  }
}
