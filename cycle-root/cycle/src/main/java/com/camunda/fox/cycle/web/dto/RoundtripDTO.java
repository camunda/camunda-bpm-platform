package com.camunda.fox.cycle.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.camunda.fox.cycle.entity.Roundtrip;

/**
 * This is a data object which exposes a {@link Roundtrip} to the client via rest.
 * 
 * It is needed to ensure clear boundaries between a JPA managed entity (the {@link Roundtrip}) 
 * and the json serialization mechanism. 
 * 
 * @author nico.rehwaldt
 */
public class RoundtripDTO {

  private Long id;
  
  private String name;
  
  private String sourceModel;
  
  private String targetModel;

  public RoundtripDTO() {
  }
  
  public RoundtripDTO(Roundtrip r) {
    this.id = r.getId();
    this.name = r.getName();
    
    // TODO: Update models?
  }
  
  public RoundtripDTO(Long id, String name, String sourceModel, String targetModel) {
    this.id = id;
    this.name = name;
    this.sourceModel = sourceModel;
    this.targetModel = targetModel;
  }
  
  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the sourceModel
   */
  public String getSourceModel() {
    return sourceModel;
  }

  /**
   * @param sourceModel the sourceModel to set
   */
  public void setSourceModel(String sourceModel) {
    this.sourceModel = sourceModel;
  }

  /**
   * @return the targetModel
   */
  public String getTargetModel() {
    return targetModel;
  }

  /**
   * @param targetModel the targetModel to set
   */
  public void setTargetModel(String targetModel) {
    this.targetModel = targetModel;
  }
  
  /**
   * Wraps a roundtrip as a data object
   * @param roundtrip
   * @return 
   */
  public static RoundtripDTO wrap(Roundtrip roundtrip) {
    return new RoundtripDTO(roundtrip);
  }
  
  /**
   * Wraps a list of roundtrips as a list of the respective roundtrip data objects
   * 
   * @param trackers
   * @return 
   */
  public static List<RoundtripDTO> wrapAll(List<Roundtrip> trackers) {
    List<RoundtripDTO> dtos = new ArrayList<RoundtripDTO>();
    for (Roundtrip t: trackers) {
      dtos.add(new RoundtripDTO(t));
    }
    
    return dtos;
  }
}
