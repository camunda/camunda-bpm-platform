/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.platform.impl.configuration;

import java.lang.reflect.Method;
import java.util.EmptyStackException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;

import org.activiti.cdi.BusinessProcess;
import org.activiti.cdi.impl.context.BusinessProcessAssociationManager;
import org.activiti.cdi.impl.util.ProgrammaticBeanLookup;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.context.ExecutionContext;
import org.activiti.engine.impl.delegate.DelegateInvocation;
import org.activiti.engine.impl.interceptor.DelegateInterceptor;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>Flushes the process variable cache of the {@link BusinessProcess}-bean.</p> 
 * 
 * @author Daniel Meyer
 */
public class CmpeVariableFlushingDelegateInterceptor implements DelegateInterceptor {
  
  private static Logger log = Logger.getLogger(CmpeVariableFlushingDelegateInterceptor.class.getName());
  
  protected final ProcessArchiveServices paArchiveServices;

  public CmpeVariableFlushingDelegateInterceptor(ProcessArchiveServices paArchiveServices) {
    this.paArchiveServices = paArchiveServices;
  }
  
  @Override
  public void handleInvocation(DelegateInvocation invocation) throws Exception {
    
    invocation.proceed();
    
    BeanManager beanManager = paArchiveServices.getBeanManager();
    if(beanManager != null) {      
      // make sure we run inside the pa
      if(ProcessArchiveContext.isWithinProcessArchive()) {
        flushVariables();
      } else {
        ProcessArchiveContext.executeWithinCurrentContext(getCallback());
      }
    }
  }

  protected ProcessArchiveCallback<Void> getCallback() {
    return new ProcessArchiveCallback<Void>() {
      public Void execute() {
        flushVariables();
        return null;
      }
    };
  }

  protected void flushVariables() {       
    try {  
      Object associationManager = getAssociationManagerInstance();
      if(associationManager != null) {
        Object beanStore = getBeanStore(associationManager);
        Map<String,Object> processVariables = getProcessVariables(beanStore);     
        
        ExecutionContext executionContext = Context.getExecutionContext();
        executionContext.getExecution().setVariables(processVariables);
        
        log.log(Level.FINE, "Process variable flush summary:  "+processVariables);
      }else {
        log.log(Level.FINE, "Not flushing variables, activiti-cdi classes not available. ");
      }
      
    }catch (EmptyStackException e) {
      // we ignore this 
    }         
  }

  protected Map<String, Object> getProcessVariables(Object beanStore) {
    try {
      Method getAllMethod = beanStore.getClass().getMethod("getAll");
      return (Map<String, Object>) getAllMethod.invoke(beanStore);
    }catch (Exception e) {
      throw new FoxPlatformException("Could not get process variables from BeanStore", e);
    }
  }

  protected Object getBeanStore(Object associationManager) {
    try {
      Method getBeanStoreMethod = associationManager.getClass().getMethod("getBeanStore");
      return getBeanStoreMethod.invoke(associationManager);
    }catch (Exception e) {
      throw new FoxPlatformException("Exception while getting BeanStore from BusinessProcessAssociationManager", e);
    }
    
  }

  protected Object getAssociationManagerInstance() {
    BeanManager beanManager = paArchiveServices.getBeanManager();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    // we are inside the pa -> this classloader knows the pa's version of the BusinessProcessAssociationManager
    try {
      Class< ? > associationManagerClass = classLoader.loadClass(BusinessProcessAssociationManager.class.getName());
      return ProgrammaticBeanLookup.lookup(associationManagerClass, beanManager);
    }catch (Exception e) {
      log.log(Level.FINE, "Could not lookup BusinessProcessAssociationManager", e);
      return null;
    }
  }

}
