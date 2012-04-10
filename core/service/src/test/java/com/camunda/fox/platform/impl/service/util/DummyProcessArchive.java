package com.camunda.fox.platform.impl.service.util;

import java.util.Map;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * 
 * @author Daniel Meyer
 */
public class DummyProcessArchive implements ProcessArchive {
  
  private final String name;
  private final String processEngineName;
  private final boolean scan;
  private final Map<String, byte[]> processResources;
  private final boolean isDelete;

  public DummyProcessArchive(String name, String processEngineName, boolean scan, Map<String, byte[]> processResources, boolean isDelete) {
    this.name = name;
    this.processEngineName = processEngineName;
    this.scan = scan;
    this.processResources = processResources;
    this.isDelete = isDelete;
  }

  public String getName() {
    return name;
  }

  public String getProcessEngineName() {
    return processEngineName;
  }

  public boolean scanForProcessDefinitions() {
    return scan;
  }

  public Map<String, byte[]> getProcessResources() {
    return processResources;
  }

  public boolean isDeleteUponUndeploy() {
    return isDelete;
  }

  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {    
    return callback.execute();
  }

  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

}
