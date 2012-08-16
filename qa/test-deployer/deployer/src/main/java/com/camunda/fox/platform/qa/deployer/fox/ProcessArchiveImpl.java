package com.camunda.fox.platform.qa.deployer.fox;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.activiti.engine.impl.util.IoUtil;

/**
 *
 * @author nico.rehwaldt
 */
public class ProcessArchiveImpl implements ProcessArchive {
  
  private final ApplicationArchiveContext context;
  private final Set<String> processes;
  private final String name;

  private final String processEngineName; 
  private final HashMap<String, Object> properties;
  
  public ProcessArchiveImpl(ApplicationArchiveContext context, Set<String> processes, String processEngineName) {
    this.context = context;
    this.processes = processes;
    
    this.processEngineName = processEngineName;
    this.name = "test-process-archive-" + new BigInteger(30, new SecureRandom()).toString(Character.MAX_RADIX);
    
    this.properties = new HashMap<String, Object>();
    
    properties.put(PROP_IS_DELETE_UPON_UNDEPLOY, true);
    properties.put(PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS, false);
  }
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, byte[]> getProcessResources() {
    Map<String, byte[]> processData = new HashMap<String, byte[]>();
    for (String processDefinition: processes) {
      InputStream resource = getClassLoader().getResourceAsStream(processDefinition);
      byte[] process = IoUtil.readInputStream(resource, processDefinition);
      processData.put(processDefinition, process);
    }
    
    return processData;
  }
  
  @Override
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws Exception {
    return context.execute(callback);
  }

  @Override
  public ClassLoader getClassLoader() {
    return context.getClassLoader();
  }

  @Override
  public String getProcessEngineName() {
    return processEngineName;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
