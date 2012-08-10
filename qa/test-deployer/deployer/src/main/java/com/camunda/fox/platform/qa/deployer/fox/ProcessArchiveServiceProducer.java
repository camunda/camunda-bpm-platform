package com.camunda.fox.platform.qa.deployer.fox;

import com.camunda.fox.platform.api.ProcessArchiveService;
import com.camunda.fox.platform.qa.deployer.exception.InitializationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class ProcessArchiveServiceProducer {
  
  private static final Logger log = Logger.getLogger(ProcessArchiveServiceProducer.class.getName());
  
  private static final String PROCESS_ARCHIVE_SERVICE_JNDI_BINDING = 
    "java:" + 
    "global/camunda-fox-platform/process-engine/" + 
    "PlatformService!com.camunda.fox.platform.api.ProcessArchiveService";
  
  @Inject
  @ApplicationScoped
  private InstanceProducer<ProcessArchiveService> processArchiveService;
  
  @Inject
  private Instance<Context> initialContext;
  
  public void beforeClass(@Observes(precedence=100) BeforeClass event) {
    
    try {
      ProcessArchiveService service = (ProcessArchiveService) initialContext.get().lookup(PROCESS_ARCHIVE_SERVICE_JNDI_BINDING);
      processArchiveService.set(service);
      
      log.log(Level.INFO, "Looked up process archive service: {0}", service);
    } catch (NamingException e) {
      throw new InitializationException("Failed to look up process archive service", e);
    }
  }
}
