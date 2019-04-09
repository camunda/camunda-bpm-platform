/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl.web;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.impl.ejb.DefaultEjbProcessApplication;


/**
 * <p>Sets the ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH property if this is 
 * deployed as part of a WebApplication.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessArchiveServletContextListener implements ServletContextListener {
  
  @EJB
  private ProcessApplicationInterface defaultEjbProcessApplication;

  public void contextInitialized(ServletContextEvent contextEvent) {

    String contextPath = contextEvent.getServletContext().getContextPath();
    
    defaultEjbProcessApplication.getProperties().put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, contextPath);
    
  }

  public void contextDestroyed(ServletContextEvent arg0) {
  }

}
