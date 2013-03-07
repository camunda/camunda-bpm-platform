package org.camunda.bpm.cycle.repository;

import org.camunda.bpm.cycle.entity.Roundtrip;
import org.springframework.stereotype.Repository;


/**
 * Roundtrip repository 
 * 
 * @author nico.rehwaldt
 */
@Repository
public class RoundtripRepository extends AbstractRepository<Roundtrip> {
  
  public boolean isNameAvailable(String name) {
    long count = em.createQuery("SELECT COUNT(r) FROM Roundtrip r WHERE r.name = :name", Long.class)
                   .setParameter("name", name)
                   .getSingleResult();
    
    return count == 0;
  }
}
