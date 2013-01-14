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
package com.camunda.fox.client.impl;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.camunda.bpm.application.spi.EjbProcessApplication;


/**
 * 
 * @author Daniel Meyer
 * 
 */
//singleton bean guarantees maximum efficiency
@Singleton
@Startup
//make sure the container does not synchronize access to this bean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
@Deprecated
public class ProcessArchiveSupport extends EjbProcessApplication {
  
  public static final String PROCESS_ARCHIVE_SERVICE_NAME = EjbProcessApplication.PROCESS_ENGINE_SERVICE_NAME;

  public static final String PROCESS_ENGINE_SERVICE_NAME = EjbProcessApplication.PROCESS_ARCHIVE_SERVICE_NAME;
  
  @PostConstruct
  public void start() {
    super.start();
  }

  @PreDestroy
  public void stop() {
    super.stop();
  }
    
}
