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
    return em.createQuery("SELECT DISTINCT b.modeler FROM BpmnDiagram b", String.class).getResultList();
  }
}
