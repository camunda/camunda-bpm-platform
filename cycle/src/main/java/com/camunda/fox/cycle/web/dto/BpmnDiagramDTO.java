package com.camunda.fox.cycle.web.dto;

import com.camunda.fox.cycle.entity.BpmnDiagram;

/**
 * This is a data object which exposes a {@link BpmnDiagram} to the client via rest.
 * 
 * @author nico.rehwaldt
 */
public class BpmnDiagramDTO {
  
  private Long id;
  private String modeller;
  private String diagramPath;

  private BpmnDiagramDTO(BpmnDiagram diagram) {
    this.id = diagram.getId();
    this.modeller = diagram.getModeller();
    this.diagramPath = diagram.getDiagramPath();
  }

  public Long getId() {
    return id;
  }

  public String getModeller() {
    return modeller;
  }
  
  public String getDiagramPath() {
    return diagramPath;
  }
  
  /**
   * Wraps a bpmn diagram as a data object
   * @param diagram
   * @return 
   */
  public static BpmnDiagramDTO wrap(BpmnDiagram diagram) {
    return new BpmnDiagramDTO(diagram);
  }
}
