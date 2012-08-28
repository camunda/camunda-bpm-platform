package com.camunda.fox.cycle.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.util.ClassUtil;


/**
 * Abstract repository which holds the base information
 * 
 * @author nico.rehwaldt
 */
public class AbstractRepository<T> {

  @PersistenceContext
  protected EntityManager em;
  
  private Class<T> entityClass;
  
  @SuppressWarnings("unchecked")
  protected AbstractRepository() {
    this.entityClass = (Class<T>) ClassUtil.extractParameterizedType(getClass());
  }
  
  @Transactional
  public T saveAndFlush(T entity) {
    
    // Persist entity unless it exists
    if (!em.contains(entity)) {
      em.persist(entity);
    }
    
    em.flush();
    return entity;
  }

  public T findById(long id) {
    return em.find(entityClass, id);
  }
  
  public List<T> findAll() {
    return em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass).getResultList();
  }
}
