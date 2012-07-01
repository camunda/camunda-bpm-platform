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
package com.camunda.fox.platform.impl.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.impl.service.ProcessEngineController;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * @author Daniel Meyer
 */
public class DefaultProcessArchiveServices implements ProcessArchiveServices {
  
  private static Logger log = Logger.getLogger(DefaultProcessArchiveServices.class.getName());

  protected ProcessEngineController processEngineServiceController;
    
  public void setProcessEngineController(ProcessEngineController processEngineServiceController) {
    this.processEngineServiceController = processEngineServiceController;
  }

  public BeanManager getBeanManager() {
    if(ProcessArchiveContext.getCurrentContext() == null) {
      return null;
    } else {
      return ProcessArchiveContext.executeWithinCurrentContext(new ProcessArchiveCallback<BeanManager>() {    
        public BeanManager execute() {
          InitialContext initialContext = getInitialContext();          
          return lookupBeanManagerInJndi(initialContext);
        }      
      });    
    }
  }

 
  protected InitialContext getInitialContext() {
    try {
      return new InitialContext();
    } catch (NamingException e) {
      throw new FoxPlatformException("Could not create JNDI InitialContext: ", e);
    }
  }

  public EntityManagerFactory getEntityManagerFactory() {
    // TODO!
    return null;
  }
  
  protected BeanManager lookupBeanManagerInJndi(InitialContext initialContext) {
    try {
      return (BeanManager) initialContext.lookup("java:comp/BeanManager");
    }catch (NamingException e) {
      // current pa might not be a CDI deployment (=no beans.xml)
      log.log(Level.FINE, "Could not lookup BeanManager in JNDI ", e);
      return null;
    }
  }

  public ProcessArchiveContext getProcessArchiveContext(String processDefinitionKey) {
    return processEngineServiceController.getProcessArchiveContext(processDefinitionKey);
  }
  
  public ProcessArchiveContext getProcessArchiveContextForExecution(ExecutionEntity executionEntity) {
    ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) executionEntity.getProcessDefinition();    
    return getProcessArchiveContext(processDefinitionEntity.getKey());
  }


}
