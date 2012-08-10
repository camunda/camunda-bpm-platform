package com.camunda.fox.platform.qa.deployer.fox;

import com.camunda.fox.platform.qa.deployer.event.BeforeFoxTest;
import com.camunda.fox.platform.qa.deployer.exception.InitializationException;
import com.camunda.fox.platform.qa.deployer.war.ApplicationArchiveContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class ApplicationArchiveContextProducer {
  
  private static final Logger log = Logger.getLogger(ApplicationArchiveContextProducer.class.getName());
  
  @Inject
  @TestScoped
  private InstanceProducer<ApplicationArchiveContext> applicationArchiveContext;
  
  @Inject
  private Instance<Context> initialContext;
  
  public void beforeTest(@Observes(precedence=400) BeforeFoxTest event) {
    produceArchiveContext();
  }
  
  protected void produceArchiveContext() {
    String contextJndiName = getArchiveContextJndiName();
    
    try {
      ApplicationArchiveContext context = (ApplicationArchiveContext) initialContext.get().lookup("java:module/ApplicationArchiveContextImpl");
      applicationArchiveContext.set(context);
      
      log.log(
        Level.FINE, 
        "Looked up application archive context {0} for archive named {1}", 
        new Object[] { context, context.getAppName(), context.getClassLoader() });
      
    } catch (NamingException e) {
      throw new InitializationException("Failed to look up application archive context at \n\t" + contextJndiName, e);
    }
  }
  
  public String getArchiveContextJndiName() {
    // TODO: try list of jndi names 
    // (see https://github.com/arquillian/arquillian-core/blob/master/testenrichers/ejb/src/main/java/org/jboss/arquillian/testenricher/ejb/EJBInjectionEnricher.java)
    return "java:module/ApplicationArchiveContextImpl";
  }
}
