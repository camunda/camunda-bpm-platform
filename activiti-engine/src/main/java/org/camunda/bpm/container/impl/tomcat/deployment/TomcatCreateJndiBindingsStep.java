package org.camunda.bpm.container.impl.tomcat.deployment;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.activiti.engine.ActivitiException;
import org.apache.catalina.core.StandardServer;
import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.ProcessEngineService;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;

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
  
  public final String PROCESS_ENGINE_SERVICE_NAME = "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";   
  
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
      bindingContext = getOrCreateSubContext(bindingContext, "camunda-fox-platform");
      bindingContext = getOrCreateSubContext(bindingContext, "process-engine");           
            
      // bind the service 
      bindingContext.bind(PROCESS_ENGINE_SERVICE_NAME, BpmPlatform.getProcessEngineService());
      
      LOGGER.info("the legacy bindings for the fox platform services are as follows: \n\n"
          + "        java:global/camunda-fox-platform/process-engine/"
          + PROCESS_ENGINE_SERVICE_NAME + "\n");
      
    } catch (NamingException e) {
      throw new ActivitiException("Unable to bind platform service in global naming context.", e);      
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
