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
package org.camunda.bpm.engine.spring.application;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationElResolver;
import org.camunda.bpm.application.impl.EjbProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;
import org.camunda.bpm.engine.spring.ApplicationContextElResolver;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>ProcessApplicationElResolver implementation providing support for the Spring Framework.</p>
 * 
 * <p>This implementation supports the following environments:
 *  <ul>
 *    <li>Bootstrapping through {@link SpringProcessApplication}. In this case the spring application context 
 *        is retrieved from the {@link SpringProcessApplication} class.</li>
 *    <li>Bootstrapping through {@link ServletProcessApplication}. In this case we have access to the {@link ServletContext}
 *        which allows accessing the web application's application context through the WebApplicationContextUtils class.</li>    
 *    </li>
 *  </ul>
 * </p>
 * 
 * <p><strong>Limitation</strong>: The {@link EjbProcessApplication} is currently unsupported.</p>
 *  
 * @author Daniel Meyer
 *
 */
public class SpringProcessApplicationElResolver implements ProcessApplicationElResolver {
  
  private final static Logger LOGGER = Logger.getLogger(SpringProcessApplicationElResolver.class.getName());

  public Integer getPrecedence() {
    return ProcessApplicationElResolver.SPRING_RESOLVER;
  }

  public ELResolver getElResolver(AbstractProcessApplication processApplication) {
    
    if (processApplication instanceof SpringProcessApplication) {
      SpringProcessApplication springProcessApplication = (SpringProcessApplication) processApplication;
      return new ApplicationContextElResolver(springProcessApplication.getApplicationContext());
      
    } else if (processApplication instanceof ServletProcessApplication) {
      ServletProcessApplication servletProcessApplication = (ServletProcessApplication) processApplication;
      
      if(!ClassUtils.isPresent("org.springframework.web.context.support.WebApplicationContextUtils", processApplication.getProcessApplicationClassloader())) {
        LOGGER.log(Level.FINE, "WebApplicationContextUtils must be present for SpringProcessApplicationElResolver to work");
        return null;
      }
      
      ServletContext servletContext = servletProcessApplication.getServletContext();
      WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
      if(applicationContext != null) {
        return new ApplicationContextElResolver(applicationContext);
      }
      
    }
    
    LOGGER.log(Level.FINE, "Process application class {0} unsupported by SpringProcessApplicationElResolver", processApplication);    
    return null;
  }
  
}
