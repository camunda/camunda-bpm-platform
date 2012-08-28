package com.camunda.fox.cycle.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents a roundtrip 
 * 
 * @author nico.rehwaldt@camunda.com
 */
@Entity
@Table(name = "cy_roundtrip")
public class Roundtrip implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
	@Id
  @GeneratedValue
  private Long id;
  
  @Column(unique=true)
	private String name;
	
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastSync;
  
  @ManyToOne
  private BpmnDiagram leftHandSide;
  
  @ManyToOne
  private BpmnDiagram rightHandSide;
  
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

  public Date getLastSync() {
    return lastSync;
  }
  
  public void setLastSync(Date lastSync) {
    this.lastSync = lastSync;
  }

  public BpmnDiagram getLeftHandSide() {
    return leftHandSide;
  }

  public void setLeftHandSide(BpmnDiagram leftHandSide) {
    this.leftHandSide = leftHandSide;
  }

  public BpmnDiagram getRightHandSide() {
    return rightHandSide;
  }

  public void setRightHandSide(BpmnDiagram rightHandSide) {
    this.rightHandSide = rightHandSide;
  }

	@Override
	public String toString() {
		return "Roundtrip[id=" + id + ", name=" + name + "]";
  }
}
