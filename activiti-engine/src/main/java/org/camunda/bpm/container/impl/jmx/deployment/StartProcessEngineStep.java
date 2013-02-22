/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.container.impl.jmx.deployment;

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESS_APPLICATION;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.impl.util.ReflectUtil;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

/**
 * <p>Deployment operation step responsible for starting a process engine.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class StartProcessEngineStep extends MBeanDeploymentOperationStep {
  
  protected final ProcessEngineXml parsedProcessEngine;
  
  protected ProcessEngine createdEngine;

  public StartProcessEngineStep(ProcessEngineXml parsedProcessEngine) {
    this.parsedProcessEngine = parsedProcessEngine;
  }

  public String getName() {    
    return "Start process engine " + parsedProcessEngine.getName();
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final ProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);
    
    ClassLoader configurationClassloader = null;
    
    if(processApplication != null) {
      configurationClassloader = processApplication.getProcessApplicationClassloader();
      
    } else {
      configurationClassloader = ProcessEngineConfiguration.class.getClassLoader();
      
    }
    
    String configurationClassName = parsedProcessEngine.getConfigurationClass();
    
    if(configurationClassName == null || configurationClassName.isEmpty()) {
      configurationClassName = StandaloneProcessEngineConfiguration.class.getName();
    }
    
    // create & instantiate configuration class    
    Class<? extends ProcessEngineConfiguration> configurationClass = loadProcessEngineConfigurationClass(configurationClassloader, configurationClassName);
    ProcessEngineConfiguration configuration = instantiateConfiguration(configurationClass);
    
    // engine started through the container infrastructure are always container-managed
    configuration.setContainerManaged(true);
    
    // set UUid generator
    // TODO: move this to configuration and use as default?
    ((ProcessEngineConfigurationImpl)configuration).setIdGenerator(new StrongUuidGenerator());
    
    // set configuration values
    String name = parsedProcessEngine.getName();
    configuration.setProcessEngineName(name);
    
    String datasourceJndiName = parsedProcessEngine.getDatasource();
    configuration.setDataSourceJndiName(datasourceJndiName);
    
    Map<String, String> properties = parsedProcessEngine.getProperties();
    for (Entry<String, String> property : properties.entrySet()) {
      
      Method setter = ReflectUtil.getSetter(property.getKey(), configurationClass, String.class);
      if(setter != null) {
        try {
          setter.invoke(configuration, property.getValue());
        } catch (Exception e) {
          throw new ActivitiException("Could not set value for property '"+property.getKey(), e);
        }
      } else {
        throw new ActivitiException("Could not find setter for property '"+property.getKey());
      }
      
    }
    
    // finally build process engine    
    createdEngine = configuration.buildProcessEngine();    
  }

  protected ProcessEngineConfiguration instantiateConfiguration(Class<? extends ProcessEngineConfiguration> configurationClass) {
    try {
      return configurationClass.newInstance();
      
    } catch (InstantiationException e) {
      throw new ActivitiException("Could not instantiate configuration class", e);
    } catch (IllegalAccessException e) {
      throw new ActivitiException("IllegalAccessException while instantiating configuration class", e);
    }
  }

  @SuppressWarnings("unchecked")
  protected Class<? extends ProcessEngineConfiguration> loadProcessEngineConfigurationClass(ClassLoader processApplicationClassloader, String processEngineConfigurationClassName) {
    try {
      return (Class<? extends ProcessEngineConfiguration>) processApplicationClassloader.loadClass(processEngineConfigurationClassName);
    } catch (ClassNotFoundException e) {
      throw new ActivitiException("Could not load process engine configuration class",e);
    }
  }

  public void cancelOperationStep(MBeanDeploymentOperation operationContext) {
    // if this process engine was successfully created, close it.
    if(createdEngine != null) {
      createdEngine.close();
    }
  }

}
