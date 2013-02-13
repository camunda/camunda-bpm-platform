package com.camunda.fox.cycle.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
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
public class Roundtrip extends AbstractEntity {
  
  private static final long serialVersionUID = 1L;
  
  public enum SyncMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
  }
  
  @Column(unique=true)
	private String name;
	
  @Temporal(TemporalType.TIMESTAMP)
  private Date lastSync;
  
  @Enumerated(EnumType.STRING)
  private SyncMode lastSyncMode;
  
  @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
  private BpmnDiagram leftHandSide;
  
  @OneToOne(cascade=CascadeType.ALL, orphanRemoval=true)
  private BpmnDiagram rightHandSide;

  public Roundtrip() { }

  public Roundtrip(Long id, String name) {
    this.setId(id);
    this.name = name;
  }

  public Roundtrip(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public SyncMode getLastSyncMode() {
    return lastSyncMode;
  }

  public void setLastSyncMode(SyncMode lastSyncMode) {
    this.lastSyncMode = lastSyncMode;
  }

  @Override
  public String toString() {
    return "Roundtrip [name=" + name + ", lastSync=" + lastSync + ", lastSyncMode=" + lastSyncMode + "]";
  }

}
