package com.camunda.fox.cycle.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Abstract super class for all entities.
 * 
 * @author nico.rehwaldt
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
  
	@Id
  @GeneratedValue
  private Long id;
  
  public Long getId() {
    return id;
  }
}
