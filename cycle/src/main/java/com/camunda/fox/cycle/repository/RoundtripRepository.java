package com.camunda.fox.cycle.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.Roundtrip;

/**
 * Roundtrip repository 
 * 
 * 
 * @author nico.rehwaldt
 */
@Repository
public class RoundtripRepository {

  @PersistenceContext
  private EntityManager em;
  
  public boolean isNameValid(String name) {
    long count = em.createQuery("SELECT COUNT(r) FROM Roundtrip r WHERE r.name = :name", Long.class)
                   .setParameter("name", name)
                   .getSingleResult();
    
    return count == 0;
  }
  
  @Transactional
  public Roundtrip saveAndFlush(Roundtrip roundtrip) {
    em.persist(roundtrip);
    em.flush();
    
    return roundtrip;
  }

  public Roundtrip findOne(long id) {
    return em.find(Roundtrip.class, id);
  }

  public List<Roundtrip> findAll() {
    return em.createQuery("SELECT r FROM Roundtrip r", Roundtrip.class).getResultList();
  }
}
