package com.camunda.fox.cycle.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Roundtrip entity
 * 
 */
@Entity
@Table(name = "cy_roundtrip")
public class Roundtrip implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
	@Id
  @GeneratedValue
  private Long id;
  
	private String name;
	
	public Roundtrip() { }
	
	public Roundtrip(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

  public Long getId() {
    return id;
  }
  
	@Override
	public String toString() {
		return "Roundtrip [name=" + name + "]";
  }
}
