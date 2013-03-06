package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.repository.ProcessDefinition;

public class MockDefinitionBuilder {

  private String id = null;
  private String key = null;
  private String category = null;
  private String description = null;
  private String name = null;
  private int version = 0;
  private String resource = null;
  private String deploymentId = null;
  private String diagram = null;
  private boolean suspended = false;
  private boolean startFormKey = false;

  public MockDefinitionBuilder id(String id) {
    this.id = id;
    return this;
  }
  
  public MockDefinitionBuilder key(String key) {
    this.key = key;
    return this;
  }
  
  public MockDefinitionBuilder category(String category) {
    this.category = category;
    return this;
  }
  
  public MockDefinitionBuilder description(String description) {
    this.description = description;
    return this;
  }
  
  public MockDefinitionBuilder name(String name) {
    this.name = name;
    return this;
  }
  
  public MockDefinitionBuilder version(int version) {
    this.version = version;
    return this;
  }
  
  public MockDefinitionBuilder resource(String resource) {
    this.resource = resource;
    return this;
  }
  
  public MockDefinitionBuilder deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }
  
  public MockDefinitionBuilder diagram(String diagram) {
    this.diagram = diagram;
    return this;
  }
  
  public MockDefinitionBuilder suspended(boolean suspended) {
    this.suspended = suspended;
    return this;
  }
  
  public MockDefinitionBuilder startFormKey(boolean startFormKey) {
    this.startFormKey = startFormKey;
    return this;
  }
  
  public ProcessDefinition build() {
    ProcessDefinition mockDefinition = mock(ProcessDefinition.class);
    when(mockDefinition.getId()).thenReturn(id);
    when(mockDefinition.getCategory()).thenReturn(category);
    when(mockDefinition.getName()).thenReturn(name);
    when(mockDefinition.getKey()).thenReturn(key);
    when(mockDefinition.getDescription()).thenReturn(description);
    when(mockDefinition.getVersion()).thenReturn(version);
    when(mockDefinition.getResourceName()).thenReturn(resource);
    when(mockDefinition.getDeploymentId()).thenReturn(deploymentId);
    when(mockDefinition.getDiagramResourceName()).thenReturn(diagram);
    when(mockDefinition.isSuspended()).thenReturn(suspended);
    when(mockDefinition.hasStartFormKey()).thenReturn(startFormKey);
    return mockDefinition;
  }
}
