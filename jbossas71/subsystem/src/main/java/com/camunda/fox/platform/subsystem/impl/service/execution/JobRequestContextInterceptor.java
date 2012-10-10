package com.camunda.fox.platform.subsystem.impl.service.execution;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.jboss.weld.context.ejb.EjbLiteral;
import org.jboss.weld.context.ejb.EjbRequestContext;

import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>ProcessArchiveCallback allowing us to execute a job in the context of 
 * the owning ProcessArchive while activating the CDI RequestScope.</p>
 * 
 * <p>See: org.jboss.as.weld.ejb.EjbRequestScopeActivationInterceptor</p>
 * 
 * @author Daniel Meyer
 *
 */
public class JobRequestContextInterceptor implements ProcessArchiveCallback<Void> {
  
  protected final CommandContext commandContext;
  protected final Command<?> executeJobCommand;

  public JobRequestContextInterceptor(CommandContext commandContext, Command<?> executeJobCommand) {
    this.commandContext = commandContext;
    this.executeJobCommand = executeJobCommand;
  }

  public Void execute() {
    try {
    
      // attempt looking up the bean manager
      BeanManager bm = InitialContext.doLookup("java:comp/BeanManager");
      proceedWithRequestContext(bm);

    } catch (NamingException e) {      
      // ignore. (not a CDI deployment)
      
      proceedWithoutRequestContext();
    }

    return null;
  }

  protected void proceedWithRequestContext(BeanManager beanManager) {
    final Bean< ? > bean = beanManager.resolve(beanManager.getBeans(EjbRequestContext.class, EjbLiteral.INSTANCE));
    final CreationalContext< ? > ctx = beanManager.createCreationalContext(bean);
    EjbRequestContext requestContext = (EjbRequestContext) beanManager.getReference(bean, EjbRequestContext.class, ctx);    
    final InvocationContext invocationContext = new JobInvocationContext();
    
    try {
      // if this call succeeds then the context is already active
      // HEMERA-2789
      beanManager.getContext(RequestScoped.class);
      executeJobCommand.execute(commandContext);
      return;
    } catch (ContextNotActiveException exception) {
    }
    
    try {
      // activate the CDI request context
      requestContext.associate(invocationContext);
      requestContext.activate();

      // now actually execute the job
      executeJobCommand.execute(commandContext);
      return;
      
    } finally {
      // deactivate the CDI request context
      requestContext.invalidate();
      requestContext.deactivate();
      requestContext.dissociate(invocationContext);              
    }    
  }

  protected void proceedWithoutRequestContext() {
    executeJobCommand.execute(commandContext);    
  }
  
  /**
   * InvocationContext that only supports Variables (ContextData).
   * This is used to store RequestScoped Bean instances.  
   */
  public static class JobInvocationContext implements InvocationContext {
    
    protected final Map<String, Object> contextData = new HashMap<String, Object>();

    public Map<String, Object> getContextData() {
      return contextData;
    }

    public Object getTarget() {
      // unsupported
      return null;
    }

    public Method getMethod() {
      // unsupported
      return null;
    }

    public Object[] getParameters() {
      // unsupported
      return null;
    }

    public void setParameters(Object[] params) {
      // unsupported
    }

    public Object getTimer() {
      // unsupported
      return null;
    }

    public Object proceed() throws Exception {
      // unsupported
      return null;
    }
  }
  
  

}
