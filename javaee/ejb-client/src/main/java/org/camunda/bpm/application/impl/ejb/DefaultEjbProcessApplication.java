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
package org.camunda.bpm.application.impl.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.engine.cdi.impl.event.CdiEventListener;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * 
 * @author Daniel Meyer
 * @author Roman Smirnov
 * 
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@ProcessApplication
@Local(ProcessApplicationInterface.class)
public class DefaultEjbProcessApplication extends EjbProcessApplication {

  protected Map<String, String> properties = new HashMap<String, String>();

  private final CdiEventListener cdiEventListener = new CdiEventListener();

  @PostConstruct
  public void start() {
    deploy();
  }

  @PreDestroy
  public void stop() {
    undeploy();
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public ExecutionListener getExecutionListener() {
    return cdiEventListener;
  }

  @Override
  public TaskListener getTaskListener() {
    return cdiEventListener;
  }
}
