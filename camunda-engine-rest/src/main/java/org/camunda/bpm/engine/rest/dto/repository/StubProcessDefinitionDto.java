package org.camunda.bpm.engine.rest.dto.repository;

/**
 * Setters are only needed to create stub results.
 * 
 * @author Thorben Lindhauer
 *
 */
public class StubProcessDefinitionDto extends ProcessDefinitionDto {

  public void setId(String id) {
    this.id = id;
  }
  public void setKey(String key) {
    this.key = key;
  }
  public void setCategory(String category) {
    this.category = category;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setVersion(int version) {
    this.version = version;
  }
  public void setResource(String resource) {
    this.resource = resource;
  }
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  public void setDiagram(String diagram) {
    this.diagram = diagram;
  }
  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }
}
