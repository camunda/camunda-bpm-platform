package com.camunda.fox.cycle.entity;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;

/**
 * Abstract super class for all entities.
 * 
 * @author nico.rehwaldt
 */
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
  
  private static final long serialVersionUID = 1L;
  
  @Id
  @GeneratedValue(strategy=GenerationType.TABLE, generator="cycleIdGenerator")  
  @TableGenerator(name="cycleIdGenerator", table="cy_id_table",  
                  pkColumnName="tablename", // TableID.TableName (value = table_name, test_table, etc.)  
                  valueColumnName="id", // TableID.ID (value = 1,2,3,etc.)  
                  allocationSize=1 // flush every 1 insert  
  )  
  private Long id;
  
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
}
