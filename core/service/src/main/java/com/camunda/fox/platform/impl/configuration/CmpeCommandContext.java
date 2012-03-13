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

import static com.camunda.fox.platform.impl.context.ProcessArchiveContext.executeWithinContext;
import static com.camunda.fox.platform.impl.context.ProcessArchiveContext.isWithinProcessArchive;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.pvm.runtime.AtomicOperation;
import org.activiti.engine.impl.pvm.runtime.InterpretableExecution;

import com.camunda.fox.platform.impl.context.ProcessArchiveContext;
import com.camunda.fox.platform.impl.context.ProcessArchiveServicesSupport;
import com.camunda.fox.platform.impl.context.spi.ProcessArchiveServices;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>{@link CommandContext} performing context switching, ensuring that {@link AtomicOperation}s are 
 * executed "within" the process archive</p> 
 *  
 * @author Daniel Meyer
 */
public class CmpeCommandContext extends CommandContext implements ProcessArchiveServicesSupport {

  private ProcessArchiveServices processArchiveServices;

  public CmpeCommandContext(Command< ? > command, ProcessEngineConfigurationImpl processEngineConfiguration) {
    super(command, processEngineConfiguration);
  }

  @Override
  public void performOperation(final AtomicOperation executionOperation, final InterpretableExecution execution) {
    
    ProcessArchiveContext targetContext = processArchiveServices.getProcessArchiveContextForExecution((ExecutionEntity) execution);
    
    if(requiresContextSwitch(executionOperation, targetContext)) {      
        executeWithinContext(getCallback(executionOperation, execution), targetContext);        
    } else {      
      super.performOperation(executionOperation, execution);      
    }    
    
  }

  protected ProcessArchiveCallback<Void> getCallback(final AtomicOperation executionOperation, final InterpretableExecution execution) {
    return new ProcessArchiveCallback<Void>() {
      @Override
      public Void execute() {
        CmpeCommandContext.super.performOperation(executionOperation, execution);
        return null;
      };
    };
  }

  protected boolean requiresContextSwitch(final AtomicOperation executionOperation, ProcessArchiveContext paContext) {
    return paContext != null
            && !paContext.isUndelploying()
            && !isWithinProcessArchive(paContext);
  }

  @Override
  public void setProcessArchiveServices(ProcessArchiveServices processArchiveServices) {
    this.processArchiveServices = processArchiveServices;
  }

}
