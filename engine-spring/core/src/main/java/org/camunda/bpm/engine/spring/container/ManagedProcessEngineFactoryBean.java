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
package org.camunda.bpm.engine.spring.container;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;

/**
 * <p>Factory bean registering a spring-managed process engine with the {@link BpmPlatform}.</p>
 * 
 * <p>Replacement for {@link ProcessEngineFactoryBean}. Use this implementation if you want to 
 * register a process engine configured in a spring application context with the {@link BpmPlatform}.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ManagedProcessEngineFactoryBean extends ProcessEngineFactoryBean {
  
  public ProcessEngine getObject() throws Exception {
    ProcessEngine processEngine = super.getObject();
    
    RuntimeContainerDelegate runtimeContainerDelegate = getRuntimeContainerDelegate();
    runtimeContainerDelegate.registerProcessEngine(processEngine);
    
    return processEngine;
  }

  protected RuntimeContainerDelegate getRuntimeContainerDelegate() {
    return RuntimeContainerDelegate.INSTANCE.get();
  }
  
  public void destroy() throws Exception {
    
    if(processEngine != null) {
      getRuntimeContainerDelegate().unregisterProcessEngine(processEngine);
    }
    
    super.destroy();
  }

}
