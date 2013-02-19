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
package org.camunda.bpm.container.impl.tomcat;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.impl.RuntimeContainerConfiguration;

import com.camunda.fox.platform.jobexecutor.impl.DefaultPlatformJobExecutor;

/**
 * Apache tomcat server listener responsible for starting and stopping configured process engines 
 * upon starting and stopping of the servlet container. 
 * 
 * @author Daniel Meyer
 *
 */
public class CamundaEngineServerListener implements LifecycleListener {
  
  private final static Logger log = Logger.getLogger(CamundaEngineServerListener.class.getName());
  
  protected DefaultPlatformJobExecutor platformJobExecutorService;
  protected RuntimeContainerConfiguration runtimeContainerConfiguration;

  protected ProcessEngine processEngine;
    
  public void lifecycleEvent(LifecycleEvent event) {

    if (Lifecycle.START_EVENT.equals(event.getType())) {

      runtimeContainerConfiguration = RuntimeContainerConfiguration.getINSTANCE();
      
      startPlatformJobexecutorService(event);
      startProcessEngineService(event);
      
    } else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
      
      processEngine.close();
      
    }
   
  }

  protected void startPlatformJobexecutorService(LifecycleEvent event) {
    
    platformJobExecutorService = new DefaultPlatformJobExecutor();
    platformJobExecutorService.start();
    
    bindContainerJobexecutorService(event);
    
  }
  
  protected void startProcessEngineService(LifecycleEvent event) {
    
    StandardServer server = (StandardServer) event.getSource();    
    runtimeContainerConfiguration.setRuntimeContainerName(server.getServerInfo());
    log.info("camunda BPM platform running in container "+runtimeContainerConfiguration.getRuntimeContainerName());
        
    // create JNDI bindings
    bindProcessEngineService(event);
    
    // start in-memory process engine:
    processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
    
  }
  
  protected void bindContainerJobexecutorService(LifecycleEvent event) {
    
    final String jobExecutorBeanName = "PlatformJobExecutorBean!com.camunda.fox.platform.jobexecutor.api.JobExecutorService";
    
    try {      
      
      StandardServer server = (StandardServer) event.getSource();
      Context bindingContext = server.getGlobalNamingContext();
      
      // lookup the context
      bindingContext = getOrCreateSubContext(bindingContext, "global");
      bindingContext = getOrCreateSubContext(bindingContext, "camunda-fox-platform");
      bindingContext = getOrCreateSubContext(bindingContext, "job-executor");           
            
      // bind the jobexecutor service 
      bindingContext.bind(jobExecutorBeanName, platformJobExecutorService);
            
    } catch (NamingException e) {
      log.log(Level.SEVERE, "Unable to bind platform job executor service in global naming context.", e);
      
    }    
    
  }
  
  protected void bindProcessEngineService(LifecycleEvent event) {
    
    final String processEngineServiceName = "DefaultProcessEngineService!com.camunda.fox.platform.api.ProcessEngineService";   
        
    try {      
      
      StandardServer server = (StandardServer) event.getSource();
      Context bindingContext = server.getGlobalNamingContext();
      
      // lookup the context
      bindingContext = getOrCreateSubContext(bindingContext, "global");
      bindingContext = getOrCreateSubContext(bindingContext, "camunda-fox-platform");
      bindingContext = getOrCreateSubContext(bindingContext, "process-engine");           
            
      // bind the services 
      bindingContext.bind(processEngineServiceName, BpmPlatform.getProcessEngineService());
      
      log.info("the legacy bindings for the fox platform services are as follows: \n\n"
          + "        java:global/camunda-fox-platform/process-engine/"
          + processEngineServiceName + "\n");
      
    } catch (NamingException e) {
      log.log(Level.SEVERE, "Unable to bind platform services in global naming context.", e);
      
    }
    
  }

  protected Context getOrCreateSubContext(Context rootNamingContext, String name) throws NamingException {
    Context context;
    try {
      // check whether the context already exists
      context = (Context) rootNamingContext.lookup(name);
    } catch (NamingException e) {
      // this means that the context does not exist
      context = rootNamingContext.createSubcontext(name);        
    }
    return context;
  }

}
