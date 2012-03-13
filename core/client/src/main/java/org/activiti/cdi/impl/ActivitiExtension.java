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
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.cdi.impl;

import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.activiti.cdi.annotation.BusinessProcessScoped;
import org.activiti.cdi.impl.context.BusinessProcessContext;
import org.activiti.cdi.impl.context.ThreadContext;
import org.activiti.cdi.impl.context.ThreadScoped;
import org.activiti.cdi.impl.util.BeanManagerLookup;
import org.activiti.cdi.spi.ProcessEngineLookup;

/**
 * Customized version of the ActivitiExtension which does not 
 * attempt to lookup a ProcessEngine  
 *  
 * @author Daniel Meyer
 */
public class ActivitiExtension implements Extension {

  private static Logger logger = Logger.getLogger(ActivitiExtension.class.getName());
  private ProcessEngineLookup processEngineLookup;

  public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
    event.addScope(BusinessProcessScoped.class, true, true);
    event.addScope(ThreadScoped.class, true, false);
  }

  public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {       
    BeanManagerLookup.localInstance = manager;
    event.addContext(new BusinessProcessContext(manager));
    event.addContext(new ThreadContext());
  }

  public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
    logger.info("Initializing activiti-cdi.");      
  }

  public void beforeShutdown(@Observes BeforeShutdown event) {
    if(processEngineLookup != null) {
      processEngineLookup.ungetProcessEngine();
      processEngineLookup = null;
    }
    logger.info("Shutting down activiti-cdi");    
  }

}
