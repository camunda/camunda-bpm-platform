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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.container.impl.jmx.JmxRuntimeContainerDelegate.ServiceTypes;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngine;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngineController;
import org.camunda.bpm.container.impl.metadata.PropertyHelper;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.jobexecutor.FoxFailedJobCommandFactory;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.impl.util.ReflectUtil;

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
    
    ClassLoader classLoader = null;
    
    if(processApplication != null) {
      classLoader = processApplication.getProcessApplicationClassloader();      
    } 
    
    String configurationClassName = processEngineXml.getConfigurationClass();
    
    if(configurationClassName == null || configurationClassName.isEmpty()) {
      configurationClassName = StandaloneProcessEngineConfiguration.class.getName();
    }
    
    // create & instantiate configuration class    
    Class<? extends ProcessEngineConfigurationImpl> configurationClass = loadClass(configurationClassName, classLoader, ProcessEngineConfigurationImpl.class);
    ProcessEngineConfigurationImpl configuration = createInstance(configurationClass);
    
    // set UUid generator
    // TODO: move this to configuration and use as default?
    ProcessEngineConfigurationImpl configurationImpl = (ProcessEngineConfigurationImpl)configuration;
    configurationImpl.setIdGenerator(new StrongUuidGenerator());
    
    configureCustomRetryStrategy(configurationImpl);
    
    // set configuration values
    String name = processEngineXml.getName();
    configuration.setProcessEngineName(name);
    
    String datasourceJndiName = processEngineXml.getDatasource();
    configuration.setDataSourceJndiName(datasourceJndiName);
    
    // apply properties
    Map<String, String> properties = processEngineXml.getProperties();
    PropertyHelper.applyProperties(configuration, properties);
    
    // instantiate plugins:
    configurePlugins(configuration, processEngineXml, classLoader);
    
    if(processEngineXml.getJobAcquisitionName() != null && !processEngineXml.getJobAcquisitionName().isEmpty()) {
      JobExecutor jobExecutor = getJobExecutorService(serviceContainer);
      if(jobExecutor == null) {
        throw new ProcessEngineException("Cannot find referenced job executor with name '"+processEngineXml.getJobAcquisitionName()+"'");
      }
      
      // set JobExecutor on process engine
      configurationImpl.setJobExecutor(jobExecutor);
    }
        
    // start the process engine inside the container.
    JmxManagedProcessEngine managedProcessEngineService = new JmxManagedProcessEngineController(configuration);
    serviceContainer.startService(ServiceTypes.PROCESS_ENGINE, configuration.getProcessEngineName(), managedProcessEngineService);
            
  }

  protected void configureCustomRetryStrategy(ProcessEngineConfigurationImpl configurationImpl) {
    // add support for custom Retry strategy
    // TODO: decide whether this should be moved  to configuration or to plugin
    List<BpmnParseListener> customPostBPMNParseListeners = configurationImpl.getCustomPostBPMNParseListeners();
    if(customPostBPMNParseListeners==null) {
      customPostBPMNParseListeners = new ArrayList<BpmnParseListener>();
      configurationImpl.setCustomPostBPMNParseListeners(customPostBPMNParseListeners);
    }    
    customPostBPMNParseListeners.add(new FoxFailedJobParseListener());    
    configurationImpl.setFailedJobCommandFactory(new FoxFailedJobCommandFactory());
  }

  /**
   * <p>Instantiates and applies all {@link ProcessEnginePlugin}s defined in the processEngineXml
   */
  protected void configurePlugins(ProcessEngineConfigurationImpl configuration, ProcessEngineXml processEngineXml, ClassLoader classLoader) {
    
    for (ProcessEnginePluginXml pluginXml : processEngineXml.getPlugins()) {
      // create plugin instance
      Class<? extends ProcessEnginePlugin> pluginClass = loadClass(pluginXml.getPluginClass(), classLoader, ProcessEnginePlugin.class);
      ProcessEnginePlugin plugin = createInstance(pluginClass);
      
      // apply configured properties
      Map<String, String> properties = pluginXml.getProperties();
      PropertyHelper.applyProperties(plugin, properties);
      
      // add to configuration
      configuration.getProcessEnginePlugins().add(plugin);
    }
    
  }

  protected JobExecutor getJobExecutorService(final MBeanServiceContainer serviceContainer) {
    // lookup container managed job executor
    String jobAcquisitionName = processEngineXml.getJobAcquisitionName();
    JobExecutor jobExecutor = serviceContainer.getServiceValue(ServiceTypes.JOB_EXECUTOR, jobAcquisitionName);
    return jobExecutor;
  }
  
  protected <T> T createInstance(Class<? extends T> clazz) {
    try {
      return clazz.newInstance();
      
    } catch (InstantiationException e) {
      throw new ProcessEngineException("Could not instantiate class", e);
    } catch (IllegalAccessException e) {
      throw new ProcessEngineException("IllegalAccessException while instantiating class", e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <T> Class<? extends T> loadClass(String className, ClassLoader customClassloader, Class<T> clazz) {
    try {
      if(customClassloader != null) {
        return (Class<? extends T>) customClassloader.loadClass(className);
      }else {
        return (Class<? extends T>) ReflectUtil.loadClass(className);
      }
      
    } catch (ClassNotFoundException e) {
      throw new ProcessEngineException("Could not load configuration class", e);
      
    } catch (ClassCastException e) {
      throw new ProcessEngineException("Custom class of wrong type. Must extend "+clazz.getName(), e);
      
    }
  }

}
