package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.repository.ProcessDefinition;

@XmlRootElement(name = "data")
public class ProcessDefinitionDto {

  @XmlElement
  private String id;
  
  @XmlElement
  private String key;
  
  @XmlElement
  private String category;
  
  @XmlElement
  private String description;
  
  @XmlElement
  private String name;
  
  @XmlElement
  private int version;
  
  @XmlElement
  private String resource;
  
  @XmlElement
  private String deploymentId;
  
  @XmlElement
  private String diagram;
  
  @XmlElement
  private boolean suspended;

  public static ProcessDefinitionDto fromProcessDefinition(ProcessDefinition definition) {
    ProcessDefinitionDto dto = new ProcessDefinitionDto();
    dto.id = definition.getId();
    dto.key = definition.getKey();
    dto.category = definition.getCategory();
    dto.description = definition.getDescription();
    dto.name = definition.getName();
    dto.version = definition.getVersion();
    dto.resource = definition.getResourceName();
    dto.deploymentId = definition.getDeploymentId();
    dto.diagram = definition.getDiagramResourceName();
    dto.suspended = definition.isSuspended();
    
    return dto;
  }
}
