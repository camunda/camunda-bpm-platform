package com.camunda.fox.platform.impl.service.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;
import org.camunda.bpm.application.spi.EmbeddedProcessApplication;
import org.camunda.bpm.application.spi.ProcessApplicationReference;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * 
 * @author Daniel Meyer
 */
public class DummyProcessArchive implements ProcessArchive {
  
  private final String name;
  private final String processEngineName;
  private final Map<String, byte[]> processResources;
  private Map<String, Object> properties =  new HashMap<String, Object>();

  public DummyProcessArchive(String name, String processEngineName, boolean scan, Map<String, byte[]> processResources, boolean isDelete) {
    ProcessApplicationReference reference = new ProcessApplicationReferenceImpl(new EmbeddedProcessApplication());
    this.name = name;
    this.processEngineName = processEngineName;
    properties.put(PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, scan);
    properties.put(PROP_PROCESS_APPLICATION_REFERENCE, reference);
    this.processResources = processResources;
  }

  public String getName() {
    return name;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public Map<String, byte[]> getProcessResources() {
    return processResources;
  }


  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {    
    return callback.execute();
  }

  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

}
