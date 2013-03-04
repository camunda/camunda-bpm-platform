package org.camunda.bpm.engine.rest.dto.repository;


public class ProcessDefinitionDiagramDto {
  
  private String id;
  private String bpmn20Xml;
  
  public String getId() {
    return id;
  }

  public String getBpmn20Xml() {
    return bpmn20Xml;
  }
  
  public static ProcessDefinitionDiagramDto create(String id, String bpmn20Xml) {
    ProcessDefinitionDiagramDto processDefinitionDiagramDto = new ProcessDefinitionDiagramDto();
    processDefinitionDiagramDto.id = id;
    processDefinitionDiagramDto.bpmn20Xml = bpmn20Xml;
    
    return processDefinitionDiagramDto;
  }
  
}
