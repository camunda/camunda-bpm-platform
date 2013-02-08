package org.camunda.bpm.engine.rest.dto;

import org.activiti.engine.repository.ProcessDefinition;

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
  
  public String getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getCategory() {
    return category;
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public int getVersion() {
    return version;
  }

  public String getResource() {
    return resource;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public String getDiagram() {
    return diagram;
  }

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
