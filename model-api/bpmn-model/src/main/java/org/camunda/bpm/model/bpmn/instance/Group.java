package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;

public interface Group extends Artifact {

  CategoryValue getCategory();

  void setCategory(CategoryValue categoryValue);

  @Override
  BpmnEdge getDiagramElement();
}