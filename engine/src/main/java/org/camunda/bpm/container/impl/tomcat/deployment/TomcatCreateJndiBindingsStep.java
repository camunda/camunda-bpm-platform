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
package org.camunda.bpm.container.impl.tomcat.deployment;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.catalina.core.StandardServer;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.engine.ProcessEngineException;

/**
 * <p>Deployment operation step responsible for creating the platform JNDI bindings on apache tomcat</p>
 * 
 * <p>This binds the {@link ProcessEngineService} as a global naming resource</p> 
 * 
 * @author Daniel Meyer
 *
 */
public class TomcatCreateJndiBindingsStep extends MBeanDeploymentOperationStep {
  
  public final static Logger LOGGER = Logger.getLogger(TomcatCreateJndiBindingsStep.class.getName());
  
  public final String PROCESS_ENGINE_SERVICE_NAME = "ProcessEngineService!org.camunda.bpm.ProcessEngineService";
  public final String PROCESS_APPLICATION_SERVICE_NAME = "ProcessApplicationService!org.camunda.bpm.ProcessApplicationService";
  
  protected Context bindingContext;

  public String getName() {
    return "Creating JNDI bindings";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    final StandardServer server = operationContext.getAttachment(TomcatAttachments.SERVER);
        
    try {            
      bindingContext = server.getGlobalNamingContext();
      
      // lookup the context
      bindingContext = getOrCreateSubContext(bindingContext, "global");
      bindingContext = getOrCreateSubContext(bindingContext, "camunda-bpm-platform");
      bindingContext = getOrCreateSubContext(bindingContext, "process-engine");           
            
      // bind the services 
      bindingContext.bind(PROCESS_ENGINE_SERVICE_NAME, BpmPlatform.getProcessEngineService());
      bindingContext.bind(PROCESS_APPLICATION_SERVICE_NAME, BpmPlatform.getProcessApplicationService());
      
//      LOGGER.info("the JNDI bindings for the BPM platform services are as follows: \n\n"
//          + "        java:global/camunda-bpm-platform/process-engine/"+ PROCESS_ENGINE_SERVICE_NAME + "\n"
//          + "        java:global/camunda-bpm-platform/process-engine/"+ PROCESS_APPLICATION_SERVICE_NAME + "\n");
      
    } catch (NamingException e) {
      throw new ProcessEngineException("Unable to bind bpm platform services in global naming context.", e);      
    }
    
  }
  
  public void cancelOperationStep(MBeanDeploymentOperation operationContext) {
    
    if(bindingContext != null) {
      try {
        
        bindingContext.unbind(PROCESS_ENGINE_SERVICE_NAME);
        
      } catch(NamingException e) {
        // effectively  swallow
        LOGGER.log(Level.FINEST, "Could not remove binding for "+PROCESS_ENGINE_SERVICE_NAME, e);
      }
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
