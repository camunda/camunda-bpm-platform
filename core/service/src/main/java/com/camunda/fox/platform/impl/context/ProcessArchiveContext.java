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

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

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
  
  ///////////////////  static

  private static ThreadLocal<ProcessArchiveContext> currentProcessArchiveContext = new ThreadLocal<ProcessArchiveContext>();
  

  public static ProcessArchiveContext getCurrentContext() {
    return currentProcessArchiveContext.get();
  }

  private static void setCurrentContext(ProcessArchiveContext processArchiveContext) {
    currentProcessArchiveContext.set(processArchiveContext);
  }
  
  private static ThreadLocal<Boolean> isWithinProcessArchive = new ThreadLocal<Boolean>() {
    protected Boolean initialValue() {
      return false;
    }
  };
  
  public static boolean isWithinProcessArchive() {
    return isWithinProcessArchive.get();
  }
  
  public static boolean isWithinProcessArchive(ProcessArchiveContext processArchiveContext) {
    return isWithinProcessArchive.get() && processArchiveContext.equals(getCurrentContext());
  }

  private static void setWithinProcessArchive(boolean b) {
    isWithinProcessArchive.set(b);
  }

  public static <T> T executeWithinContext(ProcessArchiveCallback<T> callback, ProcessArchiveContext processArchiveContext) {
    ProcessArchiveContext contextBefore = getCurrentContext();
    try {      
      if(!processArchiveContext.equals(contextBefore))  {        
        setCurrentContext(processArchiveContext);        
        return performContextSwitch(callback);        
      } else {
        return callback.execute();
      }      
    }finally {      
      if(!processArchiveContext.equals(contextBefore))  {
        setCurrentContext(contextBefore);             
      }      
    }
  }
  
  public static <T> T executeWithinCurrentContext(ProcessArchiveCallback<T> callback) {
    if(!isWithinProcessArchive()) {
      return performContextSwitch(callback);
    } else {
      return callback.execute();
    }  
  }

  private static <T> T performContextSwitch(ProcessArchiveCallback<T> callback) {
    try {
      setWithinProcessArchive(true);
      ProcessArchiveContext processArchiveContext = ProcessArchiveContext.getCurrentContext();         
      if(processArchiveContext != null) {
        try {
          return processArchiveContext
            .getProcessArchive()              
            .executeWithinContext(callback);        
        }catch (Exception e) {
          // unwrap exception
          if(e.getCause() != null && e.getCause() instanceof RuntimeException) {
            throw (RuntimeException) e.getCause();
          }else {
            throw new FoxPlatformException("Unexpected exeption while executing in process archive context ", e);
          }
        }
      } else {
        throw new FoxPlatformException("Could not determine current process archive.");
      }
    } finally {
      setWithinProcessArchive(false);        
    }
  }
  
}
