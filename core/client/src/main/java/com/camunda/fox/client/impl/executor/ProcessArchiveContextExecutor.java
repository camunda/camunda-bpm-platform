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
package com.camunda.fox.client.impl.executor;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>Singleton bean which allows to execute callbacks in the context the 
 * carrier process archive.</p>
 *  
 * @author Daniel Meyer
 * @see ProcessArchive#executeWithinContext(ProcessArchiveCallback)
 */
// singleton bean guarantees maximum efficiency
@Singleton
//make sure the container does not synchronize access to this bean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
public class ProcessArchiveContextExecutor {
  
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws
  // gets past the EJB container (must be unwrapped by caller)
  FoxApplicationException  
  {
    try {
      return callback.execute();  
    }catch (Exception e) {
      throw new FoxApplicationException("Caught Exception", e);
    }
  }
  
}
