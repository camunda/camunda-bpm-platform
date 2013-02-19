package org.camunda.bpm.application.impl.deployment.metadata.spi;

import java.util.Collections;
import java.util.List;

/**
 * <p>Java API representation of the {@link ProcessesXml} Metadata.</p>
 * 
 * @author Daniel Meyer
 *
 */
public interface ProcessesXml {
  
  /**
   * @return A {@link List} of {@link ProcessEngineXml} Metadata Items representing process engine configurations. 
   */
  public List<ProcessEngineXml> getProcessEngines();
  
  /**
   * @return A {@link List} of {@link ProcessArchiveXml} Metadata Items representing process archive deployments. 
   */
  public List<ProcessArchiveXml> getProcessArchives();

  /** 
   * <p>Constant representing the empty processes.xml</p>
   */
  public final static ProcessesXml EMPTY_PROCESSES_XML = new ProcessesXml() {
    
    public List<ProcessEngineXml> getProcessEngines() {
      return Collections.emptyList();
    }
    
    public List<ProcessArchiveXml> getProcessArchives() {
      return Collections.emptyList();
    }
    
  };
  
}
