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
package com.camunda.fox.platform.impl.context.spi;

import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.EntityManagerFactory;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;

import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.service.ProcessEngineController;

public interface ProcessArchiveServices {

  /**
   * @return the {@link BeanManager} for the current process archive or null, 
   * if the current process archive cannot be determined or if the process 
   * archive is not a CDI deployment 
   */
  public BeanManager getBeanManager();

  public EntityManagerFactory getEntityManagerFactory();

  public ProcessArchiveContext getProcessArchiveContext(String processDefinitionKey);

  public ProcessEngineController getProcessEngineController();
  
  public void setProcessEngineController(ProcessEngineController processEngineServiceBean);

  /**
   * This method assumes it it called from an activiti command 
   * 
   * @param executionEntity
   * @return
   */
  public ProcessArchiveContext getProcessArchiveContextForExecution(ExecutionEntity executionEntity);
  
}