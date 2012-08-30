package com.camunda.fox.cycle.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.BpmnDiagram;

/**
 *
 * @author nico.rehwaldt
 */
@Repository
public class BpmnDiagramRepository extends AbstractRepository<BpmnDiagram> {
  
  public List<String> findAllModelerNames() {
//    List<String> modelerNames = new ArrayList<String>();
//    modelerNames.add("BOC Adonis");
//    modelerNames.add("Signavio Process Editor");
//    modelerNames.add("Cubetto");
//    modelerNames.add("Yaoqiang");
//    modelerNames.add("Visual Paradigm");
//    modelerNames.add("Enterprise Architect");
//    modelerNames.add("BPMN 2.0 Modeler for Visio");
//    modelerNames.add("ibo Prometheus");
//    modelerNames.add("Fox designer");
//
//    return modelerNames;
    return em.createQuery("SELECT DISTINCT b.modeller FROM BpmnDiagram b", String.class).getResultList();
  }
}
