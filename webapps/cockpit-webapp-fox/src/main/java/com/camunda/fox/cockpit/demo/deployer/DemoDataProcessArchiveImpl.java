package com.camunda.fox.cockpit.demo.deployer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.cockpit.demo.DemoDataDeployer;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

public class DemoDataProcessArchiveImpl implements ProcessArchive {

  private final ProcessArchiveContextExecutor archiveContextExecutor;
  private final Map<String, Object> properties = new HashMap<String, Object>();
  private String processArchiveName;

  public DemoDataProcessArchiveImpl(ProcessArchiveContextExecutor archiveContextExecutor) {
    this.archiveContextExecutor = archiveContextExecutor;
    properties.put(PROP_IS_DELETE_UPON_UNDEPLOY, false);
    properties.put(PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, false);
  }
  
  public String getProcessEngineName() {
    return null;  // = use default
  }
    
  public String getName() {
    if (processArchiveName == null) {
      processArchiveName = "cockpit-demo-processes" + new Random().nextInt(50000);
    }
    return processArchiveName;
  }

  public Map<String, byte[]> getProcessResources() {
    Map<String, byte[]> processes = new HashMap<String, byte[]>();
    
    for (String processDefinition : DemoDataDeployer.DEMO_PROCESSES) {
      InputStream resource = getClassLoader().getResourceAsStream(processDefinition);
      byte[] process = IoUtil.readInputStream(resource, processDefinition);
      processes.put(processDefinition, process);
    }
    
    return processes;
  }

  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {
    return archiveContextExecutor.executeWithinContext(callback);
  }

  public ClassLoader getClassLoader() {
    return DemoDataProcessArchiveImpl.class.getClassLoader();
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

}
