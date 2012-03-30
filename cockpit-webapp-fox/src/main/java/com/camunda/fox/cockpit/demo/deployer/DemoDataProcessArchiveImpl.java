package com.camunda.fox.cockpit.demo.deployer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.cockpit.demo.DemoDataDeployer;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

public class DemoDataProcessArchiveImpl implements ProcessArchive {

  private final ProcessArchiveContextExecutor archiveContextExecutor;

  public DemoDataProcessArchiveImpl(ProcessArchiveContextExecutor archiveContextExecutor) {
    this.archiveContextExecutor = archiveContextExecutor;
  }
  
  public String getProcessEngineName() {
    return null;  // = use default
  }
  
  public boolean scanForProcessDefinitions() {
    return false;
  }
  
  @Override
  public String getName() {
    return "COCKPIT_DEMO_PROCESSES";
  }

  @Override
  public Map<String, byte[]> getProcessResources() {
    Map<String, byte[]> processes = new HashMap<String, byte[]>();
    
    for (String processDefinition : DemoDataDeployer.DEMO_PROCESSES) {
      InputStream resource = getClassLoader().getResourceAsStream(processDefinition);
      byte[] process = IoUtil.readInputStream(resource, processDefinition);
      processes.put(processDefinition, process);
    }
    
    return processes;
  }

  @Override
  public boolean isDeleteUponUndeploy() {
    return false;
  }

  @Override
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {
    return archiveContextExecutor.executeWithinContext(callback);
  }

  @Override
  public ClassLoader getClassLoader() {
    return DemoDataProcessArchiveImpl.class.getClassLoader();
  }

}
