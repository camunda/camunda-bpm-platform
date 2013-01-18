package org.camunda.bpm.engine.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.activiti.engine.repository.ProcessDefinition;

@XmlRootElement(name = "data")
public class ProcessDefinitionDto {

  private String id;
  private String key;
  private String category;
  private String description;
  private String name;
  private int version;
  private String resource;
  private String deploymentId;
  private String diagram;
  private boolean suspended;
  
  
  @XmlElement
  public String getId() {
    return id;
  }

  @XmlElement
  public String getKey() {
    return key;
  }

  @XmlElement
  public String getCategory() {
    return category;
  }

  @XmlElement
  public String getDescription() {
    return description;
  }

  @XmlElement
  public String getName() {
    return name;
  }

  @XmlElement
  public int getVersion() {
    return version;
  }

  @XmlElement
  public String getResource() {
    return resource;
  }

  @XmlElement
  public String getDeploymentId() {
    return deploymentId;
  }

  @XmlElement
  public String getDiagram() {
    return diagram;
  }

  @XmlElement
  public boolean isSuspended() {
    return suspended;
  }

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
