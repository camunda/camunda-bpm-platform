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

import org.activiti.engine.repository.Deployment;
import org.camunda.bpm.application.spi.ProcessApplicationReference;
import org.camunda.bpm.engine.application.ProcessApplicationRegistration;

import com.camunda.fox.platform.spi.ProcessArchive;

/**
 * 
 * @author Daniel Meyer
 */
public class ProcessArchiveContext {

  //////////////// instance 
  
  private final Deployment activitiDeployment;
  private final ProcessArchive processArchive;
  private boolean isActive;
  private boolean isUndelploying;
  private ProcessApplicationRegistration processApplicationRegistration;
  private ProcessApplicationReference processApplicationReference;
  
  public ProcessArchive getProcessArchive() {
    return processArchive;
  }
  
  public ProcessArchiveContext(Deployment activitiDeployment, ProcessArchive processArchive) {
    this.activitiDeployment = activitiDeployment;
    this.processArchive = processArchive;
  }

  public Deployment getActivitiDeployment() {
    return activitiDeployment;
  }
  
  public void setActive(boolean b) {
    isActive = b;
  }
    
  public boolean isActive() {
    return isActive;
  }
  
  public boolean isUndelploying() {
    return isUndelploying;
  }
  
  public void setUndelploying(boolean isUndelploying) {
    this.isUndelploying = isUndelploying;
  }
    
  public void setProcessApplicationRegistration(ProcessApplicationRegistration registration) {
    this.processApplicationRegistration = registration;
  }
  
  public ProcessApplicationRegistration getProcessApplicationRegistration() {
    return processApplicationRegistration;
  }

  public void setProcessApplicationReference(ProcessApplicationReference processApplicationReference) {
    this.processApplicationReference = processApplicationReference;
  }
  
  public ProcessApplicationReference getProcessApplicationReference() {
    return processApplicationReference;
  }
  
  
}
