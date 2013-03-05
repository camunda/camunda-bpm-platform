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
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.impl.util.ReflectUtil;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngineController;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;

/**
 * <p>Deployment operation step responsible for starting a managed process engine 
 * inside the runtime container.</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class StartProcessEngineStep extends MBeanDeploymentOperationStep {
  
  /** the process engine Xml configuration passed in as a parameter to the operation step */
  protected final ProcessEngineXml processEngineXml;  
  
  public StartProcessEngineStep(ProcessEngineXml processEngineXml) {
    this.processEngineXml = processEngineXml;
  }

  public String getName() {    
    return "Start process engine " + processEngineXml.getName();
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final MBeanServiceContainer serviceContainer = operationContext.getServiceContainer();
    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);
    
    ClassLoader configurationClassloader = null;
    
    if(processApplication != null) {
      configurationClassloader = processApplication.getProcessApplicationClassloader();      
    } else {
      configurationClassloader = ProcessEngineConfiguration.class.getClassLoader();      
    }
    
    String configurationClassName = processEngineXml.getConfigurationClass();
    
    if(configurationClassName == null || configurationClassName.isEmpty()) {
      configurationClassName = StandaloneProcessEngineConfiguration.class.getName();
    }
    
    // create & instantiate configuration class    
    Class<? extends ProcessEngineConfiguration> configurationClass = loadProcessEngineConfigurationClass(configurationClassloader, configurationClassName);
    ProcessEngineConfiguration configuration = instantiateConfiguration(configurationClass);
    
    // set UUid generator
    // TODO: move this to configuration and use as default?
    ((ProcessEngineConfigurationImpl)configuration).setIdGenerator(new StrongUuidGenerator());
    
    // set configuration values
    String name = processEngineXml.getName();
    configuration.setProcessEngineName(name);
    
    String datasourceJndiName = processEngineXml.getDatasource();
    configuration.setDataSourceJndiName(datasourceJndiName);
    
    Map<String, String> properties = processEngineXml.getProperties();
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
    
    // start the process engine inside the container.
    JmxManagedProcessEngine managedProcessEngineService = new JmxManagedProcessEngineController(configuration);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, configuration.getProcessEngineName(), managedProcessEngineService);
    
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
    
    // stop the process engine
    operationContext.getServiceContainer()
      .stopService(ServiceTypes.PROCESS_ENGINE.getServiceName(processEngineXml.getName()));
    
  }

}
