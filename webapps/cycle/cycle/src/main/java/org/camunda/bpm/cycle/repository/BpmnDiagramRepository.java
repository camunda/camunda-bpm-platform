package org.camunda.bpm.cycle.repository;

import java.util.List;

import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.springframework.stereotype.Repository;


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
